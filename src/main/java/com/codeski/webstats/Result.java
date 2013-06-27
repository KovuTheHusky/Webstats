package com.codeski.webstats;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Result {
	private final ResultSet result;
	private final Statement statement;

	public Result(Statement statement, ResultSet result) {
		this.statement = statement;
		this.result = result;
	}

	public void close() {
		try {
			this.result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			this.statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public ResultSet getResult() {
		return this.result;
	}

	public Statement getStatement() {
		return this.statement;
	}
}
