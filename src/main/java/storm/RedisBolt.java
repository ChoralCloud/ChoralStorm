package storm;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.storm.redis.bolt.AbstractRedisBolt;
import org.apache.storm.redis.common.config.JedisClusterConfig;
import org.apache.storm.redis.common.config.JedisPoolConfig;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.Jedis;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RedisBolt extends AbstractRedisBolt {
    private JedisPoolConfig config;

    public RedisBolt(JedisPoolConfig config) {
        super(config);
        this.config = config;
    }

    public RedisBolt(JedisClusterConfig config) {
        super(config);
    }

    protected void process(Tuple tuple) {
        Jedis jedis = new Jedis(config.getHost());
        JedisCommands jedisCommands = null;
        try {
            jedisCommands = getInstance();

            Gson gson = new Gson();
            String jsonString = tuple.getString(0);
            JsonObject json = gson.fromJson(jsonString, JsonObject.class);

            String deviceId = json.get("device_id").getAsString();
            JsonObject deviceData = json.get("device_data").getAsJsonObject();
            Timestamp deviceTimestamp = new Timestamp(json.get("device_timestamp").getAsLong());

            Set<Map.Entry<String, JsonElement>> entries = deviceData.entrySet();

            Map<String, String> update = new HashMap<>();

            entries.forEach(e -> {
                update.put(e.getKey(), e.getValue().getAsString());
                update.put("device_timestamp", String.valueOf(deviceTimestamp.getTime()));
            });

            jedisCommands.hmset(String.valueOf(deviceId), update);
            jedis.publish(deviceId, jsonString);
            collector.ack(tuple);
        } catch (Exception e) {
            collector.reportError(e);
            e.printStackTrace();
        } finally {
            if (jedisCommands != null) {
                returnInstance(jedisCommands);
            }
        }
    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("device_id", "device_data", "device_timestamp"));
    }
}
