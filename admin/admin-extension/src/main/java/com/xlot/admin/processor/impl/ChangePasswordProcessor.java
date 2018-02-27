package com.xlot.admin.processor.impl;

import com.nhb.common.async.RPCFuture;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObjectRO;
import com.xlot.admin.bean.UserBean;
import com.xlot.admin.id.PasswordUtils;
import com.xlot.admin.model.UserModel;
import com.xlot.admin.processor.AbstractAdminProcessor;
import com.xlot.admin.statics.Status;

import scala.Tuple2;

public class ChangePasswordProcessor extends AbstractAdminProcessor {
	private UserModel userModel;

	@Override
	public void init(PuObjectRO params) {
		userModel = getModelFactory().getModel(UserModel.class.getName());
	}

	@Override
	protected RPCFuture<PuElement> _process(PuObjectRO params) {
		byte[] userId = getRaw(params, "userId");
		String password = params.getString("password");
		
		UserBean user = userModel.loadById(userId);
		if (user == null) {
			return futureResponse(baseResponse(Status.USER_NOT_FOUND));
		}
		if (PasswordUtils.isPasswordValid(user.getSalt(), password, user.getPassword())) {
			return futureResponse(baseResponse(Status.DUPLICATE_PASSWORD));
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
		return futureResponse(baseResponse(Status.SUCCESS));
	}

}
