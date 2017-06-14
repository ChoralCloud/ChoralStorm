package testproducer;

import com.google.gson.JsonObject;
import org.apache.commons.net.ntp.TimeStamp;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ChoralProducer {

    public static void main(String[] args) {
        System.out.println("Apache Kafka Started");

        Properties properties = new Properties();
        // Zookeepers
        properties.put("zookeeper.connect", "localhost:2181");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        // Brokers
        properties.put("bootstrap.servers", "localhost:9092");

        Producer producer = new KafkaProducer<String, String>(properties);

        Device d1 = new Device("1", new JsonObject(), System.currentTimeMillis());
        Device d2 = new Device("2", new JsonObject(), System.currentTimeMillis());
        Device d3 = new Device("3", new JsonObject(), System.currentTimeMillis());

        int max = 50;
        int min = -50;

        String topic = "test5";

        Runnable device1 = () -> {
            d1.deviceId = "1";
            Random random = new Random();
            int temp = random.nextInt(max + 1 - min) + min;
            d1.data.addProperty("temperature", String.valueOf(temp));
            d1.timestamp = System.currentTimeMillis();
            producer.send(new ProducerRecord(topic,"",d1.toString()));
        };

        Runnable device2 = () -> {
            d2.deviceId = "2";
            Random random = new Random();
            int temp = random.nextInt(max + 1 - min) + min;
            d2.data.addProperty("temperature", String.valueOf(temp));
            d2.timestamp = System.currentTimeMillis();
            producer.send(new ProducerRecord(topic,"",d2.toString()));
        };

        Runnable device3 = () -> {
            d3.deviceId = "3";
            Random random = new Random();
            int temp = random.nextInt(max + 1 - min) + min;
            d3.data.addProperty("temperature", String.valueOf(temp));
            d3.timestamp = System.currentTimeMillis();
            producer.send(new ProducerRecord(topic,"",d3.toString()));
        };

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(3);
        executor.scheduleAtFixedRate(device1, 1, 3, TimeUnit.SECONDS);
        executor.scheduleAtFixedRate(device2, 2, 3, TimeUnit.SECONDS);
        executor.scheduleAtFixedRate(device3, 3, 3, TimeUnit.SECONDS);
    }
}
