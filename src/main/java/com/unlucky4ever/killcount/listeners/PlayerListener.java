package com.unlucky4ever.killcount.listeners;

import java.awt.Color;
import java.sql.ResultSet;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import com.unlucky4ever.killcount.KillCount;
import com.unlucky4ever.killcount.extras.db.MySQL;

public class PlayerListener implements Listener {
	
	public KillCount plugin;
	public FileConfiguration users;
	
	public PlayerListener(KillCount instance) {
		this.plugin = instance;
	}
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (plugin.getConfig().getString("storage-type").equalsIgnoreCase("file")) {
			users = plugin.getCustomConfig();
			
			String player = event.getPlayer().getName().toLowerCase();
			int kills = users.getInt(player + ".kills");
			int deaths = users.getInt(player + ".deaths");
			
			if (kills == 0) {
				users.set(player + ".kills", Integer.valueOf(0));
			}
			
			if (deaths == 0) {
				users.set(player + ".deaths", Integer.valueOf(0));
			}
			
			plugin.saveCustomConfig();
		}
	}
	
	@EventHandler
	public void onPlayerDeath(EntityDeathEvent event) {
		if (!(event.getEntity() instanceof Player)) {
			return;
		}
		
		if (!(event.getEntity().getKiller() instanceof Player)) {
			return;
		}
		
		Player killer = event.getEntity().getKiller();
		Player killed = (Player) event.getEntity();
		
		if (killer.hasPermission("killcount.kill")) {
			logKill(killer.getName().toLowerCase());
			
			if (plugin.getConfig().getString("storage-type").equalsIgnoreCase("file")) {
				users = plugin.getCustomConfig();
				int kills = users.getInt(killer.getName().toLowerCase() + ".kills");
				
				if (plugin.getConfig().getBoolean("broadcast-kills")) {
					plugin.getServer().broadcastMessage(Color.red + killer.getDisplayName() + " has killed " + killed.getDisplayName() + ", their new kill count is " + kills + ".");
				} else {
					killer.sendMessage(Color.red + "You have killed " + killed.getDisplayName() + ", you now have " + kills + " kills.");
				}
			}
			
			if (plugin.getConfig().getString("storage-type").equalsIgnoreCase("mysql")) {
				plugin.mysql = new MySQL(plugin.log, "[KillCount]", plugin.host, plugin.port, plugin.db, plugin.user, plugin.pass);
				plugin.mysql.open();
				
				if (plugin.mysql.checkConnection()) {
					try {
						ResultSet kill = plugin.mysql.query("SELECT kills FROM killcount WHERE username='" + killer.getName() + "'");
						kill.first();
						int kills = kill.getInt(1);
						kill.close();
						
						if (plugin.getConfig().getBoolean("broadcast-kills")) {
							plugin.getServer().broadcastMessage(Color.red + killer.getDisplayName() + " has killed " + killed.getDisplayName() + ", their new kill count is " + kills + ".");
						} else {
							killer.sendMessage(Color.red + "You have killed " + killed.getDisplayName() + ", you now have " + kills + " kills.");
						}
					} catch (Exception e) {
						if (plugin.getConfig().getBoolean("debug")) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		
		if (killer.hasPermission("killcount.death")) {
			logDeath(killed.getName().toLowerCase());
			
			if (plugin.getConfig().getString("storage-type").equalsIgnoreCase("file")) {
				users = plugin.getCustomConfig();
				int deaths = users.getInt(killed.getName().toLowerCase() + ".deaths");
				int kills = users.getInt(killer.getName().toLowerCase() + ".kills");
				
				if (plugin.getConfig().getBoolean("broadcast-kills")) {
					plugin.getServer().broadcastMessage(Color.red + killer.getDisplayName() + " has killed " + killed.getDisplayName() + ", their new kill count is " + kills + ".");
				} else {
					killed.sendMessage(Color.red + "You were killed by " + killer.getDisplayName() + ", you now have " + deaths + " deaths.");
				}
			}
			
			if (plugin.getConfig().getString("storage-type").equalsIgnoreCase("mysql")) {
				plugin.mysql = new MySQL(plugin.log, "[KillCount]", plugin.host, plugin.port, plugin.db, plugin.user, plugin.pass);
				plugin.mysql.open();
				
				if (plugin.mysql.checkConnection()) {
					try {
						ResultSet kill = plugin.mysql.query("SELECT kills FROM killcount WHERE username='" + killer.getName() + "'");
						ResultSet death = plugin.mysql.query("SELECT deaths FROM killcount WHERE username='" + killed.getName() + "'");
						kill.first();
						death.first();
						int kills = kill.getInt(1);
						int deaths = death.getInt(1);
						kill.close();
						death.close();
						
						if (plugin.getConfig().getBoolean("broadcast-kills")) {
							plugin.getServer().broadcastMessage(Color.red + killer.getDisplayName() + " has killed " + killed.getDisplayName() + ", their new kill count is " + kills + ".");
						} else {
							killer.sendMessage(Color.red + "You were killed by " + killer.getDisplayName() + ", you now have " + deaths + " deaths.");
						}
					} catch (Exception e) {
						if (plugin.getConfig().getBoolean("debug")) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
	
	private void logKill(String username) {
		if (plugin.getConfig().getString("storage-type").equalsIgnoreCase("file")) {
			users = plugin.getCustomConfig();
			
			int kills = users.getInt(username.toLowerCase() + ".kills");
			kills++;
			users.set(username.toLowerCase() + ".kills", Integer.valueOf(kills));
			
			plugin.saveCustomConfig();
		}
		
		if (plugin.getConfig().getString("storage-type").equalsIgnoreCase("mysql")) {
			plugin.mysql = new MySQL(plugin.log, "[KillCount]", plugin.host, plugin.port, plugin.db, plugin.user, plugin.pass);
			plugin.mysql.open();
			
			if (plugin.mysql.checkConnection()) {
				try {
					ResultSet kill = plugin.mysql.query("SELECT COUNT(*) FROM killcount WHERE username='" + username + "'");
					kill.first();
					int count = kill.getInt(1);
					
					if (count == 0) {
						if (plugin.getConfig().getBoolean("debug")) {
							plugin.log.info("Inserted " + username);
						}
						
						plugin.mysql.query("INSERT INTO killcount (username, kills, deaths) VALUES ('" + username + "', 0, 0)");
						kill.close();
					}
					
					kill = plugin.mysql.query("SELECT kills FROM killcount WHERE username='" + username + "'");
					kill.first();
					int kills = kill.getInt(1);
					kill.close();
					kills++;
					
					if (plugin.getConfig().getBoolean("debug")) {
						plugin.log.info("Added a kill to " + username);
					}
					
					plugin.mysql.query("UPDATE killcount SET kills='" + kills + "' WHERE username='" + username + "'");
				} catch (Exception e) {
					if (plugin.getConfig().getBoolean("debug")) {
						e.printStackTrace();
					}
				}
				
				plugin.mysql.close();
			}
		}
	}
	
	private void logDeath(String username) {
		if (plugin.getConfig().getString("storage-type").equalsIgnoreCase("file")) {
			users = plugin.getCustomConfig();
			
			int deaths = users.getInt(username.toLowerCase() + ".deaths");
			deaths++;
			users.set(username.toLowerCase() + ".deaths", Integer.valueOf(deaths));
			
			plugin.saveCustomConfig();
		}
		
		if (plugin.getConfig().getString("storage-type").equalsIgnoreCase("mysql")) {
			plugin.mysql = new MySQL(plugin.log, "[KillCount]", plugin.host, plugin.port, plugin.db, plugin.user, plugin.pass);
			plugin.mysql.open();
			
			if (plugin.mysql.checkConnection()) {
				try {
					ResultSet death = plugin.mysql.query("SELECT COUNT(*) FROM killcount WHERE username='" + username + "'");
					death.first();
					int count = death.getInt(1);
					
					if (count == 0) {
						if (plugin.getConfig().getBoolean("debug")) {
							plugin.log.info("Inserted " + username);
						}
						
						plugin.mysql.query("INSERT INTO killcount (username, kills, deaths) VALUES ('" + username + "', 0, 0)");
						death.close();
					}
					
					death = plugin.mysql.query("SELECT deaths FROM killcount WHERE username='" + username + "'");
					death.first();
					int deaths = death.getInt(1);
					death.close();
					deaths++;
					
					if (plugin.getConfig().getBoolean("debug")) {
						plugin.log.info("Added a death to " + username);
					}
					
					plugin.mysql.query("UPDATE killcount SET deaths='" + deaths + "' WHERE username='" + username + "'");
				} catch (Exception e) {
					if (plugin.getConfig().getBoolean("debug")) {
						e.printStackTrace();
					}
				}
				
				plugin.mysql.close();
			}
		}
	}
}