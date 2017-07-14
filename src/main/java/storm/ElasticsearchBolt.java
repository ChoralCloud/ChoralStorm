package storm;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.json.simple.JSONObject;

import java.sql.Timestamp;
import java.util.Map;

public class ElasticsearchBolt extends BaseRichBolt {
    private OutputCollector collector;

    public void prepare(Map stormConf, TopologyContext topologyContext, OutputCollector outputCollector) {
        collector = outputCollector;
    }

    public void execute(Tuple tuple) {
        try {
            Gson gson = new Gson();
            JsonObject json = gson.fromJson(tuple.getString(0), JsonObject.class);

            String deviceId = json.get("device_id").getAsString();
            JsonObject deviceData = json.get("device_data").getAsJsonObject();
            Timestamp deviceTimestamp = new Timestamp(json.get("device_timestamp").getAsLong());

            // index = choraldatastream, type = raw_data
            String elasticsearchHost = ChoralTopology.local ? "localhost" : "elasticsearch";
            HttpPost request = new HttpPost("http://" + elasticsearchHost + ":9200/choraldatastream/raw_data/" + deviceId);
            request.addHeader("Content-Type", "application/json;charset=UTF-8");

            StringEntity entity = new StringEntity("{\n" +
                    "    \"device_id\": \"" + deviceId + "\",\n" +
                    "    \"device_data\": \"" + JSONObject.escape(deviceData.toString()) + "\",\n" +
                    "    \"device_timestamp\": \"" + deviceTimestamp + "\"\n" +
                    "}", "UTF-8");

            request.setEntity(entity);

            HttpClientBuilder bld = HttpClientBuilder.create();
            HttpClient client = bld.build();

            HttpResponse response = client.execute(request);
            HttpEntity resp = response.getEntity();

            collector.emit(new Values(deviceId, deviceData, deviceTimestamp));
            collector.ack(tuple);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("device_id", "device_data", "device_timestamp"));
    }
}
