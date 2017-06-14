package computation;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.kafka.*;
import org.apache.storm.spout.SchemeAsMultiScheme;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.utils.Utils;

public class ChoralTopology {
    public static void main(String[] args) {
        //region Kafka spout creation
        BrokerHosts zooKeeperHosts = new ZkHosts("localhost:2181");
        String topicName = "test5";
        String spoutId = "choraldatastreamSpout";
        SpoutConfig spoutConfig = new SpoutConfig(zooKeeperHosts, topicName, "/" + topicName, spoutId);
        spoutConfig.startOffsetTime = System.currentTimeMillis();
        spoutConfig.scheme = new SchemeAsMultiScheme(new StringScheme());
        KafkaSpout kafkaSpout = new KafkaSpout(spoutConfig);
        //endregion

        //region Topology creation
        TopologyBuilder topologyBuilder = new TopologyBuilder();
        topologyBuilder.setSpout("kafkaSpout", kafkaSpout);
        topologyBuilder.setBolt("choralBolt", new ChoralBolt())
                .shuffleGrouping("kafkaSpout");
        topologyBuilder.setBolt("cassandraBolt", new CassandraBolt())
                .shuffleGrouping("kafkaSpout");
        topologyBuilder.setBolt("choralAverageQuery", new ChoralAverageQuery())
                .shuffleGrouping("cassandraBolt");
//        topologyBuilder.setBolt("redisBolt", new RedisBolt())
//                .shuffleGrouping("choralAverageQuery");
        //endregion

        //region Local cluster
        Config config = new Config();
        config.setDebug(true);
        LocalCluster localCluster = new LocalCluster();
        localCluster.submitTopology("ChoralTopology", config, topologyBuilder.createTopology());
        //endregion

        //region Local cluster termination
        Utils.sleep(100000);
        localCluster.killTopology("ChoralTopology");
        localCluster.shutdown();
        //endregion

        //region Remote cluster
//        Config remoteClusterConfig = new Config();
//        remoteClusterConfig.setMessageTimeoutSecs(20);
//
//        try {
//            StormSubmitter.submitTopology("ChoralTopology", remoteClusterConfig, topologyBuilder.createTopology());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        //endregion
    }
}
