
package src;
import java.util.ArrayList;
import java.util.Random;

public class ServerThread implements Runnable {

    NetworkUtility networkUtility;
    EndDevice endDevice;

    ServerThread(NetworkUtility networkUtility, EndDevice endDevice) {
        this.networkUtility = networkUtility;
        this.endDevice = endDevice;
        System.out.println("Server Ready for client " + (NetworkLayerServer.clientCount+1));
        NetworkLayerServer.clientCount++;
        new Thread(this).start();
    }

    @Override
    public void run() {
        /**
         * Synchronize actions with client.
         */
        networkUtility.write(this.endDevice);

        networkUtility.write(NetworkLayerServer.clientInterfaces);

       // networkUtility.write(NetworkLayerServer.routers);

       // networkUtility.write(NetworkLayerServer.interfacetoRouterIDStr);



        int packetnum=10;
       // networkUtility.write(packetnum);
        while (--packetnum>0){
        Packet packet=(Packet)networkUtility.read();
        deliverPacket(packet);
        System.out.println("Serv "+packet.getMessage() + " Src IP " + packet.getSourceIP()+ " dest IP " + packet.getDestinationIP());
        }



        
        /*
        Tasks:
        1. Upon receiving a packet and recipient, call deliverPacket(packet)
        2. If the packet contains "SHOW_ROUTE" request, then fetch the required information
                and send back to client
        3. Either send acknowledgement with number of hops or send failure message back to client
        */
    }


    public Boolean deliverPacket(Packet p) {


        int src,dest;

        IPAddress sip= p.getSourceIP();
        String temp=  sip.toString();
        String srcAddrOne=temp;
        int val=Integer.parseInt(temp.substring(temp.length()-1));
        String srcAddr = "";
        if(val==1) {

            srcAddr = temp.substring(0, temp.length() - 2) + ".2";
        }
        else {

            srcAddr = temp.substring(0, temp.length() - 2) + ".1";
            srcAddrOne = srcAddr;
        }
        src=NetworkLayerServer.interfacetoRouterIDStr.get(srcAddr);
        IPAddress dip= p.getDestinationIP();
        dest=NetworkLayerServer.interfacetoRouterIDStr.get(dip.getString());
        if(src==dest) {
            String msg= "Success! Total Hops:";
            networkUtility.write(msg+p.hopcount);
        }
        else {
            Router s = NetworkLayerServer.routerMap.get(src);
            Router d = NetworkLayerServer.routerMap.get(dest);
          //  System.out.println(s.getRoutingTable());
            RoutingTableEntry rt = s.getRoutingTable().get(d.getRouterId() - 1);


            if (rt.getDistance() == Constants.INFINITY) {
                rt.setDistance(1);
                synchronized (NetworkLayerServer.stateChanger) {
                    try {
                        NetworkLayerServer.stateChanger.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                NetworkLayerServer.simpleDVR(src);
                NetworkLayerServer.stateChanger.notify();
            }

           // System.out.println(rt);
         ////   System.out.print(" src " + src + " dst " + dest);
            //System.out.println(NetworkLayerServer.interfacetoRouterIDStr);
            IPAddress gateip = s.getGatewayIDtoIP().get(rt.getGatewayRouterId());
         ////   System.out.println(" gate" + gateip);

            if (!NetworkLayerServer.routers.get(NetworkLayerServer.interfacetoRouterIDStr.get(dip.toString()) - 1).getState()) {
                System.out.println("dst router" + NetworkLayerServer.routers.get(NetworkLayerServer.interfacetoRouterIDStr.get(dip.toString()) - 1).getRouterId() + " down");
                String msg = "Failed";
                networkUtility.write(msg);
            }
            else if (!NetworkLayerServer.routers.get(NetworkLayerServer.interfacetoRouterIDStr.get(srcAddrOne)-1).getState()) {
                System.out.println("src router" + NetworkLayerServer.routers.get(NetworkLayerServer.interfacetoRouterIDStr.get(srcAddrOne) - 1).getRouterId() + " down");
                String msg = "Failed";
                networkUtility.write(msg);
            } else {
                boolean state = NetworkLayerServer.routerMap.get(rt.getGatewayRouterId()).getState();
                if (!state) {
                    String msg = "Failed";
                    networkUtility.write(msg);
                    rt.setDistance(Constants.INFINITY);
                    synchronized (NetworkLayerServer.stateChanger) {
                        try {
                            NetworkLayerServer.stateChanger.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    NetworkLayerServer.simpleDVR(src);
                    NetworkLayerServer.stateChanger.notify();
                } else {

                    p.setSourceIP(gateip);
                    p.hopcount++;
                    System.out.println("hop" + p.hopcount);
                    if(p.getSpecialMessage().equals("SHOW_ROUTE"))
                    {

                        String routemsg="-----SHOW_ROUTE----"+p.getMessage()+"\n";
                        routemsg=routemsg+"Source: "+ sip.toString() + "Dest: " + dip.toString()+"\n";
                        routemsg=routemsg+"Hop Count: "+p.hopcount+"\n";
                        routemsg=routemsg+"Routing Table of Source Router:\n"+s.getRoutingTable().toString()+"\n";
                        routemsg=routemsg+"Routing Table of Dest Router:\n"+d.getRoutingTable().toString()+"\n";
                        networkUtility.write(routemsg);
                    }

                    deliverPacket(p);
                }
            }
        }
       // System.out.println(NetworkLayerServer.interfacetoRouterIDStr);

        /*
        1. Find the router s which has an interface
                such that the interface and source end device have same network address.
        2. Find the router d which has an interface
                such that the interface and destination end device have same network address.
        3. Implement forwarding, i.e., s forwards to its gateway router x considering d as the destination.
                similarly, x forwards to the next gateway router y considering d as the destination,
                and eventually the packet reaches to destination router d.

            3(a) If, while forwarding, any gateway x, found from routingTable of router r is in down state[x.state==FALSE]
                    (i) Drop packet
                    (ii) Update the entry with distance Constants.INFTY
                    (iii) Block NetworkLayerServer.stateChanger.t
                    (iv) Apply DVR starting from router r.
                    (v) Resume NetworkLayerServer.stateChanger.t

            3(b) If, while forwarding, a router x receives the packet from router y,
                    but routingTableEntry shows Constants.INFTY distance from x to y,
                    (i) Update the entry with distance 1
                    (ii) Block NetworkLayerServer.stateChanger.t
                    (iii) Apply DVR starting from router x.
                    (iv) Resume NetworkLayerServer.stateChanger.t

        4. If 3(a) occurs at any stage, packet will be dropped,
            otherwise successfully sent to the destination router
        */

        return true;
        }

        @Override
        public boolean equals(Object obj) {
        return super.equals(obj); //To change body of generated methods, choose Tools | Templates.
    }
}
