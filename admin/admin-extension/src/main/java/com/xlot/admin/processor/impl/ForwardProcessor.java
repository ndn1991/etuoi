package com.xlot.admin.processor.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Strings;
import com.nhb.common.async.RPCFuture;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.data.PuValue;
import com.xlot.admin.bean.PermissionBean;
import com.xlot.admin.model.RbacModel;
import com.xlot.admin.processor.AbstractAdminProcessor;
import com.xlot.admin.statics.Permissions;
import com.xlot.admin.statics.Status;

import lombok.Data;

public class ForwardProcessor extends AbstractAdminProcessor {
	private final Map<String, ForwardConfig> forwardConfigMap = new HashMap<>();
	private RbacModel rbacModel;

	@Override
	protected RPCFuture<PuElement> _process(PuObjectRO _params) {
		String command = _params.getString("command");
		ForwardConfig forwardConfig = forwardConfigMap.get(command);
		if (forwardConfig == null) {
			return futureResponse(baseResponse(Status.COMMAND_NOT_FOUND));
		}
		if (forwardConfig.isRequireLoggedIn() && !_params.variableExists(ADMIN_ID)) {
			return futureResponse(baseResponse(Status.USER_NOT_LOGGED_IN));
		}
		byte[] adminId = _params.getRaw(ADMIN_ID);
		Status verifyPermissionStatus = verifyPermission(adminId, forwardConfig);
		if (verifyPermissionStatus != Status.SUCCESS) {
			return futureResponse(baseResponse(verifyPermissionStatus));
		}

		PuObject params = new PuObject();
		params.addAll(_params);
		if (!Strings.isNullOrEmpty(forwardConfig.getForwardCommand())) {
			params.setString("command", forwardConfig.getForwardCommand());
		}
		if (forwardConfig.isCommandShortcut()) {
			params.setString("c", params.getString("command", null));
		}

		getLogger().debug("forward params: {}", params);
		return getProducerManager().getRPCProducer(forwardConfig.getQueue()).publish(params);
	}

	@Override
	public void init(PuObjectRO params) {
		rbacModel = getModelFactory().getModel(RbacModel.class.getName());

		PuObject configs = params.getPuObject("config");
		for (Entry<String, PuValue> e : configs) {
			PuObject config = e.getValue().getPuObject();
			ForwardConfig forwardConfig = new ForwardConfig();
			forwardConfig.setForwardCommand(config.getString("forwardCommand", e.getKey()));
			forwardConfig.setQueue(config.getString("queue"));
			forwardConfig.setPermission(config.getString("permission", null));
			forwardConfig.setRequireLoggedIn(config.getBoolean("requireLoggedIn", true));
			forwardConfig.setCommandShortcut(config.getBoolean("commandShortcut", false));
			forwardConfigMap.put(e.getKey(), forwardConfig);
		}
	}

	private Status verifyPermission(byte[] adminId, ForwardConfig forwardConfig) {
		if (Strings.isNullOrEmpty(forwardConfig.getPermission())) {
			return Status.SUCCESS;
		}
		List<PermissionBean> permissions = rbacModel.permissionsOfUser(adminId);
		if (permissions != null && !permissions.isEmpty()) {
			for (PermissionBean p : permissions) {
				if (p.getName().equalsIgnoreCase(Permissions.SUPER_USER)) {
					return Status.SUCCESS;
				}
				if (p.getName().equalsIgnoreCase(forwardConfig.getPermission())) {
					return Status.SUCCESS;
				}
			}
		}
		return Status.NOT_HAVE_PERMISSION;
	}

	@Data
	class ForwardConfig {
		private String queue;
		private String forwardCommand;
		private String permission;
		private boolean requireLoggedIn;
		private boolean commandShortcut;
	}
}
