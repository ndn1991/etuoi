package com.tuoi.credit.processor;

import com.ndn.common.processor.AbstractSyncProcessor;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObjectRO;
import com.tuoi.credit.req.PayReq;

public class PayProcessor extends AbstractSyncProcessor<PayReq> {

	@Override
	public PuElement process(PayReq params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init(PuObjectRO params) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public PayReq produceRequest(PuObjectRO params) {
		// TODO Auto-generated method stub
		return null;
	}

}
