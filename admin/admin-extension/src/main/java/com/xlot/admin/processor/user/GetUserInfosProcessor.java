package com.xlot.admin.processor.user;

import com.nhb.common.async.RPCFuture;
import com.nhb.common.async.translator.RPCFutureTranslator;
import com.nhb.common.data.PuArray;
import com.nhb.common.data.PuArrayList;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.data.PuValue;
import com.nhb.common.utils.Converter;
import com.xlot.admin.processor.AbstractAdminProcessor;
import com.xlot.id.IdClient;
import com.xlot.id.IdConfig;
import com.xlot.id.IdRpcFuture;
import com.xlot.id.bean.UserBean;
import com.xlot.id.message.MessageRsp;
import com.xlot.id.message.impl.GetUsersReq;
import com.xlot.id.message.lobby.GetUsersRsp;

public class GetUserInfosProcessor extends AbstractAdminProcessor {
	private IdClient idClient;
	
	@Override
	public void init(PuObjectRO params) {
		IdConfig idConfig = new IdConfig(params.getString("idQueue"));
		idClient = new IdClient(getProducerManager().getRabbitMQConnection(), idConfig);
	}

	@Override
	protected RPCFuture<PuElement> _process(PuObjectRO params) {
		PuArray userIds = params.getPuArray("userIds");
		GetUsersReq idReq = new GetUsersReq();
		for (PuValue userId : userIds) {
			idReq.getUserIds().add(getRaw(userId));
		}
		IdRpcFuture future = idClient.send(idReq);
		return new RPCFutureTranslator<MessageRsp, PuElement>(future) {
			@Override
			protected PuElement translate(MessageRsp sourceResult) throws Exception {
				if (sourceResult == null) {
					return null;
				}
				GetUsersRsp result = (GetUsersRsp) sourceResult;
				PuObject rsp = new PuObject();
				rsp.setInteger("status", 0);
				PuArrayList data = new PuArrayList();
				for (UserBean user : result.getUsers()) {
					PuObject e = new PuObject();
					e.setString("userId", Converter.bytesToUUIDString(user.getUserId()));
					e.setString("username", user.getUsername());
					e.setString("nickname", user.getNickname());
					e.setString("verifiedPhone", user.getVerifiedPhone());
					e.setString("prefix", user.getPrefix());
					e.setInteger("status", user.getStatus());
					e.setString("bundleName", user.getBundleName());
					e.setString("avatar", user.getAvatar());
					e.setString("statisticId", user.getStatisticId());
					e.setString("clientId", user.getClientId());
					data.addFrom(e);
				}
				rsp.setPuArray("data", data);
				return rsp;
			}
		};
	}

}
