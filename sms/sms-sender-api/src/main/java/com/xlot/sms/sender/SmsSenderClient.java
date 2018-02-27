package com.xlot.sms.sender;

import com.nhb.common.BaseLoggable;
import com.nhb.common.async.RPCFuture;
import com.nhb.common.async.translator.RPCFutureTranslator;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.messaging.rabbit.producer.RabbitMQRPCProducer;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SmsSenderClient extends BaseLoggable {
	private final RabbitMQRPCProducer producer;

	public RPCFuture<SendSMSResult> send(byte[] userId, String phone, String content, long delayMinutes) {
		PuObject params = new PuObject();
		params.setRaw("userId", userId);
		params.setString("phone", phone);
		params.setString("content", content);
		params.setLong("delayMinutes", delayMinutes);
		RPCFuture<PuElement> future = producer.publish(params);
		return new RPCFutureTranslator<PuElement, SendSMSResult>(future) {
			@Override
			protected SendSMSResult translate(PuElement source) throws Exception {
				if (source == null) {
					return null;
				}
				return new SendSMSResult().fromPuObject((PuObject) source);
			}
		};
	}

	public RPCFuture<SendSMSResult> send(byte[] userId, String phone, String content) {
		PuObject params = new PuObject();
		params.setRaw("userId", userId);
		params.setString("phone", phone);
		params.setString("content", content);
		RPCFuture<PuElement> future = producer.publish(params);
		return new RPCFutureTranslator<PuElement, SendSMSResult>(future) {
			@Override
			protected SendSMSResult translate(PuElement source) throws Exception {
				if (source == null) {
					return null;
				}
				return new SendSMSResult().fromPuObject((PuObject) source);
			}
		};
	}
}
