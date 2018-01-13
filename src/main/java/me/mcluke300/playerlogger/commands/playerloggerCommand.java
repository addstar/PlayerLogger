package me.mcluke300.playerlogger.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import me.mcluke300.playerlogger.playerlogger;
import me.mcluke300.playerlogger.config.getConfig;
import me.mcluke300.playerlogger.mysql.mysql;

public class playerloggerCommand implements CommandExecutor {

	private final playerlogger plugin;

	public playerloggerCommand(playerlogger plugin) {
		this.plugin = plugin;
	}

	// Reload Command
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (commandLabel.equalsIgnoreCase("playerlogger")) {
			if (sender.hasPermission("playerlogger.admin")) {
				if (args.length != 1) {
					doHelp(sender);
					return false;
				} else {
					if(!sender.hasPermission("PlayerLogger.reload")){
						sender.sendMessage(ChatColor.RED + "You do not have permission for this command");
						return false;
					}
					// Reload
					if (args[0].equalsIgnoreCase("reload")) {
						plugin.reloadConfig();
						getConfig.getValues();
						mysql.createDatabase();
						sender.sendMessage(ChatColor.GREEN + "PlayerLogger Config Reloaded");
					}else{
						doHelp(sender);
					}
				}
			} else {

				return false;
			}
		}
		return false;
	}
	private void doHelp(CommandSender sender){
		sender.sendMessage(ChatColor.RED + "Usage: /playerlogger reload");
	}

}
