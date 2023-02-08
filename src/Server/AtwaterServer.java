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
import java.text.SimpleDateFormat;
import java.util.Date;
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
            SimpleDateFormat formatter = new SimpleDateFormat("ddMMyy");
            Date d = new Date();
            String date=formatter.format(d);
            System.out.println(date);
            obj.movieData.put("AVATAR", new HashMap<String, Integer>());
            obj.movieData.put("TITANIC", new HashMap<String, Integer>());
            obj.movieData.put("AVENGERS", new HashMap<String, Integer>());
            obj.movieData.get("AVATAR").put("ATWM"+date, 400);
            obj.movieData.get("AVATAR").put("ATWA"+date, 400);
            obj.movieData.get("AVATAR").put("ATWE"+date, 400);
            obj.movieData.get("AVENGERS").put("ATWM"+date, 400);
            obj.movieData.get("AVENGERS").put("ATWA"+date, 400);
            obj.movieData.get("AVENGERS").put("ATWE"+date, 400);
            obj.movieData.get("TITANIC").put("ATWM"+date, 400);
            obj.movieData.get("TITANIC").put("ATWA"+date, 400);
            obj.movieData.get("TITANIC").put("ATWE"+date, 400);
            Registry registry = LocateRegistry.createRegistry(3001);
            Naming.rebind("Atwater", obj);
            System.out.println("Atwater server ready");

            DatagramSocket socket = new DatagramSocket(5099);
            while(true)
            {
                byte[] responseData = new byte[1024];
                DatagramPacket response = new DatagramPacket(responseData, responseData.length);
                socket.receive(response);
                String[] responseString =new String(response.getData()).trim().split(";");
                int port=Integer.parseInt(responseString[0]);
                String userID=responseString[1];
                String movieName=responseString[2];
                String movieID=responseString[3];
                int numberOfTickets=Integer.parseInt(responseString[4]);

                String stringToSend="";

                switch (port){
                    case 1:
                        stringToSend=obj.receiveFromServerListShows(movieName);
                        break;
                    case 2:
                        stringToSend=obj.receiveFromServerBookTickets(userID, movieID, movieName,numberOfTickets);
                        break;
                    case 3:
                        stringToSend=obj.receiveFromServerCancelTickets(userID, movieID, movieName,numberOfTickets);
                        break;
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
