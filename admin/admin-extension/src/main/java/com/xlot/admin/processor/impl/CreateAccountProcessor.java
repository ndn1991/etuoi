package com.xlot.admin.processor.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.nhb.common.async.RPCFuture;
import com.nhb.common.data.PuArray;
import com.nhb.common.data.PuArrayList;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.data.PuValue;
import com.nhb.common.utils.Converter;
import com.xlot.admin.bean.UserBean;
import com.xlot.admin.id.PasswordUtils;
import com.xlot.admin.id.UserStatus;
import com.xlot.admin.model.RbacModel;
import com.xlot.admin.model.UserModel;
import com.xlot.admin.processor.AbstractAdminProcessor;
import com.xlot.admin.statics.Status;
import com.xlot.admin.statics.UserType;

import scala.Tuple2;

public class CreateAccountProcessor extends AbstractAdminProcessor {
	private RbacModel rbacModel;
	private UserModel userModel;

	@Override
	public void init(PuObjectRO params) {
		rbacModel = getModelFactory().getModel(RbacModel.class.getName());
		userModel = getModelFactory().getModel(UserModel.class.getName());
	}

	@Override
	protected RPCFuture<PuElement> _process(PuObjectRO params) {
		byte[] adminId = getRaw(params, ADMIN_ID);
		String username = params.getString("username");
		String password = params.getString("password");
		int type = params.getInteger("type", UserType.NORMAL);
		byte[] refId = getRaw(params, "refId");
		if (type == UserType.PRODUCT && refId == null) {
			return futureResponse(baseResponse(Status.MISS_PRODUCT));
		}
		if (type == UserType.CP && refId == null) {
			return futureResponse(baseResponse(Status.MISS_CP));
		}
		Status verifyPasswordRs = PasswordUtils.verifyPassword(password);
		if (verifyPasswordRs != Status.SUCCESS) {
			return futureResponse(baseResponse(verifyPasswordRs));
		}
		if (username.toLowerCase().contains(password.toLowerCase())
				|| password.toLowerCase().contains(username.toLowerCase())) {
			return futureResponse(baseResponse(Status.USERNAME_CONTAIN_PASSWORD));
		}

		PuArray pua = params.getPuArray("roleIds", new PuArrayList());
		List<byte[]> roleIds = new ArrayList<>(pua.size());
		for (PuValue r : pua) {
			roleIds.add(getRaw(r));
		}
		long now = System.currentTimeMillis();
		byte[] userId = Converter.uuidToBytes(UUID.randomUUID());
		Tuple2<String, String> tuple = PasswordUtils.getStoredPassword(password);
		UserBean user = new UserBean();
		user.setHashKey(tuple._1);
		user.setPassword(tuple._2);
		user.setSalt(tuple._1);
		user.setStatus(UserStatus.ACTIVE);
		user.setTimestamp(now);
		user.setUserId(userId);
		user.setUsername(username);
		user.setType(type);
		user.setRefId(refId);

		int c = userModel.insertIgnore(user);
		if (c < 1) {
			return futureResponse(baseResponse(Status.DUPLICATE_USER));
		}
		rbacModel.grantRole(adminId, userId, roleIds, now);
		return futureResponse(baseResponse(Status.SUCCESS));
	}

}
