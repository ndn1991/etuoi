package com.xlot.admin.processor.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.nhb.common.async.RPCFuture;
import com.nhb.common.data.PuArray;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.data.PuValue;
import com.nhb.common.utils.UUIDUtils;
import com.xlot.admin.model.RbacModel;
import com.xlot.admin.processor.AbstractAdminProcessor;
import com.xlot.admin.statics.Status;

public class CreateRoleProcessor extends AbstractAdminProcessor {
	private RbacModel rbacModel;

	@Override
	public void init(PuObjectRO params) {
		rbacModel = getModelFactory().getModel(RbacModel.class.getName());
	}

	@Override
	protected RPCFuture<PuElement> _process(PuObjectRO params) {
		String name = params.getString("name");
		String description = params.getString("description", null);
		PuArray pua = params.getPuArray("permissionIds");
		List<byte[]> permissionIds = new ArrayList<>(pua.size());
		for (PuValue p : pua) {
			permissionIds.add(getRaw(p));
		}
		byte[] id = UUIDUtils.uuidToBytes(UUID.randomUUID());
		long now = System.currentTimeMillis();
		int c = rbacModel.insertRole(id, name, description, now);
		if (c < 1) {
			return futureResponse(baseResponse(Status.DUPLICATE_ROLE));
		}
		rbacModel.addRolePermissions(id, permissionIds, now);
		return futureResponse(baseResponse(Status.SUCCESS));
	}

}
