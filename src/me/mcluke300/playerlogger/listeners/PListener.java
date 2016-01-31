package me.mcluke300.playerlogger.listeners;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.mcluke300.playerlogger.playerlogger;
import me.mcluke300.playerlogger.config.*;
import me.mcluke300.playerlogger.mysql.*;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.inventory.ItemStack;

public class PListener implements Listener {
	playerlogger plugin;
	addData datadb;
	Pattern numberMatcher;

	IdentityHashMap<PlayerEvent, String> cachedEvents;
	IdentityHashMap<SignChangeEvent, String> cachedSignEvents;
	IdentityHashMap<ServerCommandEvent, String> cachedConsoleCommands;

	public PListener(playerlogger instance) {
		plugin = instance;
		datadb = new addData(plugin);
		cachedEvents = new IdentityHashMap<PlayerEvent, String>();
		cachedSignEvents = new IdentityHashMap<SignChangeEvent, String>();
		cachedConsoleCommands = new IdentityHashMap<ServerCommandEvent, String>();
		numberMatcher = Pattern.compile("^\\d+$");
	}

	// Player Join
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(PlayerLoginEvent event) {
		if (getConfig.PlayerJoins()) {
			if (event.getResult() == PlayerLoginEvent.Result.ALLOWED) {
				Player player = event.getPlayer();
				World world = player.getWorld();
				String ip = "Error";
				ip = event.getAddress().getHostAddress();
				datadb.add(player, "join", ip, world);
			}
		}
	}

	// Player Quit
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerQuit(PlayerQuitEvent event) {
		if (getConfig.PlayerQuit()) {
			Player player = event.getPlayer();
			World world = player.getWorld();
			datadb.add(player, "quit", "", world);
		}
	}

	// Player Chat (Lowest)
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void onPlayerChatLowest(final AsyncPlayerChatEvent event) {
		synchronized (cachedEvents) {
			cachedEvents.put(event, event.getMessage());
		}
	}

	// Player Chat (Monitor)
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void onPlayerChat(final AsyncPlayerChatEvent event) {

		String origMessage;
		synchronized (cachedEvents) {
			origMessage = cachedEvents.remove(event);
		}

		if (getConfig.PlayerChat()) {
			Player player = event.getPlayer();
			World world = player.getWorld();
			String msg = FormatMessage(origMessage, event.getMessage());

			if (event.isCancelled()) {
				Matcher match = numberMatcher.matcher(origMessage);
				if (match.matches()) {
					// The original message was simply a number and it was cancelled
					// This was most likely a market purchase; do not log it
					return;
				}
			}

			datadb.add(player, "chat", msg, world, event.isCancelled());
		}
	}

	// Player Command (Lowest)
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void onPlayerCmdLowest(final PlayerCommandPreprocessEvent event) {
		synchronized (cachedEvents) {
			cachedEvents.put(event, event.getMessage());
		}
	}

	// Player Command (Monitor)
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void onPlayerCmd(final PlayerCommandPreprocessEvent event) {

		String origMessage;
		synchronized (cachedEvents) {
			origMessage = cachedEvents.remove(event);
		}

		if (getConfig.PlayerCommands()) {
			Player player = event.getPlayer();
			World world = player.getWorld();
			String msg = FormatMessage(origMessage, event.getMessage());
			String msg2[] = event.getMessage().split(" ");
			Boolean log = true;
			if (getConfig.BlackListCommands() || getConfig.BlackListCommandsMySQL()) {
				for (String m : getConfig.CommandsToBlock()) {
					m = m.toString().toLowerCase();
					if (msg2[0].equalsIgnoreCase(m)) {
						log = false;
						break;
					}
				}
			}
			// Log this command
			if (log) {
				datadb.add(player, "command", msg, world, event.isCancelled());
			}
		}
	}

	// Player Deaths
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerDeath(EntityDeathEvent event) {
		Entity ent = event.getEntity();
		if ((ent instanceof Player) && (getConfig.PlayerDeaths())) {
			Player player = (Player) event.getEntity();
			World world = player.getWorld();
			datadb.add(player, "death", "", world);
		}
	}

	// Player Enchant
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEnchant(EnchantItemEvent event) {
		if (getConfig.PlayerEnchants()) {
			Player player = (Player) event.getEnchanter();
			World world = player.getWorld();
			Map<Enchantment, Integer> ench = event.getEnchantsToAdd();
			ItemStack item = event.getItem();
			int cost = event.getExpLevelCost();
			datadb.add(player, "enchant", item + " " + ench + " Xp Cost:" + cost, world);
		}
	}

	// Player Bucket
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBucket(PlayerBucketEmptyEvent event) {
		if (getConfig.PlayerBucketPlace()) {
			if (event.isCancelled() == false) {
				Player player = event.getPlayer();
				World world = player.getWorld();
				if (event.getBucket() != null && event.getBucket() == Material.LAVA_BUCKET) {
					datadb.add(player, "bucket", "Lava", world);
				} else if (event.getBucket() != null && event.getBucket() == Material.WATER_BUCKET) {
					datadb.add(player, "bucket", "Water", world);
				}
			}
		}
	}

	// Player Sign Change event (Lowest)
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void onSignLowest(SignChangeEvent event) {
		synchronized (cachedSignEvents) {
			cachedSignEvents.put(event, FormatSignText(event.getLines()));
		}
	}

	// Player Sign Change event (Monitor)
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void onSign(SignChangeEvent event) {
	
		String origLines;
		synchronized (cachedSignEvents) {
			origLines = cachedSignEvents.remove(event);
		}

		String signLines = FormatSignText(event.getLines());
		String formattedMsg = FormatMessage(origLines, signLines);

		if (getConfig.PlayerSignText() || !signLines.equals(formattedMsg) || event.isCancelled()) {
			Player player = event.getPlayer();
			World world = player.getWorld();
			datadb.add(player, "sign", formattedMsg, world, event.isCancelled());
		}
	}

	// PlayerPvp
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPVPDeath(EntityDeathEvent event) {
		if (getConfig.PlayerPvp()) {
			org.bukkit.entity.Entity player = event.getEntity();
			if (event.getEntity().getLastDamageCause() instanceof org.bukkit.event.entity.EntityDamageByEntityEvent) {
				org.bukkit.entity.Entity damager = ((org.bukkit.event.entity.EntityDamageByEntityEvent) event.getEntity().getLastDamageCause()).getDamager();
				if (player instanceof Player) {
					if (damager instanceof Player) {
						World world = player.getWorld();
						datadb.add((Player)damager, "kill", ((Player) player).getName(), world);
						datadb.add((Player)player, "killedby", ((Player) damager).getName(), world);
					}
				}
			}
		}
	}

	// Console Logger (Lowest)
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void onPlayerConsoleCommandLowest(ServerCommandEvent event) {
		synchronized (cachedConsoleCommands) {
			cachedConsoleCommands.put(event, event.getCommand());
		}
	}

	// Console Logger (Monitor)
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void onPlayerConsoleCommand(ServerCommandEvent event) {

		String origCommand;
		synchronized (cachedConsoleCommands) {
			origCommand = cachedConsoleCommands.remove(event);
		}

		String msg = FormatMessage(origCommand, event.getCommand());
		String senderName = event.getSender().getName();
		datadb.add(null, "console", msg, null, false, senderName);
	}

	// BlockPlace
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		if (event.isCancelled() == false) {
			if (getConfig.LogBlackListedBlocks()) {
				Player player = event.getPlayer();
				World world = player.getWorld();
				String blockid = "" + event.getBlock().getTypeId();
				Boolean log = false;
				for (String m : getConfig.Blocks()) {
					m = m.toString().toLowerCase();
					if (blockid.equals(m) || m.equalsIgnoreCase("*")) {
						log = true;
						break;
					}
				}
				if (log) {
					String blockname = event.getBlock().getType().toString();
					blockname = blockname.replaceAll("_", " ");
					datadb.add(player, "place", blockname, world);
				}
			}
		}
	}

	// BlockBreak
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.isCancelled() == false) {
			if (getConfig.LogBlackListedBlocks()) {
				Player player = event.getPlayer();
				World world = player.getWorld();
				String blockid = String.valueOf(event.getBlock().getTypeId());
				Boolean log = false;
				for (String m : getConfig.Blocks()) {
					m = m.toString().toLowerCase();
					if (blockid.equalsIgnoreCase(m) || m.equalsIgnoreCase("*")) {
						log = true;
						break;
					}
				}
				if (log) {
					String blockname = event.getBlock().getType().toString();
					blockname = blockname.replaceAll("_", " ");
					datadb.add(player, "break", blockname, world);
				}
			}
		}
	}

	private String FormatMessage(String origMessage, String message) {

		if (origMessage == null || origMessage.replace('§', '&').equals(message.replace('§', '&'))) {
			return message;
		}
		else {
			// The message has been altered
			
			if (message.equals("/nullcmd")) {
				// This happens when admins use /bcast or similar
				// It also happens for certain commands used at console
				// Log the original command
				return origMessage;
			}

			if (message.equals("commandhelper null")) {
				// This happens when admins use a CH-handled command at console
				// Log the original command followed by {commandhelper}
				return origMessage + " {commandhelper}";
			}

			return origMessage + " --> " + message;
		}
	}

	private String FormatSignText(String[] lines) {
		return "[" + lines[0] + "]" + "[" + lines[1] + "]" + "[" + lines[2] + "]" + "[" + lines[3] + "]";
	}

}
