package com.tuoi.credit.req;

import com.ndn.common.request.Request;

import lombok.Data;

@Data
public class SearchCustomerReq implements Request {
	private String content;
	private int skip;
	private int limit;
}
