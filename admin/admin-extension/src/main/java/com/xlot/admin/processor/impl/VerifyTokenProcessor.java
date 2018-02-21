package com.xlot.admin.processor.impl;

import com.nhb.common.async.RPCFuture;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.xlot.admin.id.JWTUtils;
import com.xlot.admin.id.JwtLoginData;
import com.xlot.admin.processor.AbstractAdminProcessor;
import com.xlot.admin.statics.Status;

public class VerifyTokenProcessor extends AbstractAdminProcessor {
	@Override
	protected RPCFuture<PuElement> _process(PuObjectRO params) {
		String auth = params.getString("auth");
		PuObject object = (PuObject) JWTUtils.get(auth);
		JwtLoginData jwtLoginData = JwtLoginData.fromPuObject(object);
		byte[] userId = jwtLoginData.getUserId();
		String hashKey = getUserCache().getHashKey(userId);
		if (hashKey == null) {
			return futureResponse(baseResponse(Status.INVALID_TOKEN));
		}
		if (JWTUtils.verifyAndGet(auth, hashKey) == null) {
			return futureResponse(baseResponse(Status.INVALID_TOKEN));
		}

		return futureResponse(baseResponse(Status.SUCCESS));
	}

}
