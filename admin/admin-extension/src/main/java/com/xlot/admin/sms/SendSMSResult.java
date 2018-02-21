package com.xlot.admin.sms;

import java.util.ArrayList;
import java.util.List;

import com.nhb.common.data.PuArray;
import com.nhb.common.data.PuArrayList;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuValue;

import lombok.Data;

@Data
public class SendSMSResult {
	private int transactionId;
	private int totalSMS;
	private long totalPrice;

	private int status;
	private String message;

	private final List<String> invalidPhone = new ArrayList<>(100);

	public SendSMSResult fromPuObject(PuObject puo) {
		transactionId = puo.getInteger("transactionId", 0);
		totalSMS = puo.getInteger("totalSMS", 0);
		totalPrice = puo.getLong("totalPrice", 0);
		status = puo.getInteger("status");
		message = puo.getString("message", null);
		PuArray pua = puo.getPuArray("invalidPhone", new PuArrayList());
		for (PuValue e : pua) {
			invalidPhone.add(e.getString());
		}
		return this;
	}

	public PuObject toPuObject() {
		PuObject puo = new PuObject();
		puo.setInteger("transactionId", transactionId);
		puo.setInteger("totalSMS", totalSMS);
		puo.setLong("totalPrice", totalPrice);
		puo.setInteger("status", status);
		puo.setPuArray("invalidPhone", PuArrayList.fromObject(invalidPhone));
		puo.setString("message", message);
		return puo;
	}
}
