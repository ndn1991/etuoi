package com.xlot.sms.sender;

import java.util.ArrayList;
import java.util.Collection;

import com.mario.entity.impl.BaseLifeCycle;
import com.nhb.common.async.RPCFuture;
import com.nhb.common.async.translator.RPCFutureTranslator;
import com.nhb.common.data.PuObjectRO;

import vn.speedsms.client.SpeedSMSClient;
import vn.speedsms.client.SpeedSMSRPCFuture;
import vn.speedsms.client.SpeedSMSSendingResponse;
import vn.speedsms.client.enums.SpeedSMSType;

public class SpeedSmsSender extends BaseLifeCycle implements SmsSender {
	private String accessToken;
	private SpeedSMSClient speedSmsClient;

	@Override
	public void init(PuObjectRO initParams) {
		accessToken = initParams.getString("accessToken");
		speedSmsClient = new SpeedSMSClient(accessToken);
	}

	@Override
	public void destroy() throws Exception {
		if (speedSmsClient != null) {
			System.out.println("trying to close speed sms client");
			speedSmsClient.close();
		}
	}

	@Override
	public RPCFuture<SendSMSResult> send(String content, Collection<String> recipients) {
		SpeedSMSRPCFuture future = speedSmsClient.send(content, new ArrayList<>(recipients), SpeedSMSType.CUSTOMER_CARE,
				null);
		return new RPCFutureTranslator<SpeedSMSSendingResponse, SendSMSResult>(future) {
			@Override
			protected SendSMSResult translate(SpeedSMSSendingResponse response) throws Exception {
				if (response == null) {
					getLogger().error("Null response from speed sms client, ", future.getFailedCause());
					return null;
				} else {
					SendSMSResult rs = new SendSMSResult();
					rs.setMessage(response.getError() == null ? null : response.getError().getMessage());
					rs.setStatus(response.getError() == null ? 0 : response.getError().getCode());
					rs.setTotalPrice(response.getTotalPrice());
					rs.setTotalSMS(response.getTotalSMS());
					rs.setTransactionId(response.getTransactionId());
					rs.getInvalidPhone().addAll(response.getInvalidPhone());
					return rs;
				}
			}
		};
	}

}
