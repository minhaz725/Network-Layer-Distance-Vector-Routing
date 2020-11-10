//Done!
package src;

import java.io.Serializable;

public class RoutingTableEntry implements Serializable {

    private int routerId;
    private double distance;
    private int gatewayRouterId;

    public RoutingTableEntry(int routerId, double distance, int gatewayRouterId) {
        this.routerId = routerId;
        this.distance = distance;
        this.gatewayRouterId = gatewayRouterId;
    }

    public String toString() {
        String string = "";
        string += "Destination: " + routerId + "\t" + "Distance: " + distance + "\t" + "Next Hop: " + gatewayRouterId + "\n";
        return string;
    }

    public int getRouterId() {
        return routerId;
    }

    public void setRouterId(int routerId) {
        this.routerId = routerId;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getGatewayRouterId() {
        return gatewayRouterId;
    }

    public void setGatewayRouterId(int gatewayRouterId) {
        this.gatewayRouterId = gatewayRouterId;
    }

}
