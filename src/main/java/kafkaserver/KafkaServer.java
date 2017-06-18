package kafkaserver;

import com.google.gson.JsonObject;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class KafkaServer {

    private static Producer<String, String> producer;
    private static String topic;

    public static void main(String[] args) throws Exception {
        try {
            Properties properties = new Properties();
            // Zookeepers
            properties.put("zookeeper.connect", "localhost:2181");
            properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            // Brokers
            properties.put("bootstrap.servers", "localhost:9092");

            producer = new KafkaProducer<>(properties);
            topic = "choraldatastream";

            Device d1 = new Device("1", new JsonObject(), System.currentTimeMillis());
            Device d2 = new Device("2", new JsonObject(), System.currentTimeMillis());
            Device d3 = new Device("3", new JsonObject(), System.currentTimeMillis());

            int max = 50;
            int min = -50;

            Runnable device1 = () -> {
                int temp = new Random().nextInt(max + 1 - min) + min;
                d1.deviceData.addProperty("temperature", String.valueOf(temp));
                d1.deviceTimestamp = System.currentTimeMillis();
                producer.send(new ProducerRecord<>(topic, "", d1.toString()));
                System.out.println(d1.toString());
            };

            Runnable device2 = () -> {
                int temp = new Random().nextInt(max + 1 - min) + min;
                d2.deviceData.addProperty("temperature", String.valueOf(temp));
                d2.deviceTimestamp = System.currentTimeMillis();
                producer.send(new ProducerRecord<>(topic, "", d2.toString()));
                System.out.println(d2.toString());
            };

            Runnable device3 = () -> {
                int temp = new Random().nextInt(max + 1 - min) + min;
                d3.deviceData.addProperty("temperature", String.valueOf(temp));
                d3.deviceTimestamp = System.currentTimeMillis();
                producer.send(new ProducerRecord<>(topic, "", d3.toString()));
                System.out.println(d3.toString());
            };

            ScheduledExecutorService executor = Executors.newScheduledThreadPool(3);
            executor.scheduleAtFixedRate(device1, 1, 3, TimeUnit.SECONDS);
            executor.scheduleAtFixedRate(device2, 2, 3, TimeUnit.SECONDS);
            executor.scheduleAtFixedRate(device3, 3, 3, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class Device {
        public String deviceId;
        public JsonObject deviceData;
        public long deviceTimestamp;

        public Device() {}

        public Device(String id, JsonObject data, long timestamp) {
            deviceId = id; deviceData = data; deviceTimestamp = timestamp;
        }

        public String toString() {
            return "{device_id:" + this.deviceId + ",device_data:" + this.deviceData.toString() + ",device_timestamp:" + this.deviceTimestamp + "}";
        }
    }
}
