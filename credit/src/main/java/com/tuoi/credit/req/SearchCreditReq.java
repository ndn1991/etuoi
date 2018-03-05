package com.tuoi.credit.req;

import com.ndn.common.request.Request;

import lombok.Data;

@Data
public class SearchCreditReq implements Request {
	private String content;
	private int status;
	private int from;
	private int size;
}
