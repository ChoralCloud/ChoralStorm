package storm;

import com.datastax.driver.core.*;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.sql.Timestamp;
import java.util.*;

public class ChoralAverageQueryBolt extends BaseRichBolt {

    private OutputCollector collector;
    private PreparedStatement preparedStatement;
    private Session session;
    private Cluster cluster;

    public void prepare(Map stormConf, TopologyContext topologyContext, OutputCollector outputCollector) {
        collector = outputCollector;
        try {
            preparedStatement = getSession().prepare(
                    "SELECT * " +
                    "FROM choraldatastream.raw_data " +
                    "WHERE device_id = ? " +
                    "AND device_timestamp >= ? " +
                    "AND device_timestamp <= ?;"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void execute(Tuple tuple) {
        try {
            Timestamp tenMinutesAgo = new Timestamp(System.currentTimeMillis() - (100 * 60 * 1000));
            Timestamp now = new Timestamp(System.currentTimeMillis());

            Gson gson = new Gson();
            JsonObject json = gson.fromJson(tuple.getString(0), JsonObject.class);

            String deviceId = json.get("device_id").getAsString();

            ResultSetFuture resultSetFuture = getSession().executeAsync(preparedStatement.bind(deviceId, tenMinutesAgo, now));

            // Got all data between now and 10 minutes ago
            List<Row> rows = resultSetFuture.get().all();
            long rowCount = rows.size();
            HashMap<String, Double> dataMap = new HashMap<>();
            rows.parallelStream()
                    .map(r -> new Gson().fromJson(r.get("device_data", String.class), JsonObject.class))
                    .forEach(j -> {
                        Set<Map.Entry<String, JsonElement>> dataEntries = j.entrySet();
                        dataEntries.forEach(e -> dataMap.merge(e.getKey(), e.getValue().getAsDouble(), (a, b) -> a + b));
                    });

            JsonObject deviceData = new JsonObject();
            Set<Map.Entry<String, Double>> averages = dataMap.entrySet();
            averages.forEach(a -> deviceData.addProperty(a.getKey(), (a.getValue() / rowCount)));

            collector.emit(new Values(deviceId, "average", deviceData.toString()));
            collector.ack(tuple);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("device_id", "device_function", "device_data"));
    }

    public Cluster getCluster() {
        if (cluster == null || cluster.isClosed()) {
            String cassandraHost = ChoralTopology.local ? "localhost" : "cassandra";
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
