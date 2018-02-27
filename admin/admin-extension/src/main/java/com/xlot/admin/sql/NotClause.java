package com.xlot.admin.sql;

import lombok.Data;

@Data
public class NotClause implements Clause {
	private final Clause clause;

	@Override
	public String sqlString() {
		return String.format("(NOT %s)", clause.sqlString());
	}
}
