package storm;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.kafka.*;
import org.apache.storm.redis.common.config.JedisPoolConfig;
import org.apache.storm.spout.SchemeAsMultiScheme;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.utils.Utils;

public class ChoralTopology {

    public static boolean local = false;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: ChoralTopology <TOPIC_NAME> [local | remote]");
            return;
        }

        String topicName = args[0];

        if (args.length <= 2) {
            local = args[1].equals("local");
        }

        //region Kafka spout creation
        KafkaSpout kafkaSpout = null;
        try {
            String zookeeperHost = local ? "localhost:2181" : "zookeeper:2181";
            BrokerHosts zooKeeperHosts = new ZkHosts(zookeeperHost);
            String spoutId = "choraldatastreamSpout";
            SpoutConfig spoutConfig = new SpoutConfig(zooKeeperHosts, topicName, "/" + topicName, spoutId);
            spoutConfig.startOffsetTime = System.currentTimeMillis();
            spoutConfig.scheme = new SchemeAsMultiScheme(new StringScheme());
            kafkaSpout = new KafkaSpout(spoutConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //endregion

        //region Redis creation
        JedisPoolConfig poolConfig = null;
        try {
            String redisHost = local ? "localhost" : "redis";
            poolConfig = new JedisPoolConfig.Builder().setHost(redisHost).setPort(6379).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //endregion

        /*
        Topology
        kafka -> cassandraBolt
              -> choralAverageQuery -\
              -------------------------> redisBolt
         */

        //region Topology creation
        TopologyBuilder topologyBuilder = new TopologyBuilder();
        try {
            // kafka emits tuple(device_id, device_data, device_timestamp)
            topologyBuilder.setSpout("kafkaSpout", kafkaSpout);
            // cassandraBolt emits tuple(device_id, device_data, device_timestamp)
            topologyBuilder.setBolt("cassandraBolt", new CassandraBolt())
                    .shuffleGrouping("kafkaSpout");
            // choralAverageQuery emits tuple(device_id, function, value)
            topologyBuilder.setBolt("choralAverageQuery", new ChoralAverageQuery())
                    .shuffleGrouping("kafkaSpout");
            // redisBolt emits tuple(device_id, function, value) or tuple(device_id, function_ value)
            topologyBuilder.setBolt("redisBolt", new RedisBolt(poolConfig))
                    .shuffleGrouping("kafkaSpout")
                    .shuffleGrouping("choralAverageQuery");
        } catch (Exception e) {
            e.printStackTrace();
        }
        //endregion
        if (local) {
            //region Local cluster
            try {
                Config config = new Config();
                config.setDebug(true);
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
