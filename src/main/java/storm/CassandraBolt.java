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
import org.apache.storm.tuple.Values;

import java.sql.Timestamp;
import java.util.Map;

public class CassandraBolt extends BaseRichBolt {

    private OutputCollector collector;
    private PreparedStatement preparedStatement;
    private Cluster cluster;
    private Session session;

    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
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
        Gson gson = new Gson();
        JsonObject json = gson.fromJson(tuple.getString(0), JsonObject.class);

        try {
            String deviceId = json.get("device_id").getAsString();
            String deviceData = json.get("device_data").getAsJsonObject().toString();
            Timestamp deviceTimestamp = new Timestamp(json.get("device_timestamp").getAsLong());

            getSession().executeAsync(preparedStatement.bind(deviceId, deviceData, deviceTimestamp, new Timestamp(System.currentTimeMillis())));

            collector.emit(tuple, new Values(deviceId, deviceData, deviceTimestamp));
            collector.ack(tuple);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("device_id", "device_data", "device_timestamp"));
    }

    public Cluster getCluster() {
        if (cluster == null || cluster.isClosed()) {
            String[] contactPoints = new String[]{"localhost"};
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
