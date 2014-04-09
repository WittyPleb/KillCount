package com.unlucky4ever.killcount;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.unlucky4ever.killcount.extras.db.MySQL;
import com.unlucky4ever.killcount.listeners.PlayerListener;

public class KillCount extends JavaPlugin {
	
	public MySQL mysql;
	public Logger log;
	public String host;
	public String port;
	public String db;
	public String user;
	public String pass;
	public FileConfiguration users = null;
	public File usersFile = null;
	
	public void onEnable() {
		getConfig().addDefault("debug", false);
		getConfig().addDefault("broadcast-kills", true);
		getConfig().addDefault("storage-type", "file");
		getConfig().addDefault("mysql.host", "localhost");
		getConfig().addDefault("mysql.port", "3306");
		getConfig().addDefault("mysql.database", "minecraft");
		getConfig().addDefault("mysql.username", "root");
		getConfig().addDefault("mysql.password", "password");
		
		getConfig().options().copyDefaults(true);
		
		if (getConfig().getString("storage-type").equalsIgnoreCase("file")) {
			getCustomConfig();
			saveCustomConfig();
		}
		
		saveConfig();
		
		getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
	}
	
	public void onDisable() {
		if (this.mysql.checkConnection() == true) {
			this.mysql.close();
			this.mysql = null;
		} else {
			this.mysql = null;
		}
	}
	
	public FileConfiguration getCustomConfig() {
		if (users == null) {
			reloadCustomConfig();
		}
		
		return users;
	}
	
	public void reloadCustomConfig() {
		if (usersFile == null) {
			usersFile = new File(getDataFolder(), "users.yml");
		}
		
		users = YamlConfiguration.loadConfiguration(usersFile);
	}
	
	public void saveCustomConfig() {
		if ((users == null) || (usersFile == null)) {
			return;
		}
		
		try {
			getCustomConfig().save(usersFile);
		} catch (IOException e) {
			log.severe("Could not save config to " + usersFile);
			log.severe(e.getMessage());
		}
	}
	
	public boolean deleteCustomConfig() {
		return usersFile.delete();
	}
	
	public void setupDatabase() {
		if (getConfig().getString("storage-type").equalsIgnoreCase("mysql")) {
			host = getConfig().getString("mysql.host");
			port = getConfig().getString("mysql.port");
			db = getConfig().getString("mysql.database");
			user = getConfig().getString("mysql.username");
			pass = getConfig().getString("mysql.password");
			
			mysql = new MySQL(log, "[KillCount]", host, port, db, user, pass);
			log.info("Connected to MySQL database...");
			
			mysql.open();
			
			if (mysql.checkConnection()) {
				log.info("Successfully connected to database!");
				
				if (!mysql.checkTable("killcount")) {
					log.info("Creating table \"killcount\" in database " + getConfig().getString("mysql.database"));
					mysql.createTable("CREATE TABLE killcount ( id int NOT NULL AUTO_INCREMENT, username VARCHAR(32) NOT NULL, kills int NOT NULL, deaths int NOT NULL, PRIMARY KEY (id) ) ENGINE=MyISAM;");
				}
			} else {
				log.severe("Error connecting to database, shutting down.");
				getPluginLoader().disablePlugin(this);
			}
			
			mysql.close();
		}
	}
}