package Server;

import Implementation.Implementation;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.SimpleDateFormat;
import java.util.*;

public class OutremontServer {
    public OutremontServer() {
    }

    public static void main(String[] args) {
        try {
            Implementation obj = new Implementation();
            obj.portsToPing= new int[]{5098, 5099};
            SimpleDateFormat formatter = new SimpleDateFormat("ddMMyy");
            Date d = new Date();
            String date=formatter.format(d);

            obj.movieData.put("AVATAR", new HashMap<String, Integer>());
            obj.movieData.put("TITANIC", new HashMap<String, Integer>());
            obj.movieData.put("AVENGERS", new HashMap<String, Integer>());
            obj.movieData.get("AVATAR").put("OUTM"+date, 400);
            obj.movieData.get("AVATAR").put("OUTA"+date, 400);
            obj.movieData.get("AVATAR").put("OUTE"+date, 400);
            obj.showSort.put("AVATAR",new ArrayList<>(Arrays.asList("M"+date, "A"+date, "E"+date)));

            obj.movieData.get("AVENGERS").put("OUTM"+date, 400);
            obj.movieData.get("AVENGERS").put("OUTA"+date, 400);
            obj.movieData.get("AVENGERS").put("OUTE"+date, 400);
            obj.showSort.put("AVENGERS",new ArrayList<>(Arrays.asList("M"+date, "A"+date, "E"+date)));

            obj.movieData.get("TITANIC").put("OUTM"+date, 400);
            obj.movieData.get("TITANIC").put("OUTA"+date, 400);
            obj.movieData.get("TITANIC").put("OUTE"+date, 400);
            obj.showSort.put("TITANIC",new ArrayList<>(Arrays.asList("M"+date, "A"+date, "E"+date)));



            Registry registry=LocateRegistry.createRegistry(3003);
            Naming.rebind("Outremont", obj);
            DatagramSocket socket = new DatagramSocket(5097);

            System.out.println("Outremont server ready");
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
