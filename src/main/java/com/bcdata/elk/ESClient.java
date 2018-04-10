package com.bcdata.elk;

import com.bcdata.utils.IniReader;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.client.ClusterAdminClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.elasticsearch.cluster.health.ClusterIndexHealth;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

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

    public static void main (String[] args) {
        PropertyConfigurator.configure ("conf/log4j.properties");
        config = IniReader.getInstance (IniReader.CONFIG_FILE);
        if (config == null) {
            logger.error ("read configuration file failed");
            return;
        }
        displayHealthy ();
    }
}
