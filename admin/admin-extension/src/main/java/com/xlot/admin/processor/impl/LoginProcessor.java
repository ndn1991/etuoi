package com.xlot.admin.processor.impl;

import java.security.GeneralSecurityException;
import java.util.List;

import com.nhb.common.async.RPCFuture;
import com.nhb.common.data.MapTuple;
import com.nhb.common.data.PuArray;
import com.nhb.common.data.PuArrayList;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.xlot.admin.bean.PermissionBean;
import com.xlot.admin.bean.UserBean;
import com.xlot.admin.id.JwtLoginUtils;
import com.xlot.admin.id.PasswordUtils;
import com.xlot.admin.model.RbacModel;
import com.xlot.admin.model.UserModel;
import com.xlot.admin.processor.AbstractAdminProcessor;
import com.xlot.admin.statics.Status;

public class LoginProcessor extends AbstractAdminProcessor {
	private UserModel userModel;
	private long timeout = 2592000000l;
	private RbacModel rbacModel;

	@Override
	public void init(PuObjectRO params) {
		userModel = getModelFactory().getModel(UserModel.class.getName());
		timeout = params.getLong("timeout", timeout);
		rbacModel = getModelFactory().getModel(RbacModel.class.getName());
	}

	@Override
	protected RPCFuture<PuElement> _process(PuObjectRO params) {
		String username = params.getString("username");
		String password = params.getString("password");

		UserBean user = userModel.loadByName(username);
		if (user == null) {
			return futureResponse(baseResponse(Status.USER_NOT_FOUND));
		}
		boolean b = PasswordUtils.isPasswordValid(user.getSalt(), password, user.getPassword());
		if (!b) {
			return futureResponse(baseResponse(Status.WRONG_PASSWORD));
		}
		try {
			String token = JwtLoginUtils.token(user.getUserId(), username, user.getHashKey(), timeout);
			List<PermissionBean> pers = rbacModel.permissionsOfUser(user.getUserId());
			PuObject rs = baseResponse(Status.SUCCESS);
			rs.set("data", PuObject.fromObject(new MapTuple<>("token", token, "permissions", pers(pers), "type", user.getType())));
			return futureResponse(rs);
		} catch (GeneralSecurityException e) {
			getLogger().error("error", e);
			return futureResponse(baseResponse(Status.UNKNOWN_ERROR));
		}
	}

	private PuArray pers(List<PermissionBean> pers) {
		PuArrayList pua = new PuArrayList();
		for (PermissionBean per : pers) {
			pua.addFrom(per.getName());
		}
		return pua;
	}
}
