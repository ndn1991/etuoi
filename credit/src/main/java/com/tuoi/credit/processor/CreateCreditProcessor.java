package com.tuoi.credit.processor;

import java.text.ParseException;
import java.util.Date;

import com.ndn.common.processor.AbstractSyncProcessor;
import com.ndn.common.utils.BytesUtils;
import com.ndn.common.utils.LocalDateFormat;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.utils.UUIDUtils;
import com.tuoi.credit.CreditContext;
import com.tuoi.credit.bean.CreditBean;
import com.tuoi.credit.bean.CustomerBean;
import com.tuoi.credit.model.CreditModel;
import com.tuoi.credit.model.CustomerModel;
import com.tuoi.credit.req.CreateCreditReq;
import com.tuoi.credit.utils.ResponseUtils;

public class CreateCreditProcessor extends AbstractSyncProcessor<CreateCreditReq> {
	private CreditModel creditModel;
	private CustomerModel customerModel;

	@Override
	public PuElement process(CreateCreditReq req) {
		CustomerBean customer = customerModel.fetch(req.getCustomerId());
		if (customer == null) {
			return ResponseUtils.error(ResponseUtils.CUSTOMER_NOT_FOUND);
		}
		CreditBean credit = new CreditBean();
		credit.setId(UUIDUtils.timebasedUUIDAsBytes());
		credit.setCreatedTime(System.currentTimeMillis());
		credit.setCreditedTime(req.getCreditedTime());
		credit.setCustomerId(req.getCustomerId());
		credit.setValue(req.getValue());
		credit.setRate(req.getRate());
		credit.setStatus(req.getStatus());
		credit.setCustomerName(customer.getName());
		credit.setReferer(customer.getReferer());
		credit.setPhone(customer.getPhone());
		creditModel.insert(credit);
		return ResponseUtils.success();
	}

	@Override
	public void init(PuObjectRO params) {
		CreditContext context = getContext();
		creditModel = context.getModelFactory().getModel(CreditModel.class.getName());
		customerModel = context.getModelFactory().getModel(CustomerModel.class.getName());
	}

	@Override
	public CreateCreditReq produceRequest(PuObjectRO params) {
		CreateCreditReq req = new CreateCreditReq();
		req.setCustomerId(BytesUtils.getRaw(params, "customerId"));
		req.setRate(params.getDouble("rate"));
		req.setValue(params.getLong("value"));
		req.setStatus(params.getInteger("status"));
		String dateTime = params.getString("dateTime");
		try {
			Date date = LocalDateFormat.DATE_FOMATER.getFormat().parse(dateTime);
			req.setCreditedTime(date.getTime());
		} catch (ParseException e) {
			throw new RuntimeException("wrong date time format", e);
		}
		return req;
	}

	@Override
	public void shutdown() {
		
	}

}
