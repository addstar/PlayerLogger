package me.mcluke300.playerlogger.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.bukkit.Bukkit;

import me.mcluke300.playerlogger.playerlogger;
import me.mcluke300.playerlogger.config.*;

public class addData {
	playerlogger plugin;
	Connection con = null;

	public addData(playerlogger instance) {
		plugin = instance;
		try {
			con = DriverManager.getConnection("jdbc:mysql://" + getConfig.MySQLServer() + "/" + getConfig.MySQLDatabase() , getConfig.MySQLUser(), getConfig.MySQLPassword());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//MySQL
	public void add(final String playername, final String type, final String data, final double x, final double y, final double z, final String worldname, final Boolean staff) {
		Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
		    @Override
		    public void run() {			
				PreparedStatement pst = null;
				long time = System.currentTimeMillis()/1000; //Unix time
				//Checking if they should be logged
				if (staff && getConfig.LogOnlyStaff() || !getConfig.LogOnlyStaff()) {
					try {
						String database = "playerlogger";
						//Prepared statement
						pst = con.prepareStatement("INSERT INTO " +database+"(playername, type, time, data, x, y, z, world) VALUES(?, ?, ?, ?, ?, ?, ?, ?)");
						//Values
						pst.setString(1, playername);
						pst.setString(2, type);
						pst.setLong(3, time);
						pst.setString(4, data);
						pst.setDouble(5, x);
						pst.setDouble(6, y);
						pst.setDouble(7, z);
						pst.setString(8, worldname);
						//Do the MySQL query
						pst.executeUpdate();
					} catch (SQLException ex) {
						ex.printStackTrace();
					}
				}
			}
		});
	}
}
