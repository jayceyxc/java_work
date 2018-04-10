package com.bcdata.elk;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DBUtils {

    private static final Logger log = LogManager.getLogger (ESClient.class);

    //驱动程序名
    private static String driver = "com.mysql.cj.jdbc.Driver";

    private static int MAX_RETRY_TIMES = 3;
    private static String url = "jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=UTF-8&useFastDateParsing=false&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=Asia/Jakarta";

    private static Connection connectMysql (String host, int port, String user, String password, String dbName) {
        Connection conn = null;
        for (int i = 0; i < MAX_RETRY_TIMES; i++) {
            try {
                Class.forName (driver);
                if (password.length () > 0 || user.length () > 0) {
                    conn = DriverManager.getConnection (String.format (url, host, port, dbName), user, password);
                } else {
                    conn = DriverManager.getConnection (String.format (url, host, port, dbName));
                }
                return conn;
            } catch (SQLException sqle) {
                log.error ("Failed to connect to " + String.format (url, host, port, dbName) + " for " + (i + 1) + " times.", sqle);
                try {
                    TimeUnit.SECONDS.sleep (10);
                } catch (InterruptedException ie) {
                    log.warn (ie);
                }
            } catch (ClassNotFoundException cnfe) {
                log.error ("The driver class is not found");
                log.error (cnfe);
            }
        }
        return conn;
    }

    public static Map<Integer, AdInfo> queryAdInfo(String host, int port, String user, String password, String dbName) {
        Map<Integer, AdInfo> results = new HashMap<> ();
        // 连接mysql数据库
        Connection connection = connectMysql (host, port, user, password, dbName);
        //创建statement类对象，用来执行SQL语句
        try {
            PreparedStatement statement = connection.prepareStatement ("select adid,group_id,plan_id from adp_ad_info;");
            boolean success = statement.execute ();
            if (success) {
                ResultSet resultSet = statement.getResultSet ();
                while (resultSet.next ()) {
                    String adID = resultSet.getString ("adid");
                    int groupID = resultSet.getInt ("group_id");
                    int planID = resultSet.getInt ("plan_id");
                    AdInfo adInfo = new AdInfo (planID, groupID);
                    results.put (Integer.valueOf (adID), adInfo);
                }
                return results;
            }
        } catch (SQLException sqle) {
            log.error (sqle);
        }

        return null;
    }

    public static Map<Integer, CityInfo> queryCityInfo(String host, int port, String user, String password, String dbName) {
        Map<Integer, CityInfo> results = new HashMap<> ();

        // 连接mysql数据库
        Connection connection = connectMysql (host, port, user, password, dbName);
        //创建statement类对象，用来执行SQL语句
        try {
            PreparedStatement statement = connection.prepareStatement ("select id, parent_id, area_name, level, region_name from rmc_area;");
            boolean success = statement.execute ();
            if (success) {
                ResultSet resultSet = statement.getResultSet ();
                while (resultSet.next ()) {
                    int cityID = resultSet.getInt("id");
                    int proviceID = resultSet.getInt("parent_id");
                    String cityName = resultSet.getString ("area_name");
                    int level = resultSet.getInt ("level");
                    String provinceName = resultSet.getString ("region_name");

                    if (level == 0) {
                        continue;
                    }

                    CityInfo cityInfo = new CityInfo (cityName, provinceName);
                    results.put (cityID, cityInfo);
                }
                return results;
            }
        } catch (SQLException sqle) {
            log.error (sqle);
        }

        return results;
    }
}
