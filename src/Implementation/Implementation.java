package Implementation;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.rmi.*;
import java.rmi.server.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import Interface.AdminInterface;
import Interface.CustomerInterface;

public class Implementation extends UnicastRemoteObject implements AdminInterface,CustomerInterface {

    public String message;
    public String movieID;
    public String movieName;

    public int bookingCapacity;
    public String serverName;
    public HashMap<String, HashMap<String, Integer>> movieData = new HashMap<>();

    public HashMap<String, HashMap<String, HashMap<String,Integer>>> customerData = new HashMap<>();
    //CustomerId-->MovieName-->MovieId-->Noofticketsbooked;

    public HashMap<String, HashMap<String, HashMap<String,Integer>>> customerSchedule = new HashMap<>();

    //Moviename-->MovieID-->customerID->>no-of-tickets;
    public static HashMap<String,Integer> serverPort =new HashMap<>();


    public int[] portsToPing=new int[2];

    static {
        serverPort.put("ATW",5099);
        serverPort.put("VER",5098);
        serverPort.put("OUT",5097);

    }



    public Implementation() throws RemoteException {
        super();
        this.message = "Hello this works!!!!!!!!!!";
    }


    @Override
    public String addMovieSlots(String movieID, String movieName, int bookingCapacity) throws ParseException {


        if (!checkDate(movieID)) {
            return "Sorry these slots cannot be added as it exceeds 1 week";
        } else {
            int capacity = 0;
            int flag = 0;

            for (Map.Entry<String, HashMap<String, Integer>> empMap : this.movieData.entrySet()) {
                //System.out.println(movieName+" "+empMap.getKey());
                if (movieName.equals(empMap.getKey())) {
                    HashMap<String, Integer> addMap = empMap.getValue();

                    if (addMap.containsKey(movieID)) {
                        int previousCapacity = addMap.get(movieID);
                        addMap.replace(movieID, previousCapacity + bookingCapacity);
                    } else {

                        movieData.put(movieName, new HashMap<String, Integer>());
                        movieData.get(movieName).put(movieID, bookingCapacity);
                    }

                }
                flag = 1;

            }
            if (flag == 0) {
                movieData.put(movieName, new HashMap<String, Integer>());
                movieData.get(movieName).put(movieID, bookingCapacity);
            }
            System.out.println(movieData.entrySet());
            return bookingCapacity + " slots for movie " + movieName + " by movie ID " + movieID + " have been added";
        }

    }

    @Override
    public String removeMovieSlots(String movieID, String movieName) {
        int flag = 0;
        for (Map.Entry<String, HashMap<String, Integer>> empMap : this.movieData.entrySet()) {
            if (movieName.equals(empMap.getKey())) {
                HashMap<String, Integer> addMap = empMap.getValue();
                if (addMap.containsKey(movieID)) {
                    this.movieData.get(movieName).remove(movieID);
                    System.out.println("hit");
                    flag = 1;
                }
            }

        }
        if (flag == 0) {
           return "Sorry this slot is not available";
        }
        System.out.println(movieData.entrySet());
        return "The slots for " + movieName + " by movie ID: " +movieID+" have been successfully deleted." ;
    }

    @Override
    public String listMovieShowsAvailability(String movieName) throws ExecutionException, InterruptedException {

        //StringBuilder responseBuilder = new StringBuilder();
        String responseString3="";
        for (Map.Entry<String, HashMap<String, Integer>> empMap : movieData.entrySet()) {
            System.out.println("hit");
            if(movieName.equals(empMap.getKey())){
                //  System.out.println(empMap.getKey());
                HashMap<String, Integer> addMap = empMap.getValue();
                for ( String key : addMap.keySet() ) {
                    responseString3+=key+" ";
                }
            }
        }
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Future<String> response1 = executor.submit(() -> sendRequestToServer(movieName, this.portsToPing[0]));
        Future<String> response2 =executor.submit(() -> sendRequestToServer(movieName,this.portsToPing[1]));


        executor.shutdown();
        String responseString1 = response1.get();
        String responseString2 = response2.get();


        String available= "The available shows for " +movieName+" are:\n"+responseString3+"\n"+ responseString1 +"\n" + responseString2;
        //System.out.println("avaialble is: "+available);
        return available;
    }


    public String sendRequestToServer(String movieName, int port) {
        try (DatagramSocket socket = new DatagramSocket()) {
            byte[] requestData = movieName.getBytes();
            InetAddress serverAddress = InetAddress.getLocalHost();
            DatagramPacket request = new DatagramPacket(requestData, requestData.length, serverAddress, port);
            System.out.println(request);
            socket.send(request);

            byte[] responseData = new byte[1024];
            DatagramPacket response = new DatagramPacket(responseData, responseData.length);
            socket.receive(response);

            return new String(response.getData()).trim();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }}


    // @Override
    public String bookMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets,String serverName) {
        this.serverName = serverName;
        int flag = 0;
        for (Map.Entry<String, HashMap<String, Integer>> empMap : this.movieData.entrySet()) {
            if (movieName.equals(empMap.getKey())) {
                HashMap<String, Integer> addMap = empMap.getValue();
                if (addMap.containsKey(movieID)) {
                    if (addMap.get(movieID) >= numberOfTickets) {
                        customerData.put(movieName, new HashMap<String, HashMap<String,Integer>>());
                        customerData.get(movieName).put(movieID, new HashMap<String,Integer>());
                        customerData.get(movieName).get(movieID).put(customerID, numberOfTickets);

                        customerData.put(customerID, new HashMap<String, HashMap<String,Integer>>());
                        customerData.get(customerID).put(movieID, new HashMap<String,Integer>());
                        customerData.get(customerID).get(movieID).put(movieName, numberOfTickets);
                        this.movieData.get(movieName).replace(movieID, addMap.get(movieID) - numberOfTickets);
                        flag=0;
                        break;

                    } else {
                        flag = 1;
                        break;
                    }
                }else{
                    flag=4;
                }
            }
            else {
                flag = 2;
            }


        }
        System.out.println(customerData.entrySet());
        System.out.println(movieData.entrySet());

         if (flag == 1) {
            return "Can't process this request as seats available for this show are" + this.movieData.get(movieName).get(movieID);

        } else if (flag==4) {
             return "This movie ID does not exist";
         } else if (flag == 2) {
            return "Please Enter the correct movie name";

        }else
            return numberOfTickets + " tickets for movie " + movieName + " by movie ID:" + movieID + " successfully booked for customer with ID: " + customerID;

    }

    //  @Override
    public String getBookingSchedule(String customerID) {
        String dataToSend="";

        for (Map.Entry<String, HashMap<String, HashMap<String,Integer>>> empMap : customerSchedule.entrySet()) {

            if(customerID.equals(empMap.getKey())){

                    HashMap<String, HashMap<String,Integer>> addMap = empMap.getValue();
                for (Map.Entry<String, Integer> last : addMap.get(customerID).entrySet()) {
                    HashMap<String, HashMap<String,Integer>> entry = empMap.getValue();

                    for ( String key : entry.keySet() ) {
                        dataToSend+=entry.entrySet()+"\n";
                    }
                }

            }
        }

        return dataToSend;
    }

    //  @Override
    public String cancelMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets) {
        return null;
    }
    public boolean checkDate(String movieID) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("ddMMyy");
        Date cd = new Date();
        String cd_1=formatter.format(cd);
        Date currentDate=new SimpleDateFormat("ddMMyy").parse(cd_1);
        Date userDate=new SimpleDateFormat("ddMMyy").parse(movieID.substring(4,movieID.length()));
        Calendar c=Calendar.getInstance();
        c.setTime(currentDate);
        c.add(Calendar.DATE,7);
        if(c.getTime().compareTo(userDate)<0){
            return false;
        }else{
            return true;
        }



    }

}
