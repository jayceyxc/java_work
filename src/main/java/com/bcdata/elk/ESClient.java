package com.bcdata.elk;

import com.bcdata.utils.IniReader;
import com.carrotsearch.hppc.cursors.ObjectObjectCursor;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequestBuilder;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.ClusterAdminClient;
import org.elasticsearch.client.ElasticsearchClient;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.support.AbstractClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.elasticsearch.cluster.health.ClusterIndexHealth;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.io.stream.DataOutputStreamOutput;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

public class ESClient {

    private static final Logger logger = Logger.getLogger (ESClient.class);

    private static IniReader config = null;

    public static void displayHealthy () {
        try {
            String esHost = config.getValue (IniReader.ES_SECTION, IniReader.ES_HOST_PROP);
            int esPort = config.getIntValue (IniReader.ES_SECTION, IniReader.ES_PORT_PROP);
//            String[] ipStr = esHost.split("\\.");
//            byte[] ipBuf = new byte[4];
//            for(int i = 0; i < 4; i++){
//                ipBuf[i] = (byte)(Integer.parseInt(ipStr[i])&0xff);
//            }
            logger.info ("connect to the ES server: " + StringUtils.join (esHost));
            TransportClient client = new PreBuiltTransportClient (Settings.builder ().put ("cluster.name", "yinni").put("node.name","node-log").build ())
                    .addTransportAddress (new InetSocketTransportAddress (InetAddress.getByName (esHost), esPort));
            ClusterAdminClient clusterAdminClient = client.admin().cluster();
            ClusterHealthResponse healthResponse = clusterAdminClient.prepareHealth ().get (TimeValue.timeValueSeconds (10));
            String clusterName = healthResponse.getClusterName();
            int numberOfDataNodes = healthResponse.getNumberOfDataNodes();
            int numberOfNodes = healthResponse.getNumberOfNodes();
            ClusterHealthStatus clusterHealthStatus = healthResponse.getStatus ();
            logger.info ("cluster name: " + clusterName);
            logger.info ("cluster data node number: " + numberOfDataNodes);
            logger.info ("cluster node number: " + numberOfNodes);
            logger.info ("cluster health status: " + clusterHealthStatus);

            for (ClusterIndexHealth health : healthResponse.getIndices().values()) {
                String index = health.getIndex();
                int numberOfShards = health.getNumberOfShards();
                int numberOfReplicas = health.getNumberOfReplicas();
                ClusterHealthStatus status = health.getStatus();
                logger.info ("index: " + index);
                logger.info ("numberOfShards: " + numberOfShards);
                logger.info ("numberOfReplicas: " + numberOfReplicas );
                logger.info ("status: " + status);
            }
            client.close ();
        } catch (UnknownHostException uhe) {
            logger.error (uhe);
        }
    }

    private static Set<String> queryDocType(String indexName, String docType) {
        Set<String> docTypeSet = new HashSet<> ();
        try {
            String esHost = config.getValue (IniReader.ES_SECTION, IniReader.ES_HOST_PROP);
            int esPort = config.getIntValue (IniReader.ES_SECTION, IniReader.ES_PORT_PROP);

            logger.info ("connect to the ES server: " + StringUtils.join (esHost));
            TransportClient client = new PreBuiltTransportClient (Settings.builder ().put ("cluster.name", "yinni").put("node.name","node-log").build ())
                    .addTransportAddress (new InetSocketTransportAddress (InetAddress.getByName (esHost), esPort));

            SearchResponse response = client.prepareSearch (indexName).setSearchType (SearchType.QUERY_THEN_FETCH).setTypes (docType).setScroll (new TimeValue (600000)).execute ().actionGet ();
            while (true) {
                for (SearchHit hit : response.getHits ().getHits ()) {
                    logger.info ("type: " + hit.getType () + ", id: " + hit.getId ());
                    docTypeSet.add (hit.getType ());
                }
                response = client.prepareSearchScroll(response.getScrollId()).setScroll(new TimeValue(600000))
                        .execute().actionGet();
                if (response.getHits ().getHits ().length == 0) {
                    break;
                }
            }
            client.close ();
            return docTypeSet;
        } catch (UnknownHostException uhe) {
            logger.error (uhe);
        }

        return docTypeSet;
    }

    private static Set<String> queryAllTypes(String indexName) {
        Set<String> docTypeSet = new HashSet<> ();
        try {
            String esHost = config.getValue (IniReader.ES_SECTION, IniReader.ES_HOST_PROP);
            int esPort = config.getIntValue (IniReader.ES_SECTION, IniReader.ES_PORT_PROP);

            logger.info ("connect to the ES server: " + StringUtils.join (esHost));
            TransportClient client = new PreBuiltTransportClient (Settings.builder ().put ("cluster.name", "yinni").put("node.name","node-log").build ())
                    .addTransportAddress (new InetSocketTransportAddress (InetAddress.getByName (esHost), esPort));

            IndicesAdminClient indicesAdminClient = client.admin ().indices ();
            GetMappingsResponse mappingsResponse = indicesAdminClient.prepareGetMappings ("bidder").get ();

            ImmutableOpenMap<String, MappingMetaData> mappingMap = mappingsResponse.getMappings ().get (indexName);
            for (ObjectObjectCursor<String, MappingMetaData> mapping : mappingMap) {
                docTypeSet.add (mapping.key);
                logger.debug (mapping.key + ": " + mapping.value.source ());
            }
            client.close ();
            return docTypeSet;
        } catch (UnknownHostException uhe) {
            logger.error (uhe);
        }

        return docTypeSet;
    }

    public static void main (String[] args) {
        PropertyConfigurator.configure ("conf/log4j.properties");
        config = IniReader.getInstance (IniReader.CONFIG_FILE);
        if (config == null) {
            logger.error ("read configuration file failed");
            return;
        }
        displayHealthy ();
//        Set<String> docTypes = queryDocType("bidder", "rtb_log_2018_03_31");
//        for (String docType : docTypes) {
//            logger.info ("doc type: " + docType);
//        }

        Set<String> docTypes = queryAllTypes ("bidder");
        for (String docType : docTypes) {
            System.out.println (docType);
        }
    }
}
