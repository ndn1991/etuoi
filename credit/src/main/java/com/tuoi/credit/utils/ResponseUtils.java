package com.tuoi.credit.utils;

import com.nhb.common.data.PuObject;

public interface ResponseUtils {
	String DUPLICATE_CUSTOMER = "khách hàng bị trùng";
	String CUSTOMER_NOT_FOUND = "không tìm thấy khách hàng";
	String CREDIT_NOT_FOUND = "không tìm thấy đơn nợ";
	String NOT_ENOUGH_MONEY_TO_PAY = "không đủ tiền trả";

	static PuObject res(int status, String message) {
		PuObject puo = new PuObject();
		puo.set("status", status);
		puo.set("message", message);
		return puo;
	}

	static PuObject error(String message) {
		return res(-1, message);
	}

	static PuObject success() {
		return res(0, null);
	}
}
