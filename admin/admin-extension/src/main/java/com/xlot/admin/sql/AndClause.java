package com.xlot.admin.sql;

import java.util.ArrayList;
import java.util.List;

public class AndClause implements Clause {
	private final List<Clause> clauses = new ArrayList<>(100);

	public AndClause clause(Clause... clauses) {
		for (Clause clause : clauses) {
			this.clauses.add(clause);
		}
		return this;
	}

	@Override
	public String sqlString() {
		if (clauses.isEmpty()) {
			return "TRUE";
		}
		List<String> ss = new ArrayList<>(clauses.size());
		for (Clause c : clauses) {
			ss.add(c.sqlString());
		}
		return String.format("(%s)", String.join(" AND ", ss));
	}

}
