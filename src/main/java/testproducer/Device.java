package testproducer;

import com.google.gson.JsonObject;
import org.apache.commons.net.ntp.TimeStamp;

import java.util.Date;

public class Device {
    public String deviceId;
    public JsonObject data;
    public Long timestamp;

    public Device() {}

    public Device (String id, JsonObject d, Long ts) {
        deviceId = id;
        data = d;
        timestamp = ts;
    }

    public String toString() {
        return deviceId + "," + data.toString() + "," + timestamp;
    }
}
