package me.mcluke300.playerlogger.listeners;

import java.util.Map;

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
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.inventory.ItemStack;
import org.dynmap.DynmapWebChatEvent;

public class PListener implements Listener {
	playerlogger plugin;
	addData datadb;

	public PListener(playerlogger instance) {
		plugin = instance;
		datadb = new addData(plugin);
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

	// Player Chat
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerChat(final AsyncPlayerChatEvent event) {
		if (getConfig.PlayerChat()) {
			Player player = event.getPlayer();
			World world = player.getWorld();
			String msg = event.getMessage();
			datadb.add(player, "chat", msg, world);
		}
	}

	// Player Web Chat
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerWebChat(final DynmapWebChatEvent event) {
		if (getConfig.PlayerChat()) {
			String msg = event.getMessage();
			datadb.add(null, "webchat", msg, null);
		}
	}

	// Player Command
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerCmd(final PlayerCommandPreprocessEvent event) {
		if (getConfig.PlayerCommands()) {
			Player player = event.getPlayer();
			World world = player.getWorld();
			String msg = event.getMessage();
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
				datadb.add(player, "command", msg, world);
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

	// Player Sign Change event
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onSign(SignChangeEvent event) {
		if (event.isCancelled() == false) {
			if (getConfig.PlayerSignText()) {
				Player player = event.getPlayer();
				World world = player.getWorld();
				String[] lines = event.getLines();
				datadb.add(player, "sign", "[" + lines[0] + "]" + "[" + lines[1] + "]" + "[" + lines[2] + "]" + "[" + lines[3] + "]", world);
			}
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

	// Console Logger
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerConsoleCommand(ServerCommandEvent event) {
		String msg = event.getCommand();
		datadb.add(null, "console", msg, null);
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
}
