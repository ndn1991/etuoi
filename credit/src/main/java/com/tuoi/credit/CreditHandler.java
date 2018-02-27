package com.tuoi.credit;

import com.ndn.common.Context;
import com.ndn.common.ObjectedHandler;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.db.models.ModelFactory;
import com.tuoi.credit.statics.F;

public class CreditHandler extends ObjectedHandler {

	@Override
	protected Context makeContext(PuObjectRO params) {
		ModelFactory modelFactory = new ModelFactory();
		modelFactory.setClassLoader(getClass().getClassLoader());
		modelFactory.setMongoClient(getApi().getMongoClient(params.getString("mongo")));
		CreditContext context = new CreditContext();
		context.setModelFactory(modelFactory);
		String database = params.getString("database");
		context.setDatabase(database);
		modelFactory.setEnvironmentVariable(F.DATABASE, database);
		return context;
	}

}
