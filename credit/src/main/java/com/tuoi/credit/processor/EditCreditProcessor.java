package com.tuoi.credit.processor;

import java.text.ParseException;
import java.util.Date;

import com.ndn.common.processor.AbstractSyncProcessor;
import com.ndn.common.utils.BytesUtils;
import com.ndn.common.utils.LocalDateFormat;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObjectRO;
import com.tuoi.credit.req.EditCreditReq;

public class EditCreditProcessor extends AbstractSyncProcessor<EditCreditReq> {

	@Override
	public PuElement process(EditCreditReq params) {
		return null;
	}

	@Override
	public void init(PuObjectRO params) {

	}

	@Override
	public void shutdown() {

	}

	@Override
	public EditCreditReq produceRequest(PuObjectRO params) {
		EditCreditReq req = new EditCreditReq();
		req.setId(BytesUtils.getRaw(params, "id"));
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

}
