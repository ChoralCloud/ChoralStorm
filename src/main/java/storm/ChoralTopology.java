package storm;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.kafka.*;
import org.apache.storm.redis.common.config.JedisPoolConfig;
import org.apache.storm.spout.SchemeAsMultiScheme;
import org.apache.storm.topology.TopologyBuilder;

public class ChoralTopology {

    public static boolean local = false;
    public static String psRemoteHost = "172.18.2.104";

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: ChoralTopology <TOPIC_NAME> [local]");
            return;
        }

        String topicName = args[0];
        if (args.length >= 2) {
            local = args[1].equals("local");
        }

        System.out.println("Topic: " + topicName + " on local: " + local);

        //region Kafka spout creation
        KafkaSpout kafkaSpout = null;
        try {
            String zookeeperHost = local ? "localhost:2181" : "zookeeper:2181";
            BrokerHosts zooKeeperHosts = new ZkHosts(zookeeperHost);
            String spoutId = "choraldatastreamSpout";
            SpoutConfig spoutConfig = new SpoutConfig(zooKeeperHosts, topicName, "/" + topicName, spoutId);
            spoutConfig.scheme = new SchemeAsMultiScheme(new StringScheme());
            spoutConfig.startOffsetTime = kafka.api.OffsetRequest.LatestTime();
            kafkaSpout = new KafkaSpout(spoutConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //endregion

        //region Redis creation
        JedisPoolConfig poolConfig = null;
        try {
            String redisHost = local ? "localhost" : psRemoteHost;
            poolConfig = new JedisPoolConfig.Builder().setHost(redisHost).setPort(6379).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //endregion

        /*
        Topology

        kafka
        |-> cassandraBolt
        |-> choralAverageQuery
        |   |-> RedisAverageQueryBolt
        |   |-> elasticSearchAverageQueryBolt
        |-> redisBolt
        |-> elasticSearchBolt
         */

        //region Topology creation
        TopologyBuilder topologyBuilder = new TopologyBuilder();
        try {
            // kafka emits tuple(device_id, device_data, device_timestamp)
            topologyBuilder.setSpout("kafkaSpout", kafkaSpout);

            // cassandraBolt emits tuple(device_id, device_data, device_timestamp)
            topologyBuilder.setBolt("cassandraBolt", new CassandraBolt())
                    .shuffleGrouping("kafkaSpout");
            // choralAverageQuery emits tuple(device_id, device_function, device_value)
            topologyBuilder.setBolt("choralAverageQueryBolt", new ChoralAverageQueryBolt())
                    .shuffleGrouping("kafkaSpout");

            // redisBolt emits tuple(device_id, device_data, device_timestamp)
            topologyBuilder.setBolt("redisBolt", new RedisBolt(poolConfig))
                    .shuffleGrouping("kafkaSpout");
            // redisAverageQueryBolt emits tuple(device_id, device_function, device_value)
            topologyBuilder.setBolt("redisAverageQueryBolt", new RedisAverageQueryBolt(poolConfig))
                    .shuffleGrouping("choralAverageQueryBolt");

            // elasticsearchBolt emits tuple(device_id, device_data, device_timestamp)
            topologyBuilder.setBolt("elasticsearchBolt", new ElasticsearchBolt())
                    .shuffleGrouping("kafkaSpout");
            // elasticsearchAverageQueryBolt emits tuple(device_id, device_function, device_value)
            topologyBuilder.setBolt("elasticsearchAverageQueryBolt", new ElasticsearchAverageQueryBolt())
                    .shuffleGrouping("choralAverageQueryBolt");

        } catch (Exception e) {
            e.printStackTrace();
        }
        //endregion
        if (local) {
            //region Local cluster
            try {
                Config config = new Config();
                //config.setDebug(true);
                LocalCluster localCluster = new LocalCluster();
                localCluster.submitTopology("ChoralTopology", config, topologyBuilder.createTopology());
            } catch (Exception e) {
                e.printStackTrace();
            }
            //endregion
        } else {
            //region Remote cluster
            Config remoteClusterConfig = new Config();
            remoteClusterConfig.setMessageTimeoutSecs(20);

            try {
                StormSubmitter.submitTopology("ChoralTopology", remoteClusterConfig, topologyBuilder.createTopology());
            } catch (Exception e) {
                e.printStackTrace();
            }
            //endregion
        }
    }
}
