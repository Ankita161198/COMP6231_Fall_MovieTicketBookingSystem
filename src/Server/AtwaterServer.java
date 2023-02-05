package Server;

import Implementation.Implementation;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class AtwaterServer {
    public AtwaterServer() {
    }

    private static final int NUM_THREADS = 2;

    public static void main(String[] args) {


        try {
            Implementation obj = new Implementation();
            obj.portsToPing= new int[]{5098, 5097};
            obj.movieData.put("AVATAR", new HashMap<String, Integer>());
            obj.movieData.put("TITANIC", new HashMap<String, Integer>());
            obj.movieData.put("AVENGERS", new HashMap<String, Integer>());
            obj.movieData.get("AVATAR").put("ATWM030223", 40);
            obj.movieData.get("AVENGERS").put("ATWM030223", 40);
            obj.movieData.get("TITANIC").put("ATWM030223", 40);
            Registry registry = LocateRegistry.createRegistry(3001);
            Naming.rebind("Atwater", obj);
            System.out.println("Atwater server ready");
            DatagramSocket socket = new DatagramSocket(5099);
            while(true)
            {
                byte[] responseData = new byte[1024];
                DatagramPacket response = new DatagramPacket(responseData, responseData.length);
                socket.receive(response);
                String responseString =new String(response.getData()).trim();
                String stringToSend="";
                for (Map.Entry<String, HashMap<String, Integer>> empMap : obj.movieData.entrySet()) {
                   // System.out.println("hit");
                    if(responseString.equals(empMap.getKey())){
                        HashMap<String, Integer> addMap = empMap.getValue();
                        for ( String key : addMap.keySet() ) {
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
