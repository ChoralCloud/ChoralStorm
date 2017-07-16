package storm;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.json.simple.JSONObject;

import java.sql.Timestamp;
import java.util.Map;

public class CassandraBolt extends BaseRichBolt {

    private OutputCollector collector;
    private PreparedStatement preparedStatement;
    private Cluster cluster;
    private Session session;

    private String cassandraHost;

    public CassandraBolt(String host) {
        cassandraHost = host;
    }

    public void prepare(Map stormConf, TopologyContext topologyContext, OutputCollector outputCollector) {
        collector = outputCollector;
        try {
            preparedStatement = getSession().prepare(
                    "INSERT INTO choraldatastream.raw_data(device_id, device_data, device_timestamp, time) " +
                            "VALUES (?, ?, ?, ?);"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void execute(Tuple tuple) {
        try {
            Gson gson = new Gson();
            JsonObject json = gson.fromJson(tuple.getString(0), JsonObject.class);
            String deviceId = json.get("device_id").getAsString();
            JsonObject deviceData = json.get("device_data").getAsJsonObject();
            Timestamp deviceTimestamp = new Timestamp(json.get("device_timestamp").getAsLong());
            if (!deviceId.isEmpty() && deviceData.entrySet().size() > 0 && deviceTimestamp.getTime() > 0) {
                getSession().executeAsync(preparedStatement.bind(deviceId, deviceData.toString(), deviceTimestamp, new Timestamp(System.currentTimeMillis())));
            }
            collector.ack(tuple);
        } catch (Exception e) {
            collector.reportError(e);
            e.printStackTrace();
        }
    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("device_id", "device_data", "device_timestamp"));
    }

    public Cluster getCluster() {
        if (cluster == null || cluster.isClosed()) {
            String[] contactPoints = new String[]{cassandraHost};
            cluster = Cluster.builder()
                    .addContactPoints(contactPoints)
                    .build();
        }
        return cluster;
    }

    public Session getSession() {
        if (session == null || session.isClosed()) {
            session = getCluster().connect();
        }
        return session;
    }
}
