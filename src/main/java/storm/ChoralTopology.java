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
    public static void main(String[] args) {
        //region Kafka spout creation
        String topicName = args[0];
        BrokerHosts zooKeeperHosts = new ZkHosts("localhost:2181");
        String spoutId = "choraldatastreamSpout";
        SpoutConfig spoutConfig = new SpoutConfig(zooKeeperHosts, topicName, "/" + topicName, spoutId);
        spoutConfig.startOffsetTime = System.currentTimeMillis();
        spoutConfig.scheme = new SchemeAsMultiScheme(new StringScheme());
        KafkaSpout kafkaSpout = new KafkaSpout(spoutConfig);
        //endregion

        //region Redis creation
        JedisPoolConfig poolConfig = new JedisPoolConfig.Builder().setHost("localhost").setPort(6379).build();
        //endregion

        /*
        Topology
        kafka -> choralBolt
              -> cassandraBolt
              -> choralAverageQuery -\
              -------------------------> redisBolt
         */

        //region Topology creation
        TopologyBuilder topologyBuilder = new TopologyBuilder();
        topologyBuilder.setSpout("kafkaSpout", kafkaSpout);
        topologyBuilder.setBolt("choralBolt", new ChoralBolt())
                .shuffleGrouping("kafkaSpout");
        topologyBuilder.setBolt("cassandraBolt", new CassandraBolt())
                .shuffleGrouping("kafkaSpout");
        topologyBuilder.setBolt("choralAverageQuery", new ChoralAverageQuery())
                .shuffleGrouping("kafkaSpout");
        topologyBuilder.setBolt("redisBolt", new RedisBolt(poolConfig))
                .shuffleGrouping("kafkaSpout")
                .shuffleGrouping("choralAverageQuery");
        //endregion

        //region Local cluster
//        Config config = new Config();
//        config.setDebug(true);
//        LocalCluster localCluster = new LocalCluster();
//        localCluster.submitTopology("ChoralTopology", config, topologyBuilder.createTopology());
        //endregion

        //region Local cluster termination
//        Utils.sleep(100000);
//        localCluster.killTopology("ChoralTopology");
//        localCluster.shutdown();
        //endregion

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
