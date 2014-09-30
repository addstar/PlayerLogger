package me.mcluke300.playerlogger.config;

import java.util.ArrayList;
import java.util.List;

import me.mcluke300.playerlogger.playerlogger;

public class config {
	playerlogger plugin;

	public config(playerlogger instance) {
		plugin = instance;
	}

	public static void LoadConfiguration() {

		// BlackList
		List<String> words = new ArrayList<String>();
		words.add("7");
		words.add("46");
		words.add("57");

		// Commands not to log
		List<String> cmds = new ArrayList<String>();
		cmds.add("/login");
		cmds.add("/changepassword");
		cmds.add("/register");

		// Defaults
		playerlogger.plugin.getConfig().addDefault("Log.PlayerJoins", true);
		playerlogger.plugin.getConfig().addDefault("Log.PlayerQuit", true);
		playerlogger.plugin.getConfig().addDefault("Log.PlayerChat", true);
		playerlogger.plugin.getConfig().addDefault("Log.PlayerCommands", true);
		playerlogger.plugin.getConfig().addDefault("Log.PlayerDeaths", true);
		playerlogger.plugin.getConfig().addDefault("Log.PlayerEnchants", true);
		playerlogger.plugin.getConfig().addDefault("Log.Pvp", true);
		playerlogger.plugin.getConfig().addDefault("Log.PlayerBucketPlace", true);
		playerlogger.plugin.getConfig().addDefault("Log.ConsoleCommands", true);
		playerlogger.plugin.getConfig().addDefault("BlackList.LogBlackListedBlocks", true);
		playerlogger.plugin.getConfig().addDefault("BlackList.Blocks", words);
		playerlogger.plugin.getConfig().addDefault("Log.PlayerSignText", true);
		playerlogger.plugin.getConfig().addDefault("Commands.BlackListCommands", false);
		playerlogger.plugin.getConfig().addDefault("Commands.BlackListCommandsForMySQL", false);
		playerlogger.plugin.getConfig().addDefault("Commands.CommandsToBlock", cmds);
		playerlogger.plugin.getConfig().addDefault("MySQL.Server", "localhost");
		playerlogger.plugin.getConfig().addDefault("MySQL.Database", "playerlogger");
		playerlogger.plugin.getConfig().addDefault("MySQL.Table", "playerlogger");
		playerlogger.plugin.getConfig().addDefault("MySQL.User", "user");
		playerlogger.plugin.getConfig().addDefault("MySQL.Password", "password");
		playerlogger.plugin.getConfig().addDefault("System.Debug", false);

		// Copy Defaults
		playerlogger.plugin.getConfig().options().copyDefaults(true);
		playerlogger.plugin.saveConfig();

	}

}
