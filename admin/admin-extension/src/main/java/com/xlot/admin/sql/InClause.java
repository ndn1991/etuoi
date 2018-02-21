package com.xlot.admin.sql;

import java.util.ArrayList;
import java.util.List;

public class InClause implements Clause {
	private String field;
	private final List<Object> values = new ArrayList<>(100);

	public InClause field(String field) {
		this.field = field;
		return this;
	}

	public InClause value(Object... os) {
		for (Object o : os) {
			values.add(o);
		}
		return this;
	}

	@Override
	public String sqlString() {
		List<String> ss = new ArrayList<>(values.size());
		for (Object o : values) {
			ss.add(RepresentUtils.represent(o));
		}
		return String.format("%s IN (%s)", field, String.join(", ", ss));
	}
}
