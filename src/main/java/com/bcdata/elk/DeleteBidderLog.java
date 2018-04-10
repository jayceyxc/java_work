package com.bcdata.elk;

import com.bcdata.utils.IniReader;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class DeleteBidderLog {

    private static final Logger log = Logger.getLogger (DeleteBidderLog.class);

    private static long deleteIndex(String host, int port, String docType) {
        try {
            TransportClient client = new PreBuiltTransportClient (Settings.builder ().put ("cluster.name", "yinni").put ("node.name", "node-log").build ())
                    .addTransportAddress (new InetSocketTransportAddress (InetAddress.getByName (host), port));
            BulkByScrollResponse response = DeleteByQueryAction.INSTANCE.newRequestBuilder (client)
//                    .filter (QueryBuilders.termQuery ("_index", "bidder"))
                    .filter (QueryBuilders.typeQuery (docType))
                    .source ("bidder").get ();
            return response.getDeleted ();
        } catch (UnknownHostException uhe) {
            log.error (uhe);
        }

        return -1;
    }

    public static void main (String[] args) {
        PropertyConfigurator.configure ("conf/log4j.properties");
        IniReader config = IniReader.getInstance (IniReader.CONFIG_FILE);
        if (config == null) {
            log.error ("read configuration file failed");
            return;
        }

        Options options = new Options ();
        options.addOption ("d", "doctype", true, "The day of log, for example rtb_log_2017_10_11");

        CommandLine cl = null;
        String docType = null;
        try {
            cl = new DefaultParser ().parse (options, args);
        } catch (ParseException e) {
            log.error (e);
        }
        if (cl == null || !cl.hasOption ("d")) {
            new HelpFormatter ().printHelp ("java [options] -f [logs]", options);
            return;
        } else {
            docType = cl.getOptionValue ("doctype");
        }

        String esHost = config.getValue (IniReader.ES_SECTION, IniReader.ES_HOST_PROP);
        int esPort = config.getIntValue (IniReader.ES_SECTION, IniReader.ES_PORT_PROP);

        long result = deleteIndex(esHost, esPort, docType);
        while (result != 0) {
            log.info ("deleted records: " + result);
            result = deleteIndex (esHost, esPort, docType);
        }
    }
}
