package me.mcluke300.playerlogger.mysql;

import me.mcluke300.playerlogger.config.Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class MySQL {
    private final Properties properties = new Properties();

    public MySQL() {
    }

    public  Connection getConnection() throws SQLException{
        return DriverManager.getConnection("jdbc:mysql://" + Config.MySQLServer() + "/" + Config.MySQLDatabase(), properties);
    }
    // Creating MySQL database
    public void initialize() {
        int rs = 0;
        properties.setProperty("user", Config.MySQLUser());
        properties.setProperty("password", Config.MySQLPassword());
        properties.setProperty("port", "3306");
        properties.setProperty("useSSL", Config.getMySQLSSL());
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://" + Config.MySQLServer() + "/" + Config.MySQLDatabase(), properties);
             Statement st = connection.createStatement()) {


            String tableName = Config.MySQLTable();

            // Make table if it does not exist query onEnable
            rs = st.executeUpdate("CREATE TABLE IF NOT EXISTS `" + tableName + "` "
                    + "(`id` INT(11) unsigned NOT NULL AUTO_INCREMENT,"
                    + "`playername` varchar(20),"
                    + "`type` varchar(15),"
                    + "`time` INT(255),"
                    + "`data` text,"
                    + "`x` MEDIUMINT(255),"
                    + "`y` MEDIUMINT(255),"
                    + "`z` MEDIUMINT(255),"
                    + "`world` varchar(40),"
                    + "`server` varchar(20),"
                    + "`cancelled` TINYINT,"
                    + "PRIMARY KEY (`id`),"
                    + "KEY `time` (`time`),"
                    + "KEY `playername` (`playername`),"
                    + "KEY `type` (`type`))");

            // Add field cancelled if the table does not yet have it
            addTinyIntColumnIfMissing(connection, tableName, "cancelled");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.print(rs);
        }
    }

    /*
     * Adds the given column to the given table if the column does not yet exist
     * The new column will be TINYINT NULL
     */
    private static void addTinyIntColumnIfMissing(Connection connection, String table, String column) {

        boolean columnExists = doesTableHaveColumn(connection, table, column);

        if (!columnExists) {
            addTinyIntColumnToTable(connection, table, column);
        }

    }

    private static void addTinyIntColumnToTable(Connection connection, String table, String column) {
        String query = "ALTER TABLE `" + table + "` ADD `" + column + "` TINYINT NULL DEFAULT 0";

        try (Statement statement = connection.createStatement()
        ) {
            statement.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalStateException();
        }
    }

    private static boolean doesTableHaveColumn(Connection connection, String table, String column) {

        try {

            String query =
                    "SELECT COUNT(*) as RowCount FROM information_schema.columns " +
                            "WHERE TABLE_SCHEMA = database() AND " +
                            "TABLE_NAME = '" + table + "' AND " +
                            "COLUMN_NAME = '" + column + "';";

            Statement statement = connection.createStatement();
            ResultSet res = statement.executeQuery(query);
            while (res.next()) {
                int colCount = res.getInt("RowCount");
                if (colCount > 0)
                    return true;
            }
            statement.close();
            return false;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalStateException();
        }
    }

}
