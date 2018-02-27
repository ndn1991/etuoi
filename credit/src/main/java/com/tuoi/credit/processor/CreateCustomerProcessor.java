package com.tuoi.credit.processor;

import com.ndn.common.processor.AbstractSyncProcessor;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.utils.UUIDUtils;
import com.tuoi.credit.CreditContext;
import com.tuoi.credit.bean.CustomerBean;
import com.tuoi.credit.model.CustomerModel;
import com.tuoi.credit.req.CreateCustomerReq;
import com.tuoi.credit.utils.ResponseUtils;

public class CreateCustomerProcessor extends AbstractSyncProcessor<CreateCustomerReq> {

	private CustomerModel model;

	@Override
	public PuElement process(CreateCustomerReq req) {
		CustomerBean c = new CustomerBean();
		c.setId(UUIDUtils.timebasedUUIDAsBytes());
		c.setName(req.getName());
		c.setPhone(req.getPhone());
		c.setReferer(c.getReferer());
		c.setStatus(c.getStatus());
		int n = model.insert(c);
		if (n == 1) {
			return ResponseUtils.success();
		} else {
			return ResponseUtils.error(ResponseUtils.DUPLICATE_CUSTOMER);
		}
	}

	@Override
	public void init(PuObjectRO arg0) {
		CreditContext context = getContext();
		model = context.getModelFactory().getModel(CustomerModel.class.getName());
	}

	@Override
	public CreateCustomerReq produceRequest(PuObjectRO params) {
		CreateCustomerReq req = new CreateCustomerReq();
		req.setName(params.getString("name"));
		req.setPhone(params.getString("phone"));
		req.setReferer(params.getString("referer"));
		req.setStatus(params.getInteger("status"));
		return req;
	}

	@Override
	public void shutdown() {

	}

}
