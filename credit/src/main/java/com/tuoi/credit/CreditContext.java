package com.tuoi.credit;

import com.ndn.common.Context;
import com.nhb.common.db.models.ModelFactory;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreditContext implements Context {
	private ModelFactory modelFactory;
	private String database;
}
