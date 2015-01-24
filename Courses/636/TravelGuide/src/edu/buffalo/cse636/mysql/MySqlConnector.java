package edu.buffalo.cse636.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MySqlConnector {

	private Connection connect = null;
	private Statement statement = null;
	private PreparedStatement preparedStatement = null;
	public ResultSet resultSet = null;
	private String userName = "root";
	private String password = "finalfantasy";
	
	public ResultSet readDataBase() throws Exception {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			connect = DriverManager.getConnection("jdbc:mysql://localhost/GoogleMaps?"
							+"user="+userName+"&password="+password);
			statement = connect.createStatement();
			resultSet = statement.executeQuery("select * from GoogleMaps.location");
			return resultSet;
		}
		catch (Exception e) {
			throw e;
		}
	}
	
	public void writeResultSet(ResultSet resultSet) throws SQLException {
		while (resultSet.next()) {
			
		}
	}
	
	public void close() {
		try {
			if (resultSet != null)
				resultSet.close();
			if (statement != null)
				statement.close();
			if (connect != null)
				connect.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
