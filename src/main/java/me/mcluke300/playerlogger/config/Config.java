package me.mcluke300.playerlogger.config;

import java.util.ArrayList;
import java.util.List;

import me.mcluke300.playerlogger.PlayerLogger;

public class Config {
    private static boolean SystemDebug;
    private static boolean PlayerJoins;
    private static boolean PlayerQuit;
    private static boolean PlayerChat;
    private static boolean PlayerCommands;
    private static boolean PlayerDeaths;
    private static boolean PlayerEnchants;
    private static boolean PlayerPvp;
    private static boolean PlayerBucketPlace;
    private static boolean PlayerSignText;
    private static boolean ConsoleCommands;
    private static boolean LogBlackListedBlocks;
    private static List<String> Blocks;
    private static boolean BlackListCommands;
    private static boolean BlackListCommandsMySQL;
    private static List<String> CommandsToBlock;
    private static String MySQLServer;
    private static String MySQLDatabase;
    private static String MySQLUser;
    private static String MySQLPassword;
    private static String MySQLSSL;
    private static String MySQLTable;

    public Config() {
    }

    public static String getMySQLSSL() {
        return MySQLSSL;
    }

    public static void LoadConfiguration() {

        // BlackList
        List<String> words = new ArrayList<>();
        words.add("Bedrock");
        words.add("tnt");
        words.add("diamond_block");

        // Commands not to log
        List<String> cmds = new ArrayList<>();
        cmds.add("/login");
        cmds.add("/changepassword");
        cmds.add("/register");

        // Defaults
        PlayerLogger.plugin.getConfig().addDefault("Log.PlayerJoins", true);
        PlayerLogger.plugin.getConfig().addDefault("Log.PlayerQuit", true);
        PlayerLogger.plugin.getConfig().addDefault("Log.PlayerChat", true);
        PlayerLogger.plugin.getConfig().addDefault("Log.PlayerCommands", true);
        PlayerLogger.plugin.getConfig().addDefault("Log.PlayerDeaths", true);
        PlayerLogger.plugin.getConfig().addDefault("Log.PlayerEnchants", true);
        PlayerLogger.plugin.getConfig().addDefault("Log.Pvp", true);
        PlayerLogger.plugin.getConfig().addDefault("Log.PlayerBucketPlace", true);
        PlayerLogger.plugin.getConfig().addDefault("Log.ConsoleCommands", true);
        PlayerLogger.plugin.getConfig().addDefault("BlackList.LogBlackListedBlocks", true);
        PlayerLogger.plugin.getConfig().addDefault("BlackList.Blocks", words);
        PlayerLogger.plugin.getConfig().addDefault("Log.PlayerSignText", true);
        PlayerLogger.plugin.getConfig().addDefault("Commands.BlackListCommands", false);
        PlayerLogger.plugin.getConfig().addDefault("Commands.BlackListCommandsForMySQL", false);
        PlayerLogger.plugin.getConfig().addDefault("Commands.CommandsToBlock", cmds);
        PlayerLogger.plugin.getConfig().addDefault("MySQL.Server", "localhost");
        PlayerLogger.plugin.getConfig().addDefault("MySQL.Database", "playerlogger");
        PlayerLogger.plugin.getConfig().addDefault("MySQL.Table", "playerlogger");
        PlayerLogger.plugin.getConfig().addDefault("MySQL.User", "user");
        PlayerLogger.plugin.getConfig().addDefault("MySQL.Password", "password");
        PlayerLogger.plugin.getConfig().addDefault("MySQL.useSSL", false);

        PlayerLogger.plugin.getConfig().addDefault("System.Debug", false);

        // Copy Defaults
        PlayerLogger.plugin.getConfig().options().copyDefaults(true);
        PlayerLogger.plugin.saveConfig();

    }

    public static void getValues() {
        SystemDebug = PlayerLogger.plugin.getConfig().getBoolean("System.Debug");

        PlayerJoins = PlayerLogger.plugin.getConfig().getBoolean("Log.PlayerJoins");
        PlayerQuit = PlayerLogger.plugin.getConfig().getBoolean("Log.PlayerQuit");
        PlayerChat = PlayerLogger.plugin.getConfig().getBoolean("Log.PlayerChat");
        PlayerCommands = PlayerLogger.plugin.getConfig().getBoolean("Log.PlayerCommands");
        PlayerDeaths = PlayerLogger.plugin.getConfig().getBoolean("Log.PlayerDeaths");
        PlayerEnchants = PlayerLogger.plugin.getConfig().getBoolean("Log.PlayerEnchants");
        PlayerPvp = PlayerLogger.plugin.getConfig().getBoolean("Log.Pvp");
        PlayerBucketPlace = PlayerLogger.plugin.getConfig().getBoolean("Log.PlayerBucketPlace");
        PlayerSignText = PlayerLogger.plugin.getConfig().getBoolean("Log.PlayerSignText");

        ConsoleCommands = PlayerLogger.plugin.getConfig().getBoolean("Log.ConsoleCommands");

        LogBlackListedBlocks = PlayerLogger.plugin.getConfig().getBoolean("BlackList.LogBlackListedBlocks");
        Blocks = PlayerLogger.plugin.getConfig().getStringList("BlackList.Blocks");
        for (String m : Blocks) {
            try {
                Integer i = Integer.valueOf(m);
                PlayerLogger.plugin.Log("Block Black List is still using integers. Please update to use a list of material Names - this will break in 1.13");
            } catch (NumberFormatException ignored) {
            }
        }
        BlackListCommands = PlayerLogger.plugin.getConfig().getBoolean("Commands.BlackListCommands");
        BlackListCommandsMySQL = PlayerLogger.plugin.getConfig().getBoolean("Commands.BlackListCommandsForMySQL");
        CommandsToBlock = PlayerLogger.plugin.getConfig().getStringList("Commands.CommandsToBlock");

        MySQLServer = PlayerLogger.plugin.getConfig().getString("MySQL.Server");
        MySQLDatabase = PlayerLogger.plugin.getConfig().getString("MySQL.Database");
        MySQLUser = PlayerLogger.plugin.getConfig().getString("MySQL.User");
        MySQLPassword = PlayerLogger.plugin.getConfig().getString("MySQL.Password");
        MySQLSSL = PlayerLogger.plugin.getConfig().getString("MySQL.useSSL");
        MySQLTable = PlayerLogger.plugin.getConfig().getString("MySQL.Table");
    }

    public static boolean PlayerJoins() {
        return PlayerJoins;
    }

    public static boolean PlayerQuit() {
        return PlayerQuit;
    }

    public static boolean PlayerChat() {
        return PlayerChat;
    }

    public static boolean PlayerCommands() {
        return PlayerCommands;
    }

    public static boolean PlayerDeaths() {
        return PlayerDeaths;
    }

    public static boolean PlayerEnchants() {
        return PlayerEnchants;
    }

    public static boolean PlayerPvp() {
        return PlayerPvp;
    }

    public static boolean PlayerBucketPlace() {
        return PlayerBucketPlace;
    }

    public static boolean PlayerSignText() {
        return PlayerSignText;
    }

    public static boolean ConsoleCommands() {
        return ConsoleCommands;
    }

    public static boolean LogBlackListedBlocks() {
        return LogBlackListedBlocks;
    }

    public static List<String> Blocks() {
        return Blocks;
    }

    public static boolean BlackListCommands() {
        return BlackListCommands;
    }

    public static boolean BlackListCommandsMySQL() {
        return BlackListCommandsMySQL;
    }

    public static List<String> CommandsToBlock() {
        return CommandsToBlock;
    }

    public static String MySQLServer() {
        return MySQLServer;
    }

    public static String MySQLDatabase() {
        return MySQLDatabase;
    }

    public static String MySQLUser() {
        return MySQLUser;
    }

    public static String MySQLPassword() {
        return MySQLPassword;
    }

    public static String MySQLTable() {
        return MySQLTable;
    }

    public static boolean Debug() {
        return SystemDebug;
    }
}
