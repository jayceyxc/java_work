package com.bcdata.elk;

import com.bcdata.utils.DateUtils;
import com.bcdata.utils.IniReader;
import com.bcdata.utils.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.*;

public class IndexBidderLog {

    private static final Logger log = Logger.getLogger (IndexBidderLog.class);

    private static final String SEPARATOR = "\u0001";

    private static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static String ES_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS+0800";

    private static int parseErrorNum = 0;
    private static int pushidDupNum = 0;
    private static int userIDNullNum = 0;
    private static int bulkFailedNum = 0;

//    private static String mysqlHost = "10.54.8.89";
//    private static int mysqlPort = 3306;
//    private static String mysqlUser = "root";
//    private static String mysqlPassword = "Mypassword@2qq";
//    private static String rmcDBName = "rmc";
//    private static String adDBName = "adp";
//
//    private static String esHost = "10.54.8.71";
//    private static int esPort = 9300;

    private static Map<Integer, AdInfo> adInfoMap = null;
    private static Map<Integer, CityInfo> cityInfoMap = null;

    private static Map<String, AdDetail> showDetails = new ConcurrentHashMap<> (100000);
    private static BlockingQueue<AdDetail> flushQueue = new LinkedBlockingQueue<> (100000);

//    private static ReentrantLock reentrantLock = new ReentrantLock ();

    private static long currLogTime = 0;

    private static boolean parseFinished = false;

    private static final String BIDDER_INDEX = "bidder";

//    private static IniReader config = null;

    private static String getNetwork (String net) {
        switch (net) {
            case "3":
                return "3G";
            case "4":
                return "4G";
            default:
                return "Unknown";
        }
    }


    private static void parseFile (File file) {
        try {
            BufferedReader reader = new BufferedReader (new FileReader (file));
            String line;
            while ((line = reader.readLine ()) != null) {
                log.debug ("read line: " + line);
                String[] tokens = line.split (SEPARATOR);
                for (int index = 0; index < tokens.length; index++) {
                    tokens[index] = tokens[index].trim ();
                }

                if (tokens.length < 11) {
                    log.debug ("invalid line");
                    parseErrorNum++;
                    continue;
                }
                try {
                    String time = tokens[LogIndex.TIME_INDEX.getValue ()];
                    currLogTime = DateUtils.dateToTimeStamp (time, TIME_FORMAT);
                } catch (IndexOutOfBoundsException ioobe) {
                    log.error (ioobe);
                    parseErrorNum++;
                    continue;
                }
                String logKey = tokens[LogIndex.LOG_KEY_INDEX.getValue ()];
                String pushID = tokens[LogIndex.PUSH_ID_INDEX.getValue ()];
                if (logKey.equals ("rtb_creative")) {
                    if (tokens.length < 41) {
                        log.info ("wrong creative log: " + line);
                        parseErrorNum++;
                        continue;
                    }

                    String userID = tokens[LogIndex.USER_INDEX.getValue ()];
                    String ip = tokens[LogIndex.IP_INDEX.getValue ()];
                    if (userID.isEmpty ()) {
                        userIDNullNum++;
                    }

                    if (showDetails.containsKey (pushID)) {
                        pushidDupNum++;
                        showDetails.get (pushID).setUid (tokens[LogIndex.USER_INDEX.getValue ()]);
                        showDetails.get (pushID).setAid (Integer.valueOf (tokens[LogIndex.AD_ID_INDEX.getValue ()]));
                        showDetails.get (pushID).setShow (0);
                        showDetails.get (pushID).setClick (0);
                    } else {
                        String host = tokens[LogIndex.HOST_INDEX.getValue ()];
                        String domain = Utils.hostToDomain (host);
                        String url = tokens[LogIndex.URL_INDEX.getValue ()];
                        String userAgent = tokens[LogIndex.UA_INDEX.getValue ()];
                        int cityID = -1;
                        int adID = -1;
                        int campaignID = -1;
                        int policyID = -1;
                        String cityName = "";
                        String provinceName = "";
                        String net = "";
                        try {
                            net = getNetwork (tokens[LogIndex.NET_INDEX.getValue ()]);
                            String cityIDToken = tokens[LogIndex.CITY_INDEX.getValue ()];
                            if (!cityIDToken.isEmpty () && StringUtils.isNumeric (cityIDToken)) {
                                cityID = Integer.valueOf (cityIDToken);
                            }
                            String adIDToken = tokens[LogIndex.AD_ID_INDEX.getValue ()];
                            if (!adIDToken.isEmpty () && StringUtils.isNumeric (adIDToken)) {
                                adID = Integer.valueOf (adIDToken);
                            }
                            if (adID > 0 && adInfoMap.containsKey (adID)) {
                                campaignID = adInfoMap.get (adID).getCampaignID ();
                                policyID = adInfoMap.get (adID).getPolicyID ();
                            }
                            if (cityID > 0 && cityInfoMap.containsKey (cityID)) {
                                cityName = cityInfoMap.get (cityID).getCityName ();
                                provinceName = cityInfoMap.get (cityID).getProvinceName ();
                            }
                        } catch (IndexOutOfBoundsException ioobe) {
                            log.error (ioobe);
                        }

                        AdDetail adDetail = new AdDetail ();
                        adDetail.setUid (userID);
                        adDetail.setIp (ip);
                        adDetail.setAid (adID);
                        adDetail.setCampaign (campaignID);
                        adDetail.setPolicy (policyID);
                        adDetail.setCity_id (cityID);
                        adDetail.setCity (cityName);
                        adDetail.setProvince (provinceName);
                        adDetail.setSp (tokens[LogIndex.SP_INDEX.getValue ()]);
                        adDetail.setNetwork (net);
                        adDetail.setTime (DateUtils.timeStampToDate (currLogTime, ES_TIME_FORMAT));
                        adDetail.setTimeVal (currLogTime);
                        adDetail.setDomain (domain);
                        adDetail.setHost (host);
                        adDetail.setUrl (url);
                        adDetail.setAgent (userAgent);
                        adDetail.setBidder (tokens[LogIndex.BIDDER_INDEX.getValue ()]);
                        adDetail.setPushid (pushID);
                        adDetail.setPrice (Double.valueOf (tokens[LogIndex.PRICE_INDEX.getValue ()]));
                        showDetails.put (pushID, adDetail);
                        log.debug (adDetail);
                        log.debug ("add to show details: " + pushID);
                    }
                } else if (logKey.equals ("rtb_show") && showDetails.containsKey (pushID)) {
                    showDetails.get (pushID).setShow (1);
                } else if (logKey.equals ("rtb_click") && showDetails.containsKey (pushID)) {
                    AdDetail adDetail = showDetails.remove (pushID);
                    if (adDetail != null) {
                        try {
                            adDetail.setClick (1);
                            flushQueue.put (adDetail);
                        } catch (InterruptedException ie) {
                            log.error (ie);
                        }
                    }
                }

            }
        } catch (FileNotFoundException fnfe) {
            log.error (file.getAbsolutePath () + " is not exists");
        } catch (IOException ioe) {
            log.error (String.format ("read file %s failed", file.getAbsoluteFile ()));
        }
    }

    public static void main (String[] args) {
        PropertyConfigurator.configure ("conf/log4j.properties");
        IniReader config = IniReader.getInstance (IniReader.CONFIG_FILE);
        if (config == null) {
            log.error ("read configuration file failed");
            return;
        }

        Options options = new Options ();
        options.addOption ("d", "directory", true, "The directory name of bidder log");
        options.addOption ("t", "day", true, "The day of bidder log");

        CommandLine cl = null;
        String directory = null;
        String day = null;
        try {
            cl = new DefaultParser ().parse (options, args);
        } catch (ParseException e) {
            log.error (e);
        }
        if (cl == null || !cl.hasOption ("d") || !cl.hasOption ("t")) {
            new HelpFormatter ().printHelp ("java [options] -f [logs]", options);
        } else {
            directory = cl.getOptionValue ("directory");
            day = cl.getOptionValue ("day");
        }

        String mysqlHost = config.getValue (IniReader.MYSQL_SECTION, IniReader.MYSQL_HOST_PROP);
        int mysqlPort = config.getIntValue (IniReader.MYSQL_SECTION, IniReader.MYSQL_PORT_PROP);
        String mysqlUser = config.getValue (IniReader.MYSQL_SECTION, IniReader.MYSQL_USER_PROP);
        String mysqlPassword = config.getValue (IniReader.MYSQL_SECTION, IniReader.MYSQL_PASSWORD_PROP);
        String rmcDBName = config.getValue (IniReader.MYSQL_SECTION, IniReader.MYSQL_RMC_DB_PROP);
        String adDBName = config.getValue (IniReader.MYSQL_SECTION, IniReader.MYSQL_AD_DB_PROP);

        String esHost = config.getValue (IniReader.ES_SECTION, IniReader.ES_HOST_PROP);
        int esPort = config.getIntValue (IniReader.ES_SECTION, IniReader.ES_PORT_PROP);

        String customTimeFormat = config.getValue (IniReader.SYSTEM_SECTION, IniReader.SYSTEM_TIME_FORMAT_PROP);
        if (!customTimeFormat.isEmpty ()) {
            log.info ("Use custom time format: " + customTimeFormat);
            ES_TIME_FORMAT = customTimeFormat;
        }

        adInfoMap = DBUtils.queryAdInfo (mysqlHost, mysqlPort, mysqlUser, mysqlPassword, adDBName);
        cityInfoMap = DBUtils.queryCityInfo (mysqlHost, mysqlPort, mysqlUser, mysqlPassword, rmcDBName);

        log.info ("read ad info finished, length: " + adInfoMap.size ());
        log.info ("read city province info finished, length: " + cityInfoMap.size ());

        ExecutorService executorService = Executors.newFixedThreadPool (2);
        Future<Integer> timerResult = executorService.submit (new TimerCallable (60));
        Future<Integer> flushResult = executorService.submit (new FlushCallable (esHost, esPort, 30));
        executorService.shutdown ();

        directory = StringUtils.stripEnd (directory, File.separator);
        String prefix = StringUtils.join (Arrays.asList (directory, day), File.separator);
        String fullFileName;
        for (int hour = 0; hour < 24; hour++) {
            for (int minute = 0; minute < 60; minute++) {
                fullFileName = prefix + File.separator + String.format ("rtb_log_crit_%s%02d%02d.log", day, hour, minute);
                File file = new File (fullFileName);
                if (file.exists ()) {
                    if (showDetails.size () > 500000) {
                        log.info ("The show details size is too big");
                        try {
                            TimeUnit.SECONDS.sleep (30);
                        } catch (InterruptedException ie) {
                            log.error (ie);
                        }
                    }
                    parseFile (file);
                    log.info ("finished parse file: " + fullFileName);
                } else {
                    log.error (String.format ("%s is not exists", fullFileName));
                }
            }
        }

        parseFinished = true;
        log.warn ("parse log finished");

        while (true) {
            try {
                executorService.awaitTermination (600, TimeUnit.SECONDS);
                if (executorService.isTerminated ()) {
                    log.info ("All tasks has completed");
                    if (timerResult.get () == 0) {
                        log.info ("Timer thread complete normally");
                    }
                    if (flushResult.get () == 0) {
                        log.info ("flush thread complete normally");
                    }
                    break;
                }
            } catch (InterruptedException ie) {
                log.error (ie);
            } catch (ExecutionException ee) {
                log.error (ee);
            }
        }

        log.warn (String.format ("parse_error_num: %d, bulk_failed_num: %d, push_id_dup_num: %d, user_id_null_num: %d",
                parseErrorNum, bulkFailedNum, pushidDupNum, userIDNullNum));
    }

    private static class TimerCallable implements Callable<Integer> {

        private final Logger logger = Logger.getLogger (TimerCallable.class);

        private static final int SLEEP_SECONDS = 10;

        private String name;
        private int expireSeconds;

        private TimerCallable (int expireSeconds) {
            this.expireSeconds = expireSeconds;
            this.name = "Timer Thread";
        }

        @Override
        public Integer call () {
            logger.info ("timer thread start");
            while (true) {
                try {
                    TimeUnit.SECONDS.sleep (SLEEP_SECONDS);
                    logger.info (this.name + " sleep finished");
                    logger.info ("show Details size: " + showDetails.size ());
                    for (Map.Entry<String, AdDetail> entry : showDetails.entrySet ()) {
                        if (currLogTime - entry.getValue ().getTimeVal () > expireSeconds || parseFinished) {
                            logger.debug ("Add to flush Queue: " + entry.getKey ());
                            flushQueue.put (showDetails.get (entry.getKey ()));
                            showDetails.remove (entry.getKey ());
                        }
                    }
                } catch (InterruptedException ie) {
                    logger.error (ie);
                }
                if (parseFinished && showDetails.size () == 0) {
                    logger.info ("flush show detail finished. " + this.name + " exited.");
                    break;
                }
            }

            return 0;
        }
    }

    private static class FlushCallable implements Callable<Integer> {

        private final Logger logger = Logger.getLogger (FlushCallable.class);

        private static final int SLEEP_SECONDS = 10;

        private TransportClient client;
        private String name;
        private int interval;
        private int bulkNumber = 0;
        private String host;
        private int port;

        private FlushCallable (String host, int port, int interval) {
            this.name = "Flush Thread";
            this.interval = interval;
            this.host = host;
            this.port = port;
            initESClient ();
        }

        private void initESClient () {
            try {
                this.client = new PreBuiltTransportClient (Settings.builder ().put ("cluster.name", "yinni").put ("node.name", "node-log").build ())
                        .addTransportAddress (new InetSocketTransportAddress (InetAddress.getByName (this.host), this.port));
            } catch (UnknownHostException uhe) {
                logger.error (uhe);
            }
        }


        @Override
        public Integer call () {
            while (true) {
                try {
                    logger.info (this.name + " start to flush records");
                    logger.info ("flush queue size: " + flushQueue.size ());
                    if (flushQueue.size () < 1000 && !parseFinished) {
                        TimeUnit.SECONDS.sleep (SLEEP_SECONDS);
                    }
                    BulkRequestBuilder bulkRequest = client.prepareBulk ();
                    while (!flushQueue.isEmpty ()) {
                        AdDetail adDetail = flushQueue.poll (10, TimeUnit.SECONDS);
                        if (adDetail != null) {
                            String docType = "rtb_log_" + DateUtils.timeStampToDate (currLogTime, "yyyy_MM_dd");
                            if (adDetail.getTimeVal () > 0) {
                                docType = "rtb_log_" + DateUtils.timeStampToDate (adDetail.getTimeVal (), "yyyy_MM_dd");
                            }
                            ObjectMapper mapper = new ObjectMapper ();
                            try {
                                String value = mapper.writeValueAsString (adDetail);
                                /**
                                 * {"uid":"047202596629","adID":10850,"ip":"1.28.255.45","campaignID":-1,"policyID":-1,"cityID":13923089,"cityName":"","provinceName":"","spName":"301","network":"Unknown","time":"2018-03-30 08:03:00.000+0700","timeVal":1522369096,"domain":"coohua.com","host":"www.coohua.com","url":"www.coohua.com","userAgent":"Mozilla/5.0 (Linux; Android 4.4.4; A31 Build/KTU84P; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/57.0.2987.132 MQQBrowser/6.2 TBS/043909 Mobile Safari/537.36","processHost":"localhost.localdomain","pushID":"b450c90e853a41c9e84ec6bd148c7693","lacci":null,"hasShow":true,"hasClick":true,"price":5.0}
                                 */
                                logger.debug ("value: " + value);
                                bulkRequest.add (client.prepareIndex (BIDDER_INDEX, docType).setSource (value, XContentType.JSON));
                                bulkNumber++;
                            } catch (JsonProcessingException jpe) {
                                logger.error (adDetail.getPushid (), jpe);
                            }
                        }
                        if ((bulkNumber == 1000) || (parseFinished && flushQueue.isEmpty ())) {
                            BulkResponse bulkResponse = bulkRequest.get ();
                            if (bulkResponse.hasFailures ()) {
                                logger.error (bulkResponse.buildFailureMessage ());
                                for (BulkItemResponse response : bulkResponse.getItems ()) {
                                    if (response.isFailed ()) {
                                        bulkFailedNum++;
                                        logger.error (response.getFailureMessage ());
                                    }
                                }
                            } else {
                                logger.info ("bulk success. status: " + bulkResponse.status ());
                            }
                            bulkNumber = 0;
                            bulkRequest = client.prepareBulk ();
                        }
                    }
                } catch (InterruptedException ie) {
                    logger.error (ie);
                }

                if (parseFinished && showDetails.size () == 0 && flushQueue.size () == 0) {
                    logger.info ("flush finished. flush_func exit");
                    break;
                }
            }
            return 0;
        }
    }
}
