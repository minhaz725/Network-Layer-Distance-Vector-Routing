package src;
import java.io.Serializable;

//Done!
public class EndDevice implements Serializable {



    private IPAddress ipAddress;
    private IPAddress gateway;
    private Integer deviceID;

    public EndDevice(IPAddress ipAddress, IPAddress gateway, Integer deviceID) {
        this.ipAddress = ipAddress;
        this.gateway = gateway;
        this.deviceID = deviceID;
    }

    public IPAddress getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(IPAddress ipAddress) {
        this.ipAddress = ipAddress;
    }

    public IPAddress getGateway() { return gateway; }

    public Integer getDeviceID() { return deviceID; }


    public String toString() {
        String string = "";
        string += "ID: " + deviceID + "\t" + "IP: " + ipAddress + "\t" + "Gateway: " + gateway + "\n";
        return string;
    }
}

