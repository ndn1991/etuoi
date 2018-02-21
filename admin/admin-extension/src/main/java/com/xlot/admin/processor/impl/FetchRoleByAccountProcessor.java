package com.xlot.admin.processor.impl;

import java.util.List;

import com.nhb.common.async.RPCFuture;
import com.nhb.common.data.PuArray;
import com.nhb.common.data.PuArrayList;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.xlot.admin.bean.RoleBean;
import com.xlot.admin.model.RbacModel;
import com.xlot.admin.processor.AbstractAdminProcessor;
import com.xlot.admin.statics.Status;

public class FetchRoleByAccountProcessor extends AbstractAdminProcessor {
	private RbacModel rbacModel;

	@Override
	public void init(PuObjectRO params) {
		rbacModel = getModelFactory().getModel(RbacModel.class.getName());
	}

	@Override
	protected RPCFuture<PuElement> _process(PuObjectRO params) {
		byte[] accountId = getRaw(params, "accountId");
		List<RoleBean> permissions = rbacModel.getRoles(accountId);
		PuObject rs = baseResponse(Status.SUCCESS);
		PuArray pua = new PuArrayList();
		for (RoleBean p : permissions) {
			pua.addFrom(p.toObject());
		}
		rs.setPuArray("data", pua);
		return futureResponse(rs);
	}

}
