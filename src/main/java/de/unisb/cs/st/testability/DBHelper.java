package de.unisb.cs.st.testability;

import org.apache.log4j.Logger;

import java.sql.*;
import java.util.List;

/**
 * Created by Yanchuan Li
 * Date: 2/11/11
 * Time: 10:55 PM
 */
public class DBHelper {


    private static PreparedStatement insert;
    private static PreparedStatement remove;
    private static Connection connection = null;
    private static boolean enable = false;
    private static Logger log = Logger.getLogger(DBHelper.class);

    static {
        String tempStr = System.getProperty("ENABLE_STATISTICS");
        if (tempStr != null && tempStr.equalsIgnoreCase("true")) {
            enable = true;
        }

        if (enable) {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }


            try {
                // create a database connection
                connection = DriverManager.getConnection("jdbc:sqlite:statistics.db");

                //check if table Statistics exists.
                //if not, create one
                Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery("SELECT count(*) FROM sqlite_master WHERE type='table' AND name='Statistics';");
                int exist = 0;
                while (rs.next()) {
                    exist = rs.getInt(1);
                }

                if (exist == 0) {
                    statement.executeUpdate("create table Statistics (classname string, methodsignature string, event string, integer count)");
                }

                //compile PreparedStatement for faster batch operation
                insert = connection.prepareStatement("insert into Statistics values(?,?,?,?);");
                remove = connection.prepareStatement("delete from Statistics where classname=?");
            } catch (SQLException e) {
                e.printStackTrace();
            }

            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    try {
                        if (connection != null)
                            connection.close();
                    } catch (SQLException e) {
                        System.err.println(e);
                    }
                }
            });

        }
    }

    public static void writeToDB(List<Transformation> records) {
        if (enable) {
            try {
                if (records.size() > 0) {
                    Transformation t = records.get(0);
                    remove.setString(1, t.getClassname());
                }
                remove.execute();

                for (Transformation t : records) {

                    insert.setString(1, t.getClassname());
                    insert.setString(2, t.getMethodname());
                    insert.setString(3, t.getEvent());
                    insert.setInt(4, t.getCount());
                    insert.addBatch();

                }
                insert.executeBatch();
            } catch (SQLException e) {
                e.printStackTrace();
            }


        }
    }


}
