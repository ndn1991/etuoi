package com.xlot.admin.processor.impl;

import java.util.List;

import com.nhb.common.async.RPCFuture;
import com.nhb.common.data.PuArrayList;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.xlot.admin.bean.UserWithRoleBean;
import com.xlot.admin.model.UserRbacModel;
import com.xlot.admin.processor.AbstractAdminProcessor;
import com.xlot.admin.statics.Status;

public class FetchUserProcessor extends AbstractAdminProcessor {
	private UserRbacModel model;
	
	@Override
	public void init(PuObjectRO params) {
		model = getModelFactory().getModel(UserRbacModel.class.getName());
	}

	@Override
	protected RPCFuture<PuElement> _process(PuObjectRO params) {
		String username = params.getString("username", null);
		int from = params.getInteger("from", 0);
		int size = params.getInteger("size", 10);
		List<UserWithRoleBean> list = model.search(username, from, size);
		int total = model.searchCount(username);
		PuObject puo = new PuObject();
		puo.setInteger("total", total);
		PuArrayList pua = new PuArrayList();
		for (UserWithRoleBean u : list) {
			pua.addFrom(u.toPuObject());
		}
		puo.setPuArray("data", pua);
		PuObject rsp = baseResponse(Status.SUCCESS);
		rsp.setPuObject("data", puo);
		return futureResponse(rsp);
	}

}
