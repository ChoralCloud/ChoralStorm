package storm;

import com.datastax.driver.core.*;
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
import java.util.List;
import java.util.Map;

public class ChoralAverageQuery extends BaseRichBolt {

    private OutputCollector collector;
    private PreparedStatement preparedStatement;
    private Session session;
    private Cluster cluster;

    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
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
            ResultSet rows = resultSetFuture.get();
            List<Row> all = rows.all();

            double avg = all.parallelStream().mapToDouble(r -> {
                Gson dataGson = new Gson();
                String dataJson = r.get("device_data", String.class);
                String temp = dataGson.fromJson(dataJson, JsonObject.class).get("temperature").getAsString();
                return Double.parseDouble(temp);
            }).average().orElse(0.0);

            System.out.println("Average for " + deviceId + " = " + avg);

            collector.emit(new Values(deviceId, "average", avg));
            collector.ack(tuple);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("device_id", "function", "value"));
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
