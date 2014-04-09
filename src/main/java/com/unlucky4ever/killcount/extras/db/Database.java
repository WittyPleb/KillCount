package com.unlucky4ever.killcount.extras.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;

public abstract class Database {
	
	protected Logger log;
	protected final String PREFIX;
	protected final String DATABASE_PREFIX;
	protected boolean connected;
	protected Connection connection;
	
	protected static enum Statements {
		SELECT, INSERT, UPDATE, DELETE, DO, REPLACE, LOAD, HANDLER, CALL, CREATE, ALTER, DROP, TRUNCATE, RENAME;
	}
	
	public Database(Logger log, String prefix, String dp) {
		this.log = log;
		this.PREFIX = prefix;
		this.DATABASE_PREFIX = dp;
		this.connected = false;
		this.connection = null;
	}
	
	protected void writeInfo(String toWrite) {
		if (toWrite != null) {
			this.log.info(this.PREFIX + this.DATABASE_PREFIX + toWrite);
		}
	}
	
	protected void writeError(String toWrite, boolean severe) {
		if (toWrite != null) {
			if (severe) {
				this.log.severe(this.PREFIX + this.DATABASE_PREFIX + toWrite);
			} else {
				this.log.warning(this.PREFIX + this.DATABASE_PREFIX + toWrite);
			}
		}
	}
	
	abstract boolean initialize();
	abstract Connection open();
	abstract void close();
	abstract Connection getConnection();
	abstract boolean checkConnection();
	abstract ResultSet query(String paramString);
	abstract PreparedStatement prepare(String paramString);
	
	protected Statements getStatement(String query) {
		String trimmedQuery = query.trim();
		
		if (trimmedQuery.substring(0, 6).equalsIgnoreCase("SELECT")) {
			return Statements.SELECT;
		}
		
		if (trimmedQuery.substring(0, 6).equalsIgnoreCase("INSERT")) {
			return Statements.INSERT;
		}
		
		if (trimmedQuery.substring(0, 6).equalsIgnoreCase("UPDATE")) {
			return Statements.UPDATE;
		}
		
		if (trimmedQuery.substring(0, 6).equalsIgnoreCase("DELETE")) {
			return Statements.DELETE;
		}
		
		if (trimmedQuery.substring(0, 6).equalsIgnoreCase("CREATE")) {
			return Statements.CREATE;
		}
		
		if (trimmedQuery.substring(0, 6).equalsIgnoreCase("ALTER")) {
			return Statements.ALTER;
		}
		
		if (trimmedQuery.substring(0, 6).equalsIgnoreCase("DROP")) {
			return Statements.DROP;
		}
		
		if (trimmedQuery.substring(0, 6).equalsIgnoreCase("TRUNCATE")) {
			return Statements.TRUNCATE;
		}
		
		if (trimmedQuery.substring(0, 6).equalsIgnoreCase("RENA,E")) {
			return Statements.RENAME;
		}
		
		if (trimmedQuery.substring(0, 6).equalsIgnoreCase("DO")) {
			return Statements.DO;
		}
		
		if (trimmedQuery.substring(0, 6).equalsIgnoreCase("REPLACE")) {
			return Statements.REPLACE;
		}
		
		if (trimmedQuery.substring(0, 6).equalsIgnoreCase("LOAD")) {
			return Statements.LOAD;
		}
		
		if (trimmedQuery.substring(0, 6).equalsIgnoreCase("HANDLER")) {
			return Statements.HANDLER;
		}
		
		if (trimmedQuery.substring(0, 6).equalsIgnoreCase("CALL")) {
			return Statements.CALL;
		}
		
		return Statements.SELECT;
	}
	
	abstract boolean createTable(String paramString);
	abstract boolean checkTable(String paramString);
	abstract boolean wipeTable(String paramString);
}