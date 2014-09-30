package me.mcluke300.playerlogger.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import me.mcluke300.playerlogger.playerlogger;
import me.mcluke300.playerlogger.config.*;

public class addData {
	playerlogger plugin;
	Connection con = null;
	LinkedList<DataRec> Records = new LinkedList<DataRec>();
	String servername = Bukkit.getServerName();

	public addData(playerlogger instance) {
		plugin = instance;
		if (ConnectDB()) {
			startWriterTask();

			Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
				@Override
				public void run() {
					pingDB();
				}
			}, 1200L, 1200L);
		}
	}

	static class DataRec {
		String playername;
		String type;
		String data;
		String worldname;
		String servername;
		int x;
		int y;
		int z;
	}
	
	public boolean ConnectDB() {
		// Close existing connection if there is one
		if (con != null) {
			try {
				con.commit();
				con.close();
			} catch (SQLException e) {
				plugin.Log("WARNING: Unable to close database connection!");
				e.printStackTrace();
			}
		}

		// Attempt to make a connection
		try {
			con = DriverManager.getConnection("jdbc:mysql://" + getConfig.MySQLServer() + "/" + getConfig.MySQLDatabase(), getConfig.MySQLUser(), getConfig.MySQLPassword());
			con.setAutoCommit(false);
		} catch (SQLException e) {
			plugin.Log("ERROR: Unable to connect to database!");
			e.printStackTrace();
		}
		
		if (con == null) {
			return false;
		} else {
			return true;
		}
	}

	// MySQL
	public void add(Player player, String type, String data, World world) {
		if (con == null) return;
		
		int x = 0, y = 0, z = 0;

		DataRec rec = new DataRec();

		if (player != null) {
			x = player.getLocation().getBlockX();
			y = player.getLocation().getBlockY();
			z = player.getLocation().getBlockZ();
			rec.playername = player.getName();
		} else {
			rec.playername = "";
		}

		if (world != null) {
			rec.worldname = world.getName();
		} else {
			rec.worldname = "";
		}

		rec.type = type;
		rec.data = data;
		rec.x = x;
		rec.y = y;
		rec.z = z;

		// Add record to list
		int count = 0;
		synchronized (Records) {
			Records.add(rec);
			count = Records.size();
		}
		
		if (count > 50) {
			plugin.DebugLog("Queue limit reached, forcing save.");
			writeBuffer();
		}
	}

	public void startWriterTask() {
		Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
			@Override
			public void run() {
				writeBuffer();
			}
		}, 400L, 400L);
	}
	
	public boolean writeBuffer() {
		int count = 0;
		synchronized (Records) {
			count = Records.size();
		}

		if (count == 0) {
			// Nothing to write
			plugin.DebugLog("Queue is empty. Nothing to write.");
			return false;
		}

		if (con == null) {
			plugin.Log("WARNING: No valid database connection - Not writing record queue");
			return false;
		}
		
		plugin.DebugLog("Launching save task for " + count + " queued records...");
		Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			@Override
			public void run() {
				PreparedStatement pst = null;
				long time = System.currentTimeMillis() / 1000; // Unix time
				try {
					String tablename = "`" + getConfig.MySQLTable() + "`";
					
					LinkedList<DataRec> copy = null;
					synchronized (Records) {
						copy = (LinkedList) Records.clone();
						Records.clear();
					}
					plugin.DebugLog("Saving " + copy.size() + " records...");
					
					// Prepared statement
					pst = con.prepareStatement("INSERT INTO "+ tablename +" (playername, type, time, data, x, y, z, world, server) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
					
					// Save all the queued records
					for (DataRec rec: copy) {
						// Values
						pst.setString(1, rec.playername);
						pst.setString(2, rec.type);
						pst.setLong(3, time);
						pst.setString(4, rec.data);
						pst.setInt(5, rec.x);
						pst.setInt(6, rec.y);
						pst.setInt(7, rec.z);
						pst.setString(8, rec.worldname);
						pst.setString(8, servername);
						
						// Do the MySQL query
						pst.executeUpdate();
					}
					copy.clear();
					con.commit();
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}
		});
		return true;
	}

	public void pingDB() {
		try {
			Statement st = con.createStatement();
			st.executeQuery("/* ping */ SELECT 1");
		} catch (SQLException e) {
			plugin.Log("WARNING: MySQL database ping failed!");
			plugin.Log("Reconnecting to database...");
			ConnectDB();
			e.printStackTrace();
		}
		
	}
}