package com.xlot.admin;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Strings;
import com.mario.entity.MessageHandleCallback;
import com.mario.entity.impl.BaseMessageHandler;
import com.mario.entity.message.CloneableMessage;
import com.mario.entity.message.Message;
import com.mario.gateway.rabbitmq.RabbitMQServerWrapper;
import com.nhb.common.async.Callback;
import com.nhb.common.async.RPCFuture;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuNull;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.db.models.ModelFactory;
import com.nhb.common.utils.FileSystemUtils;
import com.xlot.admin.bean.PermissionBean;
import com.xlot.admin.id.JWTUtils;
import com.xlot.admin.id.JwtLoginData;
import com.xlot.admin.model.RbacModel;
import com.xlot.admin.model.UserModel;
import com.xlot.admin.processor.AdminProcessor;
import com.xlot.admin.processor.AdminProcessorFactory;
import com.xlot.admin.statics.Permissions;
import com.xlot.admin.statics.Status;

import scala.Tuple2;

public class AdminHandler extends BaseMessageHandler implements RbacInitializer {
	private static final String FORWARD = "__forward__";

	private AdminProcessorFactory processorFactory;

	private ModelFactory modelFactory;
	private ProducerManager producerManager;
	private UserCache userCache;

	private RbacModel rbacModel;

	@Override
	public void init(PuObjectRO initParams) {
		RabbitMQServerWrapper rabbitServer = getApi().getServerWrapper(initParams.getString("rabbitServer"));
		producerManager = new ProducerManager(rabbitServer);

		Properties modelMapping = loadModelConfig(initParams.getString("modelMappingFile"));
		modelFactory = new ModelFactory(getApi().getDatabaseAdapter(initParams.getString("mysql")));
		modelFactory.setClassLoader(this.getClass().getClassLoader());
		modelFactory.addClassImplMapping(modelMapping);

		UserModel userModel = modelFactory.getModel(UserModel.class.getName());
		rbacModel = modelFactory.getModel(RbacModel.class.getName());
		initRootUser(userModel, rbacModel, initParams.getString("rootUser"), initParams.getString("rootPassword"));
		userCache = new UserCache(userModel);

		String commandPath = initParams.getString("commandPath");
		try {
			processorFactory = AdminProcessorFactory.builder() //
					.modelFactory(modelFactory) //
					.producerManager(producerManager) //
					.userCache(userCache) //
					.build()
					.init(FileSystemUtils.createAbsolutePathFrom("extensions", getExtensionName(), commandPath));
		} catch (IOException e) {
			getLogger().error("error", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public PuElement handle(Message message) {
		PuElement _params = message.getData();
		if (!(_params instanceof PuObject)) {
			getLogger().warn("only support puobject");
			return null;
		}
		PuObject params = (PuObject) _params;
		getLogger().debug("handling request: {}", params);
		String command = params.getString("command");
		AdminProcessor processor = processorFactory.getProcessor(command);
		if (processor == null) {
			processor = processorFactory.getProcessor(FORWARD);
		}
		Tuple2<Status, JwtLoginData> tuple = verifyLoggedIn(processor, params);
		Status verifyLoggedIn = tuple._1;
		JwtLoginData jwtLoginData = tuple._2;
		if (verifyLoggedIn != Status.SUCCESS) {
			return baseResponse(verifyLoggedIn);
		}
		if (jwtLoginData != null) {
			params.setRaw(AdminProcessor.ADMIN_ID, jwtLoginData.getUserId());
			params.setString(AdminProcessor.ADMIN_NAME, jwtLoginData.getUsername());
			Status verifyPermission = verifyPermission(processor, params, jwtLoginData);
			if (verifyPermission != Status.SUCCESS) {
				return baseResponse(verifyPermission);
			}
		}

		Message cloneMessage = (message instanceof CloneableMessage) ? ((CloneableMessage) message).makeClone() : null;
		MessageHandleCallback callback = message.getCallback();
		RPCFuture<PuElement> future = processor.process(params);
		future.setTimeout(30, TimeUnit.SECONDS);
		future.setCallback(new Callback<PuElement>() {
			@Override
			public void apply(PuElement result) {
				if (result == null) {
					getLogger().error("error - {}", future.getFailedCause());
					callback.onHandleError(cloneMessage, future.getFailedCause());
				} else {
					callback.onHandleComplete(cloneMessage, result);
				}
			}
		});
		return PuNull.IGNORE_ME;
	}

	private Status verifyPermission(AdminProcessor processor, PuObject params, JwtLoginData jwtLoginData) {
		if (Strings.isNullOrEmpty(processor.getPermission())) {
			return Status.SUCCESS;
		}
		List<PermissionBean> permissions = rbacModel.permissionsOfUser(jwtLoginData.getUserId());
		if (permissions != null && !permissions.isEmpty()) {
			for (PermissionBean p : permissions) {
				if (p.getName().equalsIgnoreCase(Permissions.SUPER_USER)) {
					return Status.SUCCESS;
				}
				if (p.getName().equalsIgnoreCase(processor.getPermission())) {
					return Status.SUCCESS;
				}
			}
		}
		return Status.NOT_HAVE_PERMISSION;
	}

	private Tuple2<Status, JwtLoginData> verifyLoggedIn(AdminProcessor processor, PuObject params) {
		if (processor.isRequireLoggedIn()) {
			if (!params.variableExists("auth")) {
				return new Tuple2<>(Status.USER_NOT_LOGGED_IN, null);
			}
			return checkHashKey(params);
		} else {
			if (params.variableExists("auth")) {
				return checkHashKey(params);
			}
			return new Tuple2<>(Status.SUCCESS, null);
		}
	}

	private Tuple2<Status, JwtLoginData> checkHashKey(PuObject params) {
		String auth = params.getString("auth");
		PuObject object = (PuObject) JWTUtils.get(auth);
		JwtLoginData jwtLoginData = JwtLoginData.fromPuObject(object);
		byte[] userId = jwtLoginData.getUserId();
		String hashKey = userCache.getHashKey(userId);
		if (hashKey == null) {
			return new Tuple2<>(Status.INVALID_TOKEN, null);
		}
		if (JWTUtils.verifyAndGet(auth, hashKey) == null) {
			return new Tuple2<>(Status.INVALID_TOKEN, null);
		}
		return new Tuple2<>(Status.SUCCESS, jwtLoginData);
	}

	private Properties loadModelConfig(String modelFactoryFile) {
		String path = FileSystemUtils.createAbsolutePathFrom("extensions", getExtensionName(), //
				modelFactoryFile);
		try (InputStream is = new FileInputStream(path)) {
			Properties props = new Properties();
			props.load(is);
			return props;
		} catch (Exception e) {
			getLogger().error("error when load model mapping", e);
		}

		return null;
	}

	private PuObject baseResponse(Status status) {
		PuObject puo = new PuObject();
		puo.setInteger("status", status.getId());
		puo.setString("message", status.getMessage());
		return puo;
	}
}
