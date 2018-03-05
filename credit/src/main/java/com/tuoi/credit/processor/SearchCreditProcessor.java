package com.tuoi.credit.processor;

import java.util.List;

import com.ndn.common.processor.AbstractSyncProcessor;
import com.nhb.common.data.PuArrayList;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.tuoi.credit.CreditContext;
import com.tuoi.credit.bean.CreditBean;
import com.tuoi.credit.model.CreditModel;
import com.tuoi.credit.req.SearchCreditReq;
import com.tuoi.credit.utils.ResponseUtils;

public class SearchCreditProcessor extends AbstractSyncProcessor<SearchCreditReq> {
	private CreditModel model;

	@Override
	public PuElement process(SearchCreditReq params) {
		List<CreditBean> cs = model.search(params.getContent(), params.getStatus(), params.getFrom(), params.getSize());
		int total = model.count(params.getContent(), params.getStatus());
		PuObject _data = new PuObject();
		PuArrayList data = new PuArrayList();
		for (CreditBean c : cs) {
			data.addFrom(PuObject.fromObject(c));
		}
		_data.set("data", data);
		_data.set("total", total);
		PuObject rs = ResponseUtils.success();
		rs.set("data", data);
		return rs;
	}

	@Override
	public void init(PuObjectRO params) {
		CreditContext context = getContext();
		model = context.getModelFactory().getModel(CreditModel.class.getName());
	}

	@Override
	public void shutdown() {
		
	}

	@Override
	public SearchCreditReq produceRequest(PuObjectRO params) {
		SearchCreditReq req = new SearchCreditReq();
		req.setContent(params.getString("content"));
		req.setStatus(params.getInteger("status"));
		req.setFrom(params.getInteger("from"));
		req.setSize(params.getInteger("size"));
		return req;
	}

}
