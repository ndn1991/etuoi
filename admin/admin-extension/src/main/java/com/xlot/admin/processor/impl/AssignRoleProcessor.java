package com.xlot.admin.processor.impl;

import java.util.ArrayList;
import java.util.List;

import com.nhb.common.async.RPCFuture;
import com.nhb.common.data.PuArray;
import com.nhb.common.data.PuArrayList;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.data.PuValue;
import com.xlot.admin.model.RbacModel;
import com.xlot.admin.processor.AbstractAdminProcessor;
import com.xlot.admin.statics.Status;

public class AssignRoleProcessor extends AbstractAdminProcessor {
	private RbacModel rbacModel;

	@Override
	public void init(PuObjectRO params) {
		rbacModel = getModelFactory().getModel(RbacModel.class.getName());
	}

	@Override
	protected RPCFuture<PuElement> _process(PuObjectRO params) {
		byte[] adminId = getRaw(params, ADMIN_ID);
		byte[] userId = getRaw(params, "userId");
		PuArray pua = params.getPuArray("roleIds", new PuArrayList());
		List<byte[]> roleIds = new ArrayList<>(pua.size());
		for (PuValue r : pua) {
			roleIds.add(getRaw(r));
		}
		long now = System.currentTimeMillis();
		rbacModel.removeRoleExclude(adminId, userId, roleIds);
		rbacModel.grantRole(adminId, userId, roleIds, now);
		return futureResponse(baseResponse(Status.SUCCESS));
	}

}
