package com.unlucky4ever.killcount.extras.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public class MySQL extends Database {
	
	private String host = "localhost";
	private String port = "3306";
	private String user = "minecraft";
	private String pass = "";
	private String database = "minecraft";
	
	public MySQL(Logger log, String prefix, String host, String port, String database, String user, String pass) {
		super(log, prefix, "[MySQL] ");
		this.host = host;
		this.port = port;
		this.database = database;
		this.user = user;
		this.pass = pass;
	}
	
	protected boolean initialize() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			return true;
		} catch (ClassNotFoundException e) {
			writeError("Class Not Found Exception: " + e.getMessage() + ".", true);
		}
		
		return false;
	}
	
	public Connection open() {
		if (initialize()) {
			String url = "";
			try {
				url = "jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database;
				this.connection = DriverManager.getConnection(url, this.user, this.pass);
			} catch (SQLException e) {
				writeError(url, true);
				writeError("Could not be resolved because of a SQL Exception: " + e.getMessage() + ".", true);
			}
		}
		
		return null;
	}
	
	public void close() {
		try {
			if (this.connection != null) {
				this.connection.close();
			}
		} catch (Exception e) {
			writeError("Failed to close database connection: " + e.getMessage() + ".", true);
		}
	}
	
	public Connection getConnection() {
		return this.connection;
	}
	
	public boolean checkConnection() {
		if (this.connection != null) {
			return true;
		}
		
		return false;
	}
	
	public ResultSet query(String query) {
		Statement statement = null;
		ResultSet result = null;
		
		try {
			statement = this.connection.createStatement();
			result = statement.executeQuery("SELECT CURTIME()");
			
			switch(getStatement(query)) {
			case ALTER:
				result = statement.executeQuery(query);
				break;
			default:
				statement.executeUpdate(query);
			}
			
			return result;
		} catch (SQLException e) {
			writeError("Error in SQL query: " + e.getMessage(), true);
		}
		
		return result;
	}
	
	public PreparedStatement prepare(String query) {
		PreparedStatement ps = null;
		
		try {
			return this.connection.prepareStatement(query);
		} catch (SQLException e) {
			if (!e.toString().contains("not return ResultSet")) {
				writeError("Error in SQL prepare() query: " + e.getMessage(), false);
			}
		}
		
		return ps;
	}
	
	public boolean createTable(String query) {
		Statement statement = null;
		
		try {
			if ((query.equals("")) || (query == null)) {
				writeError("SQL query empty: createTable(" + query + ")", true);
				return false;
			}
			
			statement = this.connection.createStatement();
			statement.execute(query);
			return true;
		} catch (SQLException e) {
			writeError(e.getMessage(), true);
			return false;
		} catch (Exception e) {
			writeError(e.getMessage(), true);
		}
		
		return false;
	}
	
	public boolean checkTable(String table) {
		try {
			Statement statement = this.connection.createStatement();
			ResultSet result = statement.executeQuery("SELECT * FROM " + table);
			
			if (result == null) {
				return false;
			}
			
			if (result != null) {
				return true;
			}
		} catch (SQLException e) {
			if (e.getMessage().contains("exist")) {
				return false;
			}
			
			writeError("Error in SQL query: " + e.getMessage(), true);
			
			if (query("SELECT * FROM " + table) == null) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean wipeTable(String table) {
		Statement statement = null;
		String query = null;
		
		try {
			if (!checkTable(table)) {
				writeError("Error wiping table: \"" + table + "\" does not exist.", true);
				return false;
			}
			
			statement = this.connection.createStatement();
			query = "DELETE FROM " + table + ";";
			statement.executeUpdate(query);
			
			return true;
		} catch (SQLException e) {
			if (!e.toString().contains("not return ResultSet")) {
				return false;
			}
		}
		
		return false;
	}
}