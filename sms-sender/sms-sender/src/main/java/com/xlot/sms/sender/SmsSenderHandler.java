package com.xlot.sms.sender;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.mario.entity.MessageHandleCallback;
import com.mario.entity.impl.BaseMessageHandler;
import com.mario.entity.message.CloneableMessage;
import com.mario.entity.message.Message;
import com.mario.services.sms.SmsService;
import com.nhb.common.async.Callback;
import com.nhb.common.async.RPCFuture;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuNull;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.db.models.ModelFactory;
import com.nhb.common.utils.UUIDUtils;
import com.nhb.common.vo.ByteArrayWrapper;
import com.xlot.sms.sender.bean.SmsLogBean;
import com.xlot.sms.sender.model.MongoSmsLogModel;
import com.xlot.sms.sender.statics.ErrorMessages;
import com.xlot.sms.sender.statics.F;
import com.xlot.sms.sender.vo.RequestCount;

import lombok.Getter;

public class SmsSenderHandler extends BaseMessageHandler implements RequestCountChecker, Common {
	@Getter
	private final ConcurrentHashMap<ByteArrayWrapper, RequestCount> user2ReqCount = new ConcurrentHashMap<>();
	@Getter
	private final ConcurrentHashMap<String, RequestCount> phone2ReqCount = new ConcurrentHashMap<>();

	private int delayMinutes = 10;
	private int maxPerUnit = 1;

	private SmsSender smsSender;
	private MongoSmsLogModel model;

	@Override
	public void init(PuObjectRO initParams) {
		delayMinutes = initParams.getInteger("delayMinutes", delayMinutes);
		maxPerUnit = initParams.getInteger("maxPerUnit", maxPerUnit);
		SmsService<RPCFuture<SendSMSResult>> service = getApi().getSmsService(initParams.getString("smsService"));
		smsSender = (SmsSender) service;
		ModelFactory modelFactory = new ModelFactory();
		modelFactory.setClassLoader(getClass().getClassLoader());
		modelFactory.setMongoClient(getApi().getMongoClient(initParams.getString("mongo")));
		modelFactory.setEnvironmentVariable(F.DATABASE, initParams.getString(F.DATABASE));
		model = modelFactory.getModel(MongoSmsLogModel.class.getName());
	}

	@Override
	public PuElement handle(Message message) {
		PuElement _data = message.getData();
		getLogger().debug("handling message: {}", _data);
		if (!(_data instanceof PuObject)) {
			return null;
		}
		PuObject params = (PuObject) _data;
		byte[] userId = getRaw(params, "userId");
		String phone = params.getString("phone");
		String content = params.getString("content");
		long delayMinutes = params.getLong("delayMinutes", this.delayMinutes);
		long delayNanoseconds = ((long) delayMinutes) * MINUTE;

		if (!checkReqCount(phone, delayNanoseconds, maxPerUnit)) {
			String msg = String.format(ErrorMessages.TOO_FAST, getRemainTime(phone, delayNanoseconds).toString());
			return message(Status.REQUEST_TOO_FAST, msg);
		}

		if (userId != null && !checkReqCount(userId, delayNanoseconds, maxPerUnit)) {
			String msg = String.format(ErrorMessages.TOO_FAST, getRemainTime(phone, delayNanoseconds).toString());
			return message(Status.REQUEST_TOO_FAST, msg);
		}
		RPCFuture<SendSMSResult> future = smsSender.send(content, phone);

		SmsLogBean logBean = makeLogBean(phone);
		model.log(logBean);

		Message cloneMessage = (message instanceof CloneableMessage) ? ((CloneableMessage) message).makeClone() : null;
		MessageHandleCallback callback = message.getCallback();

		future.setTimeout(30, TimeUnit.SECONDS);
		future.setCallback(new Callback<SendSMSResult>() {
			@Override
			public void apply(SendSMSResult smsResult) {
				if (smsResult == null) {
					logBean.setId(UUIDUtils.timebasedUUIDAsBytes());
					logBean.setStatus(Status.TIMEOUT);
					logBean.setTimeout(true);
					model.log(logBean);

					callback.onHandleError(cloneMessage, future.getFailedCause());
				} else {
					logBean.setId(UUIDUtils.timebasedUUIDAsBytes());
					logBean.setStatus(Status.OK);
					logBean.setResult(smsResult);
					logBean.getInvalidPhones().addAll(smsResult.getInvalidPhone());
					model.log(logBean);

					callback.onHandleComplete(cloneMessage, smsResult.toPuObject());
				}
			}
		});

		return PuNull.IGNORE_ME;
	}

	private SmsLogBean makeLogBean(String phone) {
		byte[] logId = UUIDUtils.timebasedUUIDAsBytes();
		SmsLogBean logBean = new SmsLogBean();
		logBean.setId(UUIDUtils.timebasedUUIDAsBytes());
		logBean.setLogId(logId);
		logBean.setStatus(Status.SENT);
		logBean.getSendPhones().addAll(Arrays.asList(phone));
		logBean.setTimeout(false);
		return logBean;
	}

}
