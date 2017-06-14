package computation;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
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
        preparedStatement = getSession().prepare(
                "INSERT INTO choraldatastream.raw_data(device_id, data, time) " +
                "VALUES (?, ?, ?);"
        );
    }

    public void execute(Tuple tuple) {
        String data[] = tuple.getString(0).split(",");

        try {
            int deviceId = Integer.parseInt(data[0]);
            String deviceData = data[1];
            Timestamp timestamp = new Timestamp(Long.parseLong(data[2]));

            getSession().executeAsync(preparedStatement.bind(deviceId, deviceData, timestamp));

            collector.emit(tuple, new Values(deviceId));
            collector.ack(tuple);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("device_id"));
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
