package storm;

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


public class RedisAverageQueryBolt extends AbstractRedisBolt {

    public RedisAverageQueryBolt(JedisPoolConfig config) {
        super(config);
    }

    public RedisAverageQueryBolt(JedisClusterConfig config) {
        super(config);
    }

    protected void process(Tuple tuple) {
        JedisCommands jedisCommands = null;
        try {
            jedisCommands = getInstance();
            String deviceId = tuple.getStringByField("device_id");
            String deviceFunc = tuple.getStringByField("device_function");
            double deviceValue = tuple.getDoubleByField("device_value");

            Map<String, String> update = new HashMap<>();
            update.put(deviceFunc, String.valueOf(deviceValue));

            jedisCommands.hmset(deviceId, update);
            collector.emit(new Values(deviceId, deviceFunc, deviceValue));
            collector.ack(tuple);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedisCommands != null) {
                returnInstance(jedisCommands);
            }
        }
    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declareStream("redis_computed_data", new Fields("device_id", "device_function", "device_value"));
    }
}
