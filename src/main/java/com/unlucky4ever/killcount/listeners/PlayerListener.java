package com.unlucky4ever.killcount.listeners;

import java.awt.Color;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import com.unlucky4ever.killcount.KillCount;

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
	}
}