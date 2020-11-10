package src;//Work needed
import java.io.Serializable;
import java.util.*;

public class Router implements Serializable {
    private int routerId;
    private int numberOfInterfaces;
    private ArrayList<IPAddress> interfaceAddresses;//list of IP address of all interfaces of the router
    private ArrayList<RoutingTableEntry> routingTable;//used to implement DVR
    private ArrayList<Integer> neighborRouterIDs;//Contains both "UP" and "DOWN" state routers
    private Boolean state;//true represents "UP" state and false is for "DOWN" state
    private Map<Integer, IPAddress> gatewayIDtoIP;
    public Router() {
        interfaceAddresses = new ArrayList<>();
        routingTable = new ArrayList<>();
        neighborRouterIDs = new ArrayList<>();

        /**
         * 80% Probability that the router is up
         */
        Random random = new Random();
        double p = random.nextDouble();
        if(p < 0.80) state = true;
        else state = false;

        numberOfInterfaces = 0;
    }

    public Router(int routerId, ArrayList<Integer> neighborRouters, ArrayList<IPAddress> interfaceAddresses, Map<Integer, IPAddress> gatewayIDtoIP) {
        this.routerId = routerId;
        this.interfaceAddresses = interfaceAddresses;
        this.neighborRouterIDs = neighborRouters;
        this.gatewayIDtoIP = gatewayIDtoIP;
        routingTable = new ArrayList<>();



        /**
         * 80% Probability that the router is up
         */
        Random random = new Random();
        double p = random.nextDouble();
        if(p < 0.80) state = true;
        else state = false;

        numberOfInterfaces = interfaceAddresses.size();
    }

    @Override
    public String toString() {
        String string = "";
        string += "Router ID: " + routerId + "\n" + "Interfaces: \n";
        for (int i = 0; i < numberOfInterfaces; i++) {
            string += interfaceAddresses.get(i).getString() + "\t";
        }
        string += "\n" + "Neighbors: \n";
        for(int i = 0; i < neighborRouterIDs.size(); i++) {
            string += neighborRouterIDs.get(i) + "\t";
        }
        return string;
    }



    /**
     * Initialize the distance(hop count) for each router.
     * for itself, distance=0; for any connected router with state=true, distance=1; otherwise distance=Constants.INFTY;
     */
    public void initiateRoutingTable() {
        RoutingTableEntry rt= new RoutingTableEntry(this.routerId,0,this.routerId);

        addRoutingTableEntry(rt);
        RoutingTableEntry temp;
            for(int i=0; i< neighborRouterIDs.size() ; i++)
            {

                    int neighid=neighborRouterIDs.get(i);
                    if(NetworkLayerServer.routerMap.get(neighid).getState()) {
                            temp = new RoutingTableEntry(neighid,1,neighid);
                            addRoutingTableEntry(temp);
                        }
                    else {
                            temp = new RoutingTableEntry(neighid,10.0,neighid);
                            addRoutingTableEntry(temp);
                        }


            }
        
    }

    /**
     * Delete all the routingTableEntry
     */
    public void clearRoutingTable() {
        this.routingTable.clear();
    }

    /**
     * Update the routing table for this router using the entries of Router neighbor
     * @param neighbor
     */
    public boolean updateRoutingTable(Router neighbor) {

        int finalSize=0;
        double intitSum=0;
        double finalsum=1;
        Map<Integer,Double> DistanceVectorN= new HashMap<>();

        for (int i=0;i<neighbor.getRoutingTable().size() ;i++)
        {
            int destination=neighbor.getRoutingTable().get(i).getRouterId();
            double distance=neighbor.getRoutingTable().get(i).getDistance();
            if(neighbor.getState() && distance==Constants.INFINITY)
            {
                distance=1;
                neighbor.getRoutingTable().get(i).setDistance(1);
            }
            DistanceVectorN.put(destination,distance);
            if(destination>finalSize) finalSize=destination;
        }
        Map<Integer,Double> DistanceVectorS= new HashMap<>();
        Map<Integer,Integer> HopS= new HashMap<>();
        for (int i=0;i<this.getRoutingTable().size() ;i++)
        {
            int destination=this.getRoutingTable().get(i).getRouterId();
            double distance=this.getRoutingTable().get(i).getDistance();
            int hop=this.getRoutingTable().get(i).getGatewayRouterId();
            intitSum=intitSum+distance;
            DistanceVectorS.put(destination,distance);
            HopS.put(destination,hop);
            if(destination>finalSize) finalSize=destination;
        }
    //    System.out.println("No." + routerId);
    //    System.out.println(DistanceVectorS);
    //    System.out.println(DistanceVectorN);
        double dist= DistanceVectorN.get(this.routerId);
    //    System.out.println("fin" +finalSize);
        for (int i=0;i< finalSize +1 ;i++)
        {

            if(!NetworkLayerServer.routerMap.get(neighbor.getRouterId()).getState()) continue;
            if(i+1==neighbor.getRouterId() && neighbor.getState()) continue;


            if(DistanceVectorN.containsKey(i+1) && DistanceVectorS.containsKey(i+1))
            {

                double temp= dist + DistanceVectorN.get(i+1);
                if(temp < DistanceVectorS.get(i+1)) DistanceVectorS.replace(i+1,temp);
                finalsum=finalsum + DistanceVectorS.get(i+1);
            }
            else if(DistanceVectorN.containsKey(i+1) && !DistanceVectorS.containsKey(i+1))
            {

                double temp= dist + DistanceVectorN.get(i+1);
                if(temp>=Constants.INFINITY) DistanceVectorS.put(i+1,10.0);
                else DistanceVectorS.put(i+1,temp);
                finalsum=finalsum + DistanceVectorS.get(i+1);

            }

        }


       // System.out.println("rout "+ this.routerId + " neigh "+ neighbor.getRouterId()+" Init "+ intitSum + " final " + finalsum);
        this.clearRoutingTable();

        for(int i=0 ; i< finalSize + 1 ; i++)
        {
            int hop;
            if(DistanceVectorS.containsKey(i)) {
                if(HopS.containsKey(i)) {
                     hop=HopS.get(i);
                }
                else hop= neighbor.getRouterId();
                RoutingTableEntry rt;
                if(DistanceVectorS.get(i)>=Constants.INFINITY)  rt = new RoutingTableEntry(i, 10.0, hop);
                else rt = new RoutingTableEntry(i, DistanceVectorS.get(i), hop);

                addRoutingTableEntry(rt);
            }
        }
        return intitSum != finalsum;
    }

    public boolean sfupdateRoutingTable(Router neighbor) {
        double dist =  0;
        boolean change = false;

        for(int i=0;i<routingTable.size();i++)
        {
            if(this.getRouterId() == neighbor.getRoutingTable().get(i).getGatewayRouterId()
               || this.getRoutingTable().get(neighbor.getRouterId()-1).getGatewayRouterId() !=i+1 )
                continue;

            dist=neighbor.routingTable.get(this.routerId-1).getDistance() + neighbor.routingTable.get(i).getDistance();
            if(dist<this.routingTable.get(i).getDistance())
            {
                change=true;
                this.routingTable.get(i).setDistance(dist);
                this.routingTable.get(i).setGatewayRouterId(neighbor.getRouterId());
            }
        }
        return change;
    }

    /**
     * If the state was up, down it; if state was down, up it
     */
    public void revertState() {
        state = !state;
        if(state) { initiateRoutingTable(); }
        else { clearRoutingTable(); }
    }

    public int getRouterId() {
        return routerId;
    }

    public void setRouterId(int routerId) {
        this.routerId = routerId;
    }

    public int getNumberOfInterfaces() {
        return numberOfInterfaces;
    }

    public void setNumberOfInterfaces(int numberOfInterfaces) {
        this.numberOfInterfaces = numberOfInterfaces;
    }

    public ArrayList<IPAddress> getInterfaceAddresses() {
        return interfaceAddresses;
    }

    public void setInterfaceAddresses(ArrayList<IPAddress> interfaceAddresses) {
        this.interfaceAddresses = interfaceAddresses;
        numberOfInterfaces = interfaceAddresses.size();
    }

    public ArrayList<RoutingTableEntry> getRoutingTable() {
        return routingTable;
    }

    public void addRoutingTableEntry(RoutingTableEntry entry) {
        this.routingTable.add(entry);
    }

    public ArrayList<Integer> getNeighborRouterIDs() {
        return neighborRouterIDs;
    }

    public void setNeighborRouterIDs(ArrayList<Integer> neighborRouterIDs) { this.neighborRouterIDs = neighborRouterIDs; }

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    public Map<Integer, IPAddress> getGatewayIDtoIP() { return gatewayIDtoIP; }

    public void printRoutingTable() {
        System.out.println("Router " + routerId);
        System.out.println("DestID Distance Nexthop");
        for (RoutingTableEntry routingTableEntry : routingTable) {
            System.out.println(routingTableEntry.getRouterId() + " " + routingTableEntry.getDistance() + " " + routingTableEntry.getGatewayRouterId());
        }
        System.out.println("-----------------------");
    }
    public String strRoutingTable() {
        String string = "Router" + routerId + "\n";
        string += "DestID Distance Nexthop\n";
        for (RoutingTableEntry routingTableEntry : routingTable) {
            if(routingTableEntry.getDistance()==10) string += routingTableEntry.getRouterId() + "\t\t" + routingTableEntry.getDistance() + "\t" + routingTableEntry.getGatewayRouterId() + "\n";
            else string += routingTableEntry.getRouterId() + "\t\t" + routingTableEntry.getDistance() + "\t\t" + routingTableEntry.getGatewayRouterId() + "\n";
        }

        string += "-----------------------\n";
        return string;
    }

}
