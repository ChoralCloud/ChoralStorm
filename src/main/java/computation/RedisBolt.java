package computation;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.storm.redis.bolt.AbstractRedisBolt;
import org.apache.storm.redis.common.config.JedisClusterConfig;
import org.apache.storm.redis.common.config.JedisPoolConfig;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import redis.clients.jedis.JedisCommands;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class RedisBolt extends AbstractRedisBolt {

    public RedisBolt(JedisPoolConfig config) {
        super(config);
    }

    public RedisBolt(JedisClusterConfig config) {
        super(config);
    }

    protected void process(Tuple tuple) {
        final JedisCommands jedisCommands = getInstance();

        try {
            if (tuple.getSourceComponent().equals("kafkaSpout")) {
                String data[] = tuple.getString(0).split(",");
                int deviceId = Integer.parseInt(data[0]);
                String deviceData = data[1];
                Timestamp timestamp = new Timestamp(Long.parseLong(data[2]));
                JsonParser parser = new JsonParser();
                JsonObject json = parser.parse(deviceData).getAsJsonObject();
                Set<Map.Entry<String, JsonElement>> entries = json.entrySet();

                Map<String, String> update = new HashMap<>();

                entries.forEach(e -> {
                    update.put(e.getKey(), e.getValue().getAsString());
                    update.put("timestamp", String.valueOf(timestamp.getTime()));
                });

                jedisCommands.hmset(String.valueOf(deviceId), update);
                collector.emit(new Values(deviceId, deviceData));
            } else {
                int deviceId = tuple.getIntegerByField("device_id");
                String func = tuple.getStringByField("function");
                double value = tuple.getDoubleByField("value");

                Map<String, String> update = new HashMap<>();
                update.put(func, String.valueOf(value));

                jedisCommands.hmset(String.valueOf(deviceId), update);
                collector.emit(new Values(deviceId, func, value));
            }
        } finally {
            if (jedisCommands != null) {
                returnInstance(jedisCommands);
            }
            collector.ack(tuple);
        }
    }

    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("device_id", "data"));
    }
}
