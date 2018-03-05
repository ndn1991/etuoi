package com.tuoi.credit.processor;

import java.util.List;

import com.ndn.common.processor.AbstractSyncProcessor;
import com.nhb.common.data.PuArrayList;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.tuoi.credit.CreditContext;
import com.tuoi.credit.bean.CustomerBean;
import com.tuoi.credit.model.CustomerModel;
import com.tuoi.credit.req.SearchCustomerReq;
import com.tuoi.credit.utils.ResponseUtils;

public class SearchCustomerProcessor extends AbstractSyncProcessor<SearchCustomerReq> {
	private CustomerModel model;

	@Override
	public PuElement process(SearchCustomerReq params) {
		List<CustomerBean> customers = model.search(params.getContent());
		int count = model.count(params.getContent(), params.getSkip(), params.getLimit());
		PuObject _data = new PuObject();
		PuArrayList data = new PuArrayList();
		for (CustomerBean c : customers) {
			data.addFrom(PuObject.fromObject(c));
		}
		_data.set("data", data);
		_data.set("total", count);
		PuObject rs = ResponseUtils.success();
		rs.set("data", data);
		return rs;
	}

	@Override
	public void init(PuObjectRO params) {
		CreditContext context = getContext();
		model = context.getModelFactory().getModel(CustomerModel.class.getName());
	}

	@Override
	public void shutdown() {

	}

	@Override
	public SearchCustomerReq produceRequest(PuObjectRO params) {
		SearchCustomerReq req = new SearchCustomerReq();
		req.setContent(params.getString("content"));
		req.setLimit(params.getInteger("size"));
		req.setSkip(params.getInteger("from"));
		return req;
	}

}
