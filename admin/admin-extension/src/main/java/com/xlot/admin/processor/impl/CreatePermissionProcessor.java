package com.xlot.admin.processor.impl;

import java.util.UUID;

import com.nhb.common.async.RPCFuture;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.utils.UUIDUtils;
import com.xlot.admin.model.RbacModel;
import com.xlot.admin.processor.AbstractAdminProcessor;
import com.xlot.admin.statics.Status;

public class CreatePermissionProcessor extends AbstractAdminProcessor {
	private RbacModel rbacModel;
	
	@Override
	public void init(PuObjectRO params) {
		rbacModel = getModelFactory().getModel(RbacModel.class.getName());
	}

	@Override
	protected RPCFuture<PuElement> _process(PuObjectRO params) {
		String name = params.getString("name");
		String description = params.getString("description", null);
		byte[] id = UUIDUtils.uuidToBytes(UUID.randomUUID());
		int c = rbacModel.insertPermission(id, name, description);
		if (c < 1) {
			return futureResponse(baseResponse(Status.DUPLICATE_PERMISSION));
		}
		return futureResponse(baseResponse(Status.SUCCESS));
	}

}
