package com.bcdata.elk;

import com.bcdata.utils.IniReader;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ShardOperationFailedException;
import org.elasticsearch.action.admin.indices.forcemerge.ForceMergeResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ForceMerge {
    private static final Logger logger = Logger.getLogger (ESClient.class);

    private static IniReader config = null;

    public static void main (String[] args) {
        PropertyConfigurator.configure ("conf/log4j.properties");
        config = IniReader.getInstance (IniReader.CONFIG_FILE);
        if (config == null) {
            logger.error ("read configuration file failed");
            return;
        }

        Options options = new Options ();
        options.addOption ("i", "index", true, "The index to force merge, for example bidder");

        CommandLine cl = null;
        String indexName = null;
        try {
            cl = new DefaultParser ().parse (options, args);
        } catch (ParseException e) {
            logger.error (e);
        }
        if (cl == null || !cl.hasOption ("i")) {
            new HelpFormatter ().printHelp ("java [options] -i [index]", options);
            return;
        } else {
            indexName = cl.getOptionValue ("index");
        }

        try {
            String esHost = config.getValue (IniReader.ES_SECTION, IniReader.ES_HOST_PROP);
            int esPort = config.getIntValue (IniReader.ES_SECTION, IniReader.ES_PORT_PROP);

            logger.info ("connect to the ES server: " + StringUtils.join (esHost));
            TransportClient client = new PreBuiltTransportClient (Settings.builder ().put ("cluster.name", "yinni").put("node.name","node-log").build ())
                    .addTransportAddress (new InetSocketTransportAddress (InetAddress.getByName (esHost), esPort));

            ForceMergeResponse forceMergeResponse = client.admin ().indices ().prepareForceMerge (indexName).setOnlyExpungeDeletes (true).setFlush (true).get ();

            int totalShards = forceMergeResponse.getTotalShards();
            int successfulShards = forceMergeResponse.getSuccessfulShards();
            int failedShards = forceMergeResponse.getFailedShards();
            ShardOperationFailedException[] failures = forceMergeResponse.getShardFailures();
            logger.info ("totalShards: " + totalShards);
            logger.info ("successfulShards: " + successfulShards);
            logger.info ("failedShards: " + failedShards);
            for (ShardOperationFailedException failedException : failures) {
                logger.info (failedException);
            }

            client.close ();
        } catch (UnknownHostException uhe) {
            logger.error (uhe);
        } catch (ElasticsearchException exception) {
            if (exception.status () == RestStatus.NOT_FOUND) {
                logger.error ("The specified index is not exist");
            }
        }

    }
}
