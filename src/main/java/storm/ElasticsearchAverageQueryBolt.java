package storm;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.Map;

public class ElasticsearchAverageQueryBolt extends BaseRichBolt {
    private OutputCollector collector;

    public void prepare(Map stormConf, TopologyContext topologyContext, OutputCollector outputCollector) {
        collector = outputCollector;
    }

    public void execute(Tuple tuple) {
        try {
            String deviceId = tuple.getStringByField("device_id");
            String deviceFunc = tuple.getStringByField("device_function");
            double deviceValue = tuple.getDoubleByField("device_value");

            // index = choraldatastream, type = function
            String elasticsearchHost = ChoralTopology.local ? "localhost" : "elasticsearch";
            HttpPost request = new HttpPost("http://" + elasticsearchHost + ":9200/choraldatastream/function/" + deviceId);
            request.addHeader("Content-Type", "application/json;charset=UTF-8");

            StringEntity entity = new StringEntity("{\n" +
                    "    \"device_id\": \"" + deviceId + "\",\n" +
                    "    \"device_function\": \"" + deviceFunc + "\",\n" +
                    "    \"device_value\": \"" + deviceValue + "\"\n" +
                    "}", "UTF-8");

            request.setEntity(entity);

            HttpClientBuilder bld = HttpClientBuilder.create();
            HttpClient client = bld.build();

            HttpResponse response = client.execute(request);
            HttpEntity resp = response.getEntity();
            System.out.println("Post to ElasticSearch: " + EntityUtils.toString(resp, "UTF-8"));

            collector.emit(new Values(deviceId, deviceFunc, deviceValue));
            collector.ack(tuple);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declareStream("redis_computed_data", new Fields("device_id", "device_function", "device_value"));
    }
}
