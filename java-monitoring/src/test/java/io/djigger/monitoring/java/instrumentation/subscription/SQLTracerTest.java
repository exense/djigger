/*******************************************************************************
 * (C) Copyright 2016 Jérôme Comte and Dorian Cransac
 *  
 *  This file is part of djigger
 *  
 *  djigger is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  djigger is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *  
 *  You should have received a copy of the GNU Affero General Public License
 *  along with djigger.  If not, see <http://www.gnu.org/licenses/>.
 *
 *******************************************************************************/
package io.djigger.monitoring.java.instrumentation.subscription;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SQLTracerTest {
	
	Connection connection;
	
	public static void main(String[] args) throws InterruptedException {
		SQLTracerTest test = new SQLTracerTest();
		while(true) {
			test.testPreparedStatementExecuteQuery();
			Thread.sleep(1000);
		}
	}
	
	public SQLTracerTest() {
		super();
		try {
			connection = DriverManager.getConnection("jdbc:hsqldb:mem:mymemdb", "SA", "");
			connection.createStatement().execute("CREATE TABLE test (att1 VARCHAR(10))");
		} catch (SQLException e) {
		}
	}
	
	public long testPreparedStatementExecuteQuery() {
		try {
			Class.forName("org.hsqldb.jdbc.JDBCDriver");
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		
		PreparedStatement statement = null;
		try {
			statement = connection.prepareStatement("select * from test");
			//statement.execute();
			statement.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				statement.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return 1;
	}


}
