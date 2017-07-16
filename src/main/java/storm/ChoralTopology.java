package storm;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.kafka.*;
import org.apache.storm.redis.common.config.JedisPoolConfig;
import org.apache.storm.spout.SchemeAsMultiScheme;
import org.apache.storm.topology.TopologyBuilder;

public class ChoralTopology {

    public static void main(String[] args) {

        boolean local = false;
        boolean cluster = false;
        String psRemoteHost = "172.18.2.104";

        if (args.length < 1) {
            System.out.println("Usage: ChoralTopology <TOPIC_NAME> [local] [cluster]");
            return;
        }

        String topicName = args[0];
        if (args.length >= 2) {
            local = args[1].equals("local");
        }

        if (args.length >= 3) {
            cluster = args[2].equals("cluster");
        }

        System.out.println("Topic: " + topicName + " on local: " + local + " on cluster: " + cluster);

        //region Kafka spout creation
        KafkaSpout kafkaSpout = null;
        try {
            String zookeeperHost = (local && !cluster) ? "localhost:2181" : "zookeeper:2181";
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
            String redisHost;
            if (local && !cluster) redisHost = "localhost";
            else if (local) redisHost = "redis";
            else redisHost = psRemoteHost;
            poolConfig = new JedisPoolConfig.Builder().setHost(redisHost).setPort(6379).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //endregion

        //region Cassandra config
        String cassandraHost;
        if (local && !cluster) cassandraHost = "localhost";
        else if (local) cassandraHost = "cassandra";
        else cassandraHost = psRemoteHost;
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

        Config conf = new Config();

        //region Topology creation
        TopologyBuilder topologyBuilder = new TopologyBuilder();
        try {
            // kafka emits tuple(device_id, device_data, device_timestamp)
            topologyBuilder.setSpout("kafkaSpout", kafkaSpout);

            // cassandraBolt emits tuple(device_id, device_data, device_timestamp)
            topologyBuilder.setBolt("cassandraBolt", new CassandraBolt(cassandraHost))
                    .shuffleGrouping("kafkaSpout");
            // choralAverageQuery emits tuple(device_id, device_function, device_value)
            // topologyBuilder.setBolt("choralAverageQueryBolt", new ChoralAverageQueryBolt(cassandraHost))
            //        .shuffleGrouping("kafkaSpout");

            // redisBolt emits tuple(device_id, device_data, device_timestamp)
            topologyBuilder.setBolt("redisBolt", new RedisBolt(poolConfig))
                    .shuffleGrouping("kafkaSpout");
            // redisAverageQueryBolt emits tuple(device_id, device_function, device_value)
            // topologyBuilder.setBolt("redisAverageQueryBolt", new RedisAverageQueryBolt(poolConfig))
            //        .shuffleGrouping("choralAverageQueryBolt");

        } catch (Exception e) {
            e.printStackTrace();
        }
        //endregion
        if (!cluster) {
            //region Local cluster
            try {
                LocalCluster localCluster = new LocalCluster();
                localCluster.submitTopology("ChoralTopology", conf, topologyBuilder.createTopology());
            } catch (Exception e) {
                e.printStackTrace();
            }
            //endregion
        } else {
            //region Remote cluster
            try {
                StormSubmitter.submitTopology("ChoralTopology", conf, topologyBuilder.createTopology());
            } catch (Exception e) {
                e.printStackTrace();
            }
            //endregion
        }
    }
}
