package Server;

import Implementation.Implementation;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;

public class OutremontServer {
    public OutremontServer() {
    }

    public static void main(String[] args) {
        try {
            Implementation obj = new Implementation();
            obj.portsToPing= new int[]{5098, 5099};

            obj.movieData.put("AVATAR", new HashMap<String, Integer>());
            obj.movieData.put("TITANIC", new HashMap<String, Integer>());
            obj.movieData.put("AVENGERS", new HashMap<String, Integer>());
            obj.movieData.get("AVATAR").put("OUTM030223", 40);
            obj.movieData.get("AVENGERS").put("OUTM030223", 30);
            obj.movieData.get("TITANIC").put("OUTM030223", 30);

            Registry registry=LocateRegistry.createRegistry(3003);
            Naming.rebind("Outremont", obj);
            DatagramSocket socket = new DatagramSocket(5097);

            System.out.println("Outremont server ready");
            while(true)
            {

                byte[] responseData = new byte[1024];
                DatagramPacket response = new DatagramPacket(responseData, responseData.length);
                socket.receive(response);
                String responseString =new String(response.getData()).trim();
                String stringToSend="";
                // System.out.println(responseString);
                for (Map.Entry<String, HashMap<String, Integer>> empMap : obj.movieData.entrySet()) {
                    //System.out.println("hit");
                    if(responseString.equals(empMap.getKey())){
                        //  System.out.println(empMap.getKey());
                        HashMap<String, Integer> addMap = empMap.getValue();
                        for ( String key : addMap.keySet() ) {
                            //   System.out.println(key);
                            stringToSend+=key+" ";
                        }
                    }
                }
                System.out.println(stringToSend);
                byte[] b2= stringToSend.getBytes();
                InetAddress ia=InetAddress.getLocalHost();
                DatagramPacket dp1=new DatagramPacket(b2,b2.length,ia,response.getPort());
                socket.send(dp1);
            }


        } catch (Exception e) {
            System.out.println(e);
        }
    }
}