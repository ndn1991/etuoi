package com.xlot.admin.processor.impl;

import java.util.ArrayList;
import java.util.List;

import com.nhb.common.async.RPCFuture;
import com.nhb.common.data.PuArray;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.data.PuValue;
import com.xlot.admin.model.RbacModel;
import com.xlot.admin.processor.AbstractAdminProcessor;
import com.xlot.admin.statics.Status;

public class AssignPermissionProcessor extends AbstractAdminProcessor {
	private RbacModel rbacModel;

	@Override
	public void init(PuObjectRO params) {
		rbacModel = getModelFactory().getModel(RbacModel.class.getName());
	}

	@Override
	protected RPCFuture<PuElement> _process(PuObjectRO params) {
		byte[] roleId = getRaw(params, "roleId");
		PuArray pua = params.getPuArray("permissionIds");
		List<byte[]> permissionIds = new ArrayList<>(pua.size());
		for (PuValue p : pua) {
			permissionIds.add(getRaw(p));
		}
		rbacModel.removePermissionsExclude(roleId, permissionIds);
		long now = System.currentTimeMillis();
		rbacModel.addRolePermissions(roleId, permissionIds, now);
		return futureResponse(baseResponse(Status.SUCCESS));
	}

}
