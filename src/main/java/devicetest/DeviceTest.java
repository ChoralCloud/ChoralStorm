package devicetest;

import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DeviceTest {

    public static void main(String[] args) throws Exception {
        try {
            Device d1 = new Device("1", "secret", new JsonObject(), System.currentTimeMillis());
            Device d2 = new Device("2", "secret", new JsonObject(), System.currentTimeMillis());
            Device d3 = new Device("3", "secret", new JsonObject(), System.currentTimeMillis());

            int max = 50;
            int min = -50;

            Runnable device1 = () -> {
                int temp = new Random().nextInt(max + 1 - min) + min;
                d1.deviceData.addProperty("temperature", String.valueOf(temp));
                d1.deviceTimestamp = System.currentTimeMillis();
                post(d1.toString());
                System.out.println(d1.toString());
            };

            Runnable device2 = () -> {
                int temp = new Random().nextInt(max + 1 - min) + min;
                d2.deviceData.addProperty("temperature", String.valueOf(temp));
                d2.deviceTimestamp = System.currentTimeMillis();
                post(d2.toString());
                System.out.println(d2.toString());
            };

            Runnable device3 = () -> {
                int temp = new Random().nextInt(max + 1 - min) + min;
                d3.deviceData.addProperty("temperature", String.valueOf(temp));
                d3.deviceTimestamp = System.currentTimeMillis();
                post(d3.toString());
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

    private static void post(String data) {
        try {
            URL url = new URL("http://choralcluster.csc.uvic.ca:8080");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);//5 secs
            connection.setReadTimeout(5000);//5 secs

            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
            out.write(data);
            out.flush();
            out.close();

            int res = connection.getResponseCode();

            System.out.println(res);

            InputStream is = connection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class Device {
        public String deviceId;
        public String userSecret;
        public JsonObject deviceData;
        public long deviceTimestamp;

        public Device() {}

        public Device(String id, String secret, JsonObject data, long timestamp) {
            deviceId = id; userSecret = secret; deviceData = data; deviceTimestamp = timestamp;
        }

        public String toString() {
            return "{\"device_id\":\"" + this.deviceId + "\",\"user_secret\":\"" + this.userSecret + "\",\"device_data\":" + this.deviceData.toString() + ",\"device_timestamp\":" + this.deviceTimestamp + "}";
        }
    }
}

