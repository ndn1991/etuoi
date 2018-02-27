package com.xlot.admin.processor.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.nhb.common.async.BaseRPCFuture;
import com.nhb.common.async.Callback;
import com.nhb.common.async.RPCFuture;
import com.nhb.common.data.PuArray;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.data.PuValue;
import com.nhb.common.utils.Converter;
import com.nhb.common.vo.ByteArrayWrapper;
import com.xlot.admin.processor.AbstractAdminProcessor;
import com.xlot.admin.statics.AssetTypes;
import com.xlot.id.IdClient;
import com.xlot.id.IdConfig;
import com.xlot.id.IdRpcFuture;
import com.xlot.id.bean.UserBean;
import com.xlot.id.message.MessageRsp;
import com.xlot.id.message.impl.GetUsersReq;
import com.xlot.id.message.lobby.GetUsersRsp;

public class SearchUserProcessor extends AbstractAdminProcessor {
	private String figuresQueue;
	private String forwardCommand;
	private IdClient idClient;

	@Override
	public void init(PuObjectRO params) {
		figuresQueue = params.getString("figuresQueue");
		forwardCommand = params.getString("forwardCommand");
		IdConfig idConfig = new IdConfig(params.getString("idQueue"));
		idClient = new IdClient(getProducerManager().getRabbitMQConnection(), idConfig);
	}

	@Override
	protected RPCFuture<PuElement> _process(PuObjectRO _params) {
		PuObject params = new PuObject();
		params.addAll(_params);
		params.setString("command", forwardCommand);
		RPCFuture<PuElement> future = getProducerManager().getRPCProducer(figuresQueue).publish(params);
		future.setTimeout(30, TimeUnit.SECONDS);
		final BaseRPCFuture<PuElement> rs = new BaseRPCFuture<>();
		future.setCallback(new Callback<PuElement>() {
			@Override
			public void apply(PuElement result) {
				if (result == null) {
					getLogger().error("error when fetch user list", future.getFailedCause());
					rs.setFailedCause(future.getFailedCause());
					rs.setAndDone(null);
					return;
				}
				userGettedProcess(rs, result);
			}
		});
		return rs;
	}

	private List<byte[]> getUserIds(PuArray pua) {
		List<byte[]> list = new ArrayList<>(pua.size());
		for (PuValue user : pua) {
			list.add(Converter.uuidToBytes(user.getPuObject().getString("userId")));
		}
		return list;
	}

	private void addMoneyField(PuArray data, Map<ByteArrayWrapper, Long> moneys) {
		for (PuValue user : data) {
			byte[] userId = Converter.uuidToBytes(user.getPuObject().getString("userId"));
			user.getPuObject().setLong("money", moneys.getOrDefault(new ByteArrayWrapper(userId), -1l));
		}
	}

	private void userGettedProcess(final BaseRPCFuture<PuElement> rs, PuElement result) {
		PuObject puo = (PuObject) result;
		PuArray data = puo.getPuObject("data").getPuArray("data");
		List<byte[]> userIds = getUserIds(data);
		RPCFuture<Map<ByteArrayWrapper, Long>> amsFuture = getAssets(userIds, AssetTypes.MONEY);
		amsFuture.setTimeout(30, TimeUnit.SECONDS);
		amsFuture.setCallback(new Callback<Map<ByteArrayWrapper, Long>>() {
			@Override
			public void apply(Map<ByteArrayWrapper, Long> result) {
				if (result == null) {
					getLogger().error("error when get moneys", amsFuture.getFailedCause());
					rs.setFailedCause(amsFuture.getFailedCause());
					rs.setAndDone(null);
					return;
				}
				addMoneyField(data, result);
				addStatus(rs, puo, data, userIds);
			}
		});
	}

	private void addStatus(final BaseRPCFuture<PuElement> rs, PuObject puo, PuArray data, List<byte[]> userIds) {
		GetUsersReq idReq = new GetUsersReq();
		idReq.getUserIds().addAll(userIds);
		IdRpcFuture idFuture = idClient.send(idReq);
		idFuture.setTimeout(30, TimeUnit.SECONDS);
		idFuture.setCallback(new Callback<MessageRsp>() {
			@Override
			public void apply(MessageRsp result) {
				if (result == null) {
					rs.setFailedCause(idFuture.getFailedCause());
					rs.setAndDone(null);
				} else {
					GetUsersRsp rsp = (GetUsersRsp) result;
					Map<ByteArrayWrapper, UserBean> userMap = new HashMap<>();
					for (UserBean user : rsp.getUsers()) {
						userMap.put(new ByteArrayWrapper(user.getUserId()), user);
					}
					for (PuValue user : data) {
						byte[] userId = Converter.uuidToBytes(user.getPuObject().getString("userId"));
						ByteArrayWrapper key = new ByteArrayWrapper(userId);
						if (userMap.containsKey(key)) {
							user.getPuObject().setInteger("status", userMap.get(key).getStatus());
						}
					}
					rs.setAndDone(puo);
				}
			}
		});
	}

}
