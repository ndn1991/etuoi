package com.xlot.admin.processor.impl;

import java.util.ArrayList;
import java.util.List;

import com.gaia.acs.api.GaiaAcsRPCFuture;
import com.gaia.acs.api.message.ACSMessage;
import com.gaia.acs.api.message.request.cp.FetchAllCpOfProductsRequest;
import com.gaia.acs.api.message.response.cp.FetchAllCpOfProductsResponse;
import com.gaia.acs.api.statics.AcsResponseStatus;
import com.gaia.acs.api.vo.ContentProvider;
import com.nhb.common.async.RPCFuture;
import com.nhb.common.async.translator.RPCFutureTranslator;
import com.nhb.common.data.PuArrayList;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.xlot.admin.bean.UserBean;
import com.xlot.admin.bean.UserWithRoleBean;
import com.xlot.admin.model.UserModel;
import com.xlot.admin.model.UserRbacModel;
import com.xlot.admin.processor.AbstractAdminProcessor;
import com.xlot.admin.statics.Status;
import com.xlot.admin.statics.UserType;

public class FetchUserOfProductProcessor extends AbstractAdminProcessor {
	private UserRbacModel model;
	private UserModel userModel;

	@Override
	public void init(PuObjectRO params) {
		model = getModelFactory().getModel(UserRbacModel.class.getName());
		userModel = getModelFactory().getModel(UserModel.class.getName());
	}

	@Override
	protected RPCFuture<PuElement> _process(PuObjectRO params) {
		byte[] adminId = getRaw(params, ADMIN_ID);
		UserBean admin = userModel.loadById(adminId);
		if (admin.getType() != UserType.PRODUCT) {
			getLogger().error("not a product account");
			return futureResponse(baseResponse(Status.UNKNOWN_ERROR));
		}
		FetchAllCpOfProductsRequest fetchCpsReq = new FetchAllCpOfProductsRequest();
		fetchCpsReq.getProductIds().add(admin.getRefId());
		GaiaAcsRPCFuture future = getAcsClient().send(fetchCpsReq);
		return new RPCFutureTranslator<ACSMessage, PuElement>(future) {
			@Override
			protected PuElement translate(ACSMessage result) throws Exception {
				if (result == null) {
					return null;
				}
				FetchAllCpOfProductsResponse response = (FetchAllCpOfProductsResponse) result;
				if (response.getStatus() != AcsResponseStatus.OK) {
					throw new Exception("acs status: " + response.getStatus());
				}
				List<byte[]> refIds = new ArrayList<>(response.getCps().size() + 1);
				refIds.add(admin.getRefId());
				for (ContentProvider cp : response.getCps()) {
					refIds.add(cp.getId());
				}
				String username = params.getString("username", null);
				int from = params.getInteger("from", 0);
				int size = params.getInteger("size", 10);
				List<UserWithRoleBean> list = model.searchOfProduct(refIds, username, from, size);
				int total = model.searchOfProductCount(refIds, username);
				PuObject puo = new PuObject();
				puo.setInteger("total", total);
				PuArrayList pua = new PuArrayList();
				for (UserWithRoleBean u : list) {
					pua.addFrom(u.toPuObject());
				}
				puo.setPuArray("data", pua);
				PuObject rsp = baseResponse(Status.SUCCESS);
				rsp.setPuObject("data", puo);
				return rsp;
			}
		};
	}

}
