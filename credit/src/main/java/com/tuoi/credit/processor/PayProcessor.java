package com.tuoi.credit.processor;

import com.ndn.common.processor.AbstractSyncProcessor;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.utils.UUIDUtils;
import com.tuoi.credit.CreditContext;
import com.tuoi.credit.bean.CreditBean;
import com.tuoi.credit.bean.CreditBean.Payment;
import com.tuoi.credit.model.CreditModel;
import com.tuoi.credit.req.PayReq;
import com.tuoi.credit.statics.PayType;
import com.tuoi.credit.statics.PaymentStatus;
import com.tuoi.credit.utils.ResponseUtils;

public class PayProcessor extends AbstractSyncProcessor<PayReq> {
	private CreditModel model;

	@Override
	public PuElement process(PayReq params) {
		CreditBean credit = model.detail(params.getId());
		if (credit == null) {
			return ResponseUtils.error(ResponseUtils.CREDIT_NOT_FOUND);
		}
		long moneyToPay = moneyToPay(credit, params.getType());
		if (params.getValue() < moneyToPay) {
			return ResponseUtils.error(ResponseUtils.NOT_ENOUGH_MONEY_TO_PAY);
		}
		long now = System.currentTimeMillis();
		Payment payment = new Payment();
		payment.setId(UUIDUtils.timebasedUUIDAsBytes());
		payment.setOriValue(credit.getValue());
		payment.setTimestamp(now);
		payment.setType(params.getType());
		payment.setValue(params.getValue());
		model.pay(params.getId(), payment);
		int creditStatus = params.getType() == PayType.INTEREST_ONLY ? PaymentStatus.PAID_INTEREST //
				: PaymentStatus.PAID_ALL;
		model.updateCreditStatus(params.getId(), creditStatus);
		return null;
	}

	private long moneyToPay(CreditBean credit, int type) {
		// TODO Auto-generated method stub
		return 0;
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
	public PayReq produceRequest(PuObjectRO params) {
		return null;
	}

}
