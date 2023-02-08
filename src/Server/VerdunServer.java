package Server;

import Implementation.Implementation;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class VerdunServer {
    public static void main(String[] args) {
        try {

            Implementation obj = new Implementation();
            obj.portsToPing= new int[]{5099, 5097};

            SimpleDateFormat formatter = new SimpleDateFormat("ddMMyy");
            Date d = new Date();
            String date=formatter.format(d);
            System.out.println(date);
            obj.movieData.put("AVATAR", new HashMap<String, Integer>());
            obj.movieData.put("TITANIC", new HashMap<String, Integer>());
            obj.movieData.put("AVENGERS", new HashMap<String, Integer>());
            obj.movieData.get("AVATAR").put("VERM"+date, 400);
            obj.movieData.get("AVATAR").put("VERA"+date, 400);
            obj.movieData.get("AVATAR").put("VERE"+date, 400);
            obj.movieData.get("AVENGERS").put("VERM"+date, 400);
            obj.movieData.get("AVENGERS").put("VERA"+date, 400);
            obj.movieData.get("AVENGERS").put("VERE"+date, 400);
            obj.movieData.get("TITANIC").put("VERM"+date, 400);
            obj.movieData.get("TITANIC").put("VERA"+date, 400);
            obj.movieData.get("TITANIC").put("VERE"+date, 400);


            Registry registry=LocateRegistry.createRegistry(3002);
            Naming.rebind("Verdun", obj);
            System.out.println("Verdun server ready");
            DatagramSocket socket = new DatagramSocket(5098);
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
