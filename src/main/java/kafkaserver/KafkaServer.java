package kafkaserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.Properties;

public class KafkaServer {

    private static Producer<String, String> producer;
    private static String topic;

    public static void main(String[] args) throws Exception {
        Properties properties = new Properties();
        // Zookeepers
        properties.put("zookeeper.connect", "localhost:2181");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        // Brokers
        properties.put("bootstrap.servers", "localhost:9092");

        producer = new KafkaProducer<>(properties);
        topic = "test8";

        int port = 3030;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        System.out.println("server started at " + port);
        server.createContext("/", new RootHandler());
        server.setExecutor(null);
        server.start();
    }

    static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange he) throws IOException {
            InputStreamReader isr = new InputStreamReader(he.getRequestBody(), "utf-8");
            BufferedReader br = new BufferedReader(isr);
            String request = JSONObject.escape(br.readLine()).replace("\\", "");
            System.out.println(request);

            producer.send(new ProducerRecord<>(topic,"", request));
        }
    }
}
