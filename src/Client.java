package src;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

//Work needed
public class Client {
    public static void main(String[] args) throws InterruptedException {
        NetworkUtility networkUtility = new NetworkUtility("127.0.0.1", 4444);
        System.out.println("Connected to server");


        EndDevice source =(EndDevice)networkUtility.read();
        Map<IPAddress,Integer> clientInterfaces = (Map<IPAddress, Integer>)  networkUtility.read();
       // ArrayList<Router> routers = (ArrayList<Router>)  networkUtility.read();
       // Map<String, Integer> interfacetoRouterIDStr = (Map<String, Integer>) networkUtility.read();
       // System.out.println(source);
       // System.out.println(clientInterfaces);
      //  int packetnum = (Integer) networkUtility.read();



         for(int i=0;i<10;i++)
         {
        //5.      Generate a random message
                String msg = "Packet no: " + (i+1) ;
                String smsg=""+"SHOW_ROUTE";
        //6.      Assign a random receiver from active client list
             Random random = new Random(System.currentTimeMillis());
             int r = Math.abs(random.nextInt(clientInterfaces.size()));

          //   System.out.println("Size: " + clientInterfaces.size() + "\n" + r);

             IPAddress ip = null;
             IPAddress gateway = null;

             int x = 0;
             for (Map.Entry<IPAddress, Integer> entry : clientInterfaces.entrySet()) {
                 IPAddress key = entry.getKey();
                 Integer value = entry.getValue();
                 if(x == r) {
                     gateway = key;
                     ip = new IPAddress(gateway.getBytes()[0] + "." + gateway.getBytes()[1] + "." + gateway.getBytes()[2] + "." + (value+2));
                     //value++;
                     //clientInterfaces.put(key, value);
                     //Layer??
                    // System.out.println("dst router"+ routers.get(interfacetoRouterIDStr.get(gateway.toString())-1).getRouterId());
                     break;
                 }
                 x++;
             }

             EndDevice destination = new EndDevice(ip, gateway, NetworkLayerServer.endDevices.size()+1);

              if(i==20)
              {
              //      Send the message and recipient IP address to server and a special request "SHOW_ROUTE"
              //     Display routing path, hop count and routing table of each router [You need to receive
              //              all the required info from the server in response to "SHOW_ROUTE" request]
                  smsg=smsg+"SHOW_ROUTE";
                  Packet packet = new Packet(msg,smsg,source.getIpAddress(),destination.getGateway());
                  networkUtility.write(packet);

              }
              else
             {
                 //System.out.println(msg);

                // source.setIpAddress(newSrcIp);
                  Packet packet = new Packet(msg,smsg,source.getIpAddress(),destination.getGateway());
                  networkUtility.write(packet);

             }String delivery=(String)networkUtility.read();
             System.out.println(delivery);
             String sp=(String)networkUtility.read();
             System.out.println(sp);
             System.out.println("\n");
        //16.     If server can successfully send the message, client will get an acknowledgement along with hop count
        ///            Otherwise, client will get a failure message [dropped packet]
         }
        //18. Report average number of hops and drop rate



    }
}
