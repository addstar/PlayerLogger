package me.mcluke300.playerlogger;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import me.mcluke300.playerlogger.commands.playerloggerCommand;
import me.mcluke300.playerlogger.config.*;
import me.mcluke300.playerlogger.listeners.PListener;
import me.mcluke300.playerlogger.mysql.mysql;

public class playerlogger extends JavaPlugin {

	// Plugin
	public static playerlogger plugin;

	// Command
	private playerloggerCommand executor;

	@Override
	public void onEnable() {
		plugin = this;

		config.LoadConfiguration();
		getConfig.getValues();
		mysql.createDatabase();

		// Registering Listeners
		Bukkit.getServer().getPluginManager().registerEvents(new PListener(this), this);

		// Commands
		executor = new playerloggerCommand(this);
		getCommand("playerlogger").setExecutor(executor);
	}

	@Override
	public void onDisable() {
		// Do Nothing
	}

	public void DebugLog(String msg) {
		if (getConfig.Debug()) {
			System.out.println("[PlayerLogger] " + msg);
		}
	}
}
