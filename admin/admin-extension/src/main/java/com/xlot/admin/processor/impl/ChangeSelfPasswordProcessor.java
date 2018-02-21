package com.xlot.admin.processor.impl;

import java.security.GeneralSecurityException;

import com.nhb.common.async.RPCFuture;
import com.nhb.common.data.MapTuple;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.xlot.admin.bean.UserBean;
import com.xlot.admin.id.JwtLoginUtils;
import com.xlot.admin.id.PasswordUtils;
import com.xlot.admin.model.UserModel;
import com.xlot.admin.processor.AbstractAdminProcessor;
import com.xlot.admin.statics.Status;

import scala.Tuple2;

public class ChangeSelfPasswordProcessor extends AbstractAdminProcessor {
	private UserModel userModel;
	private long timeout = 2592000000l;

	@Override
	public void init(PuObjectRO params) {
		userModel = getModelFactory().getModel(UserModel.class.getName());
		timeout = params.getLong("timeout", timeout);
	}

	@Override
	protected RPCFuture<PuElement> _process(PuObjectRO params) {
		String oldPassword = params.getString("oldPassword");
		String password = params.getString("password");
		byte[] userId = params.getRaw(ADMIN_ID);

		UserBean user = userModel.loadById(userId);
		if (user == null) {
			return futureResponse(baseResponse(Status.USER_NOT_FOUND));
		}

		if (PasswordUtils.isPasswordValid(user.getSalt(), password, user.getPassword())) {
			return futureResponse(baseResponse(Status.DUPLICATE_PASSWORD));
		}

		if (!PasswordUtils.isPasswordValid(user.getSalt(), oldPassword, user.getPassword())) {
			return futureResponse(baseResponse(Status.WRONG_PASSWORD));
		}

		Status verifyPasswordRs = PasswordUtils.verifyPassword(password);
		if (verifyPasswordRs != Status.SUCCESS) {
			return futureResponse(baseResponse(verifyPasswordRs));
		}
		String lUsername = user.getUsername().toLowerCase();
		if (lUsername.contains(password.toLowerCase()) || password.toLowerCase().contains(lUsername)) {
			return futureResponse(baseResponse(Status.USERNAME_CONTAIN_PASSWORD));
		}

		Tuple2<String, String> tuple = PasswordUtils.getStoredPassword(password);
		int c = userModel.updatePassword(userId, tuple._1, tuple._1, tuple._2, System.currentTimeMillis());
		if (c < 1) {
			return futureResponse(baseResponse(Status.UNKNOWN_ERROR));
		}
		getUserCache().invalidate(userId);
		try {
			String token = JwtLoginUtils.token(user.getUserId(), user.getUsername(), tuple._1, timeout);
			PuObject baseResponse = baseResponse(Status.SUCCESS);
			baseResponse.setPuObject("data", PuObject.fromObject(new MapTuple<>("token", token)));
			return futureResponse(baseResponse);
		} catch (GeneralSecurityException e) {
			getLogger().error("error", e);
			return futureResponse(baseResponse(Status.UNKNOWN_ERROR));
		}
	}

}
