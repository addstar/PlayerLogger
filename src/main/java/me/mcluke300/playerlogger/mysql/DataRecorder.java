package me.mcluke300.playerlogger.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import me.mcluke300.playerlogger.PlayerLogger;
import me.mcluke300.playerlogger.config.*;

public class DataRecorder {
    private final PlayerLogger plugin;
    private final LinkedList<DataRec> records = new LinkedList<>();
    private final String servername;
    private Connection con = null;

    public DataRecorder(PlayerLogger instance) {
        plugin = instance;
        servername = instance.getConfig().getString("server-name", Bukkit.getServer().getName() + new Random().nextInt(1000));
        if (ConnectDB()) {
            startWriterTask();
            Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this::pingDB, 1200L, 1200L);
        }
    }

    private boolean ConnectDB() {
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
            con = plugin.getDataSource().getConnection();
            con.setAutoCommit(false);
        } catch (SQLException e) {
            plugin.Log("ERROR: Unable to connect to database!");
            e.printStackTrace();
        }
        return con != null;
    }

    // MySQL
    public void add(Player player, String type, String data, World world) {
        add(player, type, data, world, false, "");
    }

    public void add(Player player, String type, String data, World world, boolean cancelled) {
        add(player, type, data, world, cancelled, "");
    }

    public void add(Player player, String type, String data, World world, boolean cancelled, String senderName) {
        if (con == null) return;

        int x = 0, y = 0, z = 0;

        DataRec rec = new DataRec();

        if (player != null) {
            x = player.getLocation().getBlockX();
            y = player.getLocation().getBlockY();
            z = player.getLocation().getBlockZ();
            rec.playername = player.getName();
            rec.playeruuid = player.getUniqueId().toString();
        } else {
            rec.playeruuid = null;
            if (senderName == null)
                rec.playername = "";
            else
                rec.playername = senderName;
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
        rec.time = System.currentTimeMillis() / 1000; // Unix time
        rec.servername = servername;
        if (cancelled)
            rec.cancelled = (byte) 1;
        else
            rec.cancelled = (byte) 0;

        // Add record to list
        int count;
        synchronized (records) {
            records.add(rec);
            count = records.size();
        }

        if (count > 50) {
            plugin.DebugLog("Queue limit reached, forcing save.");
            writeBuffer();
        }
    }

    private void startWriterTask() {
        Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this::writeBuffer, 400L, 400L);
    }

    private void writeBuffer() {
        int count;
        synchronized (records) {
            count = records.size();
        }

        if (count == 0) {
            // Nothing to write
            plugin.DebugLog("Queue is empty. Nothing to write.");
            return;
        }

        if (con == null) {
            plugin.Log("WARNING: No valid database connection - Not writing record queue");
            return;
        }

        plugin.DebugLog("Launching save task for " + count + " queued records...");
        Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            PreparedStatement pst = null;
            try {
                String tablename = "`" + Config.MySQLTable() + "`";

                LinkedList<DataRec> copy;
                synchronized (records) {
                    copy = new LinkedList<>(records);
                    records.clear();

                }
                plugin.DebugLog("Saving " + copy.size() + " records...");

                // Prepared statement
                pst = con.prepareStatement("INSERT INTO " + tablename + " (playeruuid, playername, type, time, data, x, y, z, world, server, cancelled) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

                // Save all the queued records
                for (DataRec rec : copy) {
                    // Values
                    pst.setString(1, rec.playeruuid);
                    pst.setString(2, rec.playername);
                    pst.setString(3, rec.type);
                    pst.setLong(4, rec.time);
                    pst.setString(5, rec.data);
                    pst.setInt(6, rec.x);
                    pst.setInt(7, rec.y);
                    pst.setInt(8, rec.z);
                    pst.setString(9, rec.worldname);
                    pst.setString(10, servername);
                    pst.setByte(11, rec.cancelled);

                    // Do the MySQL query
                    pst.executeUpdate();
                }
                copy.clear();
                con.commit();
            } catch (SQLException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    if (pst != null) pst.close();
                } catch (SQLException e) {
                    plugin.Log("ERROR: Failed to close PreparedStatement!");
                    e.printStackTrace();
                }
            }
        });
    }

    private void pingDB() {
        Statement st = null;
        try {
            st = con.createStatement();
            st.executeQuery("/* ping */ SELECT 1");
        } catch (SQLException e) {
            plugin.Log("WARNING: MySQL database ping failed!");
            plugin.Log("Reconnecting to database...");
            ConnectDB();
            e.printStackTrace();
        } finally {
            try {
                if (st != null) st.close();
            } catch (SQLException e) {
                plugin.Log("ERROR: Failed to close Statement!");
                e.printStackTrace();
            }
        }
    }

    static class DataRec {
        String playeruuid;
        String playername;
        String type;
        String data;
        String worldname;
        String servername;
        int x;
        int y;
        int z;
        long time;
        byte cancelled;
    }
}