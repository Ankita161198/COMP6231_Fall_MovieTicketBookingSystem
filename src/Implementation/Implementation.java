package Implementation;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.rmi.*;
import java.rmi.server.*;
import java.sql.SQLOutput;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import Interface.AdminInterface;
import Interface.CustomerInterface;

public class Implementation extends UnicastRemoteObject implements AdminInterface, CustomerInterface {


    //LOGGER

    public String message;
    public String movieName;
    public int currentPort;
    public HashMap<String, HashMap<String, Integer>> movieData = new HashMap<>();
    public HashMap<String, HashMap<String, HashMap<String, Integer>>> customerData = new HashMap<>();
    public HashMap<String, HashMap<String, HashMap<String, Integer>>> customerSchedule = new HashMap<>();
    public static HashMap<String, Integer> serverPort = new HashMap<>();
    public  HashMap<String,ArrayList<String> > showSort=new HashMap<>();
    public ArrayList<String> movieArray;
    public int[] portsToPing = new int[2];

    static {
        serverPort.put("ATW", 5099);
        serverPort.put("VER", 5098);
        serverPort.put("OUT", 5097);

    }


    public Implementation() throws RemoteException {
        super();
        this.message = "Hello this works!!!!!!!!!!";
    }


    @Override
    public String addMovieSlots(String movieID, String movieName, int bookingCapacity) throws ParseException {


        if (checkDate(movieID)==7) {
            return "Sorry these slots cannot be added as it exceeds 1 week";
        } else if (checkDate(movieID)==-1) {
            return "You cannot add slots for the previous dates";
        } else {
            int flag = 0;
            if(movieData.containsKey(movieName)){
                if(movieData.get(movieName).containsKey(movieID)){
                    movieData.get(movieName).replace(movieID,bookingCapacity+movieData.get(movieName).get(movieID));
                }else{
                    movieData.get(movieName).put(movieID,bookingCapacity);

                    if(showSort.get(movieName)==null){

                        movieArray=new ArrayList<>();
                        movieArray.add(movieID.substring(3));
                        showSort.put(movieName,movieArray);
                    }
                    else{
                        movieArray.add(movieID.substring(3));
                        showSort.replace(movieName,movieArray);
                        if(showSort.get(movieName).size()>2)
                        {
                            System.out.println("hit");
                            showSort.replace(movieName,sortDates(movieArray));
                        }
                    }
                }
            }else{
                movieData.put(movieName, new HashMap<String, Integer>());
                movieData.get(movieName).put(movieID, bookingCapacity);
            }

            System.out.println(movieData.entrySet());
            System.out.println(showSort.entrySet());
            return bookingCapacity + " slots for movie " + movieName + " by movie ID " + movieID + " have been added";
        }

    }

    @Override
    public String removeMovieSlots(String movieID, String movieName) {
        int flag = 0;
        String nextShow="";
        if(movieData.containsKey(movieName)){
            if(movieData.get(movieName).containsKey(movieID)){
                for(int i=0;i<showSort.get(movieName).size();i++){
                    System.out.println(showSort.get(movieName).get(i));
                    if(movieID.substring(3).equals(showSort.get(movieName).get(i))){
                        System.out.println("line 110");
                        if(i!=showSort.get(movieName).size()-1)
                        {
                            System.out.println("line 113");
                            nextShow=movieID.substring(0,3)+showSort.get(movieName).get(i+1);
                            break;
                        }
                    }
                }
                System.out.println("next show is "+nextShow);
                System.out.println(customerData);
                if(customerData.containsKey(movieName)){
                    if(customerData.get(movieName).containsKey(movieID)){
                        HashMap<String, HashMap<String, Integer>> firstHashMap = customerData.get(movieName);
                        HashMap<String, Integer> secondHashMap = firstHashMap.get(movieID);
                        firstHashMap.remove(movieID);
                        firstHashMap.put(nextShow, secondHashMap);
                        customerData.put(movieName, firstHashMap);
                        for(String key : customerData.get(movieName).get(nextShow).keySet()){
                            System.out.println(key);
                            if(customerSchedule.containsKey(key)){
                                if(customerSchedule.get(key).get(movieName).containsKey(movieID)){
                                    customerSchedule.get(key).get(movieName).remove(movieID);
                                    customerSchedule.get(key).get(movieName).put(nextShow,customerData.get(movieName).get(nextShow).get(key));
                                }
                            }
                        }


                        int sum=0;
                        for(int i:secondHashMap.values()){
                            sum+=i;
                        }
                        if(movieData.containsKey(movieName)){
                            if(movieData.get(movieName).containsKey(nextShow)){
                                movieData.get(movieName).replace(nextShow,movieData.get(movieName).get(nextShow)-sum);
                            }
                        }
                    }
                }

            }
            movieData.get(movieName).remove(movieID);
            flag=1;
        }

        System.out.println(movieData.entrySet());
        System.out.println(customerData.entrySet());
        System.out.println(customerSchedule.entrySet());
        if (flag == 0) {
            return "Sorry this slot is not available";
        }
        //   System.out.println(movieData.entrySet());
        return "The slots for " + movieName + " by movie ID: " + movieID + " have been successfully deleted.";
    }

    @Override
    public String listMovieShowsAvailability(String movieName) throws ExecutionException, InterruptedException {
        //   this.requestType=1;
        this.movieName = movieName;
        String responseString3 = "";
        for (Map.Entry<String, HashMap<String, Integer>> empMap : movieData.entrySet()) {
            System.out.println("hit");
            if (movieName.equals(empMap.getKey())) {
                HashMap<String, Integer> addMap = empMap.getValue();
                for (String key : addMap.keySet()) {
                    responseString3 += key + " ";
                }
            }
        }
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Future<String> response1 = executor.submit(() -> sendRequestToServer(this.portsToPing[0], 1, null, movieName, null, 0));
        Future<String> response2 = executor.submit(() -> sendRequestToServer(this.portsToPing[1], 1, null, movieName, null, 0));


        executor.shutdown();
        String responseString1 = response1.get();
        String responseString2 = response2.get();


        String available = "The available shows for " + movieName + " are:\n" + responseString3 + "\n" + responseString1 + "\n" + responseString2;
        return available;
    }


    public String sendRequestToServer(int port, int requestType, String userID, String movieName, String movieID, int noOfTickets) {

        try (DatagramSocket socket = new DatagramSocket()) {
            String requestString = requestType + ";" + userID + ";" + movieName + ";" + movieID + ";" + noOfTickets;

            byte[] requestData = requestString.getBytes();
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
        }
    }


    // @Override
    public String bookMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets, Boolean serverName) {
        String flag;
        String totalTickets = "";
        System.out.println(customerID + " " + movieID);
        int count=0;
        if(customerSchedule.containsKey(customerID) && !movieID.substring(0, 3).equals(customerID.substring(0, 3)) ){
            if(customerSchedule.get(customerID).containsKey(movieName)){

                HashMap<String, Integer> addMap = customerSchedule.get(customerID).get(movieName);
                for (String key : addMap.keySet()) {
                    if(!customerSchedule.get(customerID).get(movieName).containsKey(movieID)){
                        if(key.substring(3).equals(movieID.substring(3))){
                            return "You already have a booking for this show in your one area area.\nYou cannot book the same show in another area";

                        }
                    }
                }
            }
        }
        if(customerSchedule.containsKey(customerID)){

            for(String movieIndex:customerSchedule.get(customerID).keySet()){
                for(String movieIDindex:customerSchedule.get(customerID).get(movieIndex).keySet()){
                    if(movieIDindex.substring(0,3)!=customerID.substring(0,3))
                    {
                        System.out.println(count);
                        count++;
                        if(count>=3)
                            return "Sorry cannot book more than 3 tickets outside your area";
                    }
                }
            }
        }


        if (!movieID.substring(0, 3).equals(customerID.substring(0, 3))) {
            this.currentPort = serverPort.get(movieID.substring(0, 3));
            flag = sendRequestToServer(this.currentPort, 2, customerID, movieName, movieID, numberOfTickets);
            if (flag.startsWith("1")) {

                String[] temp = flag.split(" ");
                totalTickets = temp[1];
                flag = "1";
            }

            if (Objects.equals(flag, "0")) {
                customerData.put(movieName, new HashMap<String, HashMap<String, Integer>>());
                customerData.get(movieName).put(movieID, new HashMap<String, Integer>());
                customerData.get(movieName).get(movieID).put(customerID, numberOfTickets);

                if (customerSchedule.get(customerID) == null) {

                    customerSchedule.put(customerID, new HashMap<String, HashMap<String, Integer>>());
                    customerSchedule.get(customerID).put(movieName, new HashMap<String, Integer>());
                    customerSchedule.get(customerID).get(movieName).put(movieID, numberOfTickets);
                } else {
                    if(customerSchedule.get(customerID).get(movieName)==null){
                        customerSchedule.get(customerID).put(movieName, new HashMap<String, Integer>());
                        customerSchedule.get(customerID).get(movieName).put(movieID, numberOfTickets);
                    }else{
                        customerSchedule.get(customerID).get(movieName).put(movieID, numberOfTickets);
                    }

                }
            }

        } else {
            if (movieData.containsKey(movieName)) {
                if (movieData.get(movieName).containsKey(movieID)) {
                    if (movieData.get(movieName).get(movieID) >= numberOfTickets) {
                        if(customerData.get(movieName)==null){
                            customerData.put(movieName, new HashMap<String, HashMap<String, Integer>>());
                            customerData.get(movieName).put(movieID, new HashMap<String, Integer>());
                            customerData.get(movieName).get(movieID).put(customerID, numberOfTickets);
                        }else{
                            if(customerData.get(movieName).get(movieID)==null){
                                customerData.get(movieName).put(movieID, new HashMap<String, Integer>());
                                customerData.get(movieName).get(movieID).put(customerID, numberOfTickets);
                            }else{
                                customerData.get(movieName).get(movieID).put(customerID, numberOfTickets);
                            }
                        }
                        if (customerSchedule.get(customerID) == null) {

                            customerSchedule.put(customerID, new HashMap<String, HashMap<String, Integer>>());
                            customerSchedule.get(customerID).put(movieName, new HashMap<String, Integer>());
                            customerSchedule.get(customerID).get(movieName).put(movieID, numberOfTickets);
                        } else {

                            if(customerSchedule.get(customerID).get(movieName)==null){
                                customerSchedule.get(customerID).put(movieName, new HashMap<String, Integer>());
                                customerSchedule.get(customerID).get(movieName).put(movieID, numberOfTickets);
                            }else{
                                customerSchedule.get(customerID).get(movieName).put(movieID, numberOfTickets);
                            }
                        }
                        this.movieData.get(movieName).replace(movieID, movieData.get(movieName).get(movieID) - numberOfTickets);
                        flag = "0";
                    } else {
                        totalTickets = String.valueOf(this.movieData.get(movieName).get(movieID));
                        flag = "1";
                    }
                } else {
                    flag = "4";
                }
            } else {
                flag = "2";
            }
        }
        if (Objects.equals(flag, "1")) {
            return "Can't process this request as seats available for this show are only " + totalTickets;
        } else if (Objects.equals(flag, "4")) {
            return "This movie ID does not exist";
        } else if (Objects.equals(flag, "2")) {
            return "Please Enter the correct movie name";
        } else
            return numberOfTickets + " tickets for movie " + movieName + " by movie ID:" + movieID + " successfully booked for customer with ID: " + customerID;

    }

    //  @Override
    public String getBookingSchedule(String customerID) {
        String dataToSend = "";
        System.out.println(customerSchedule.entrySet());
        if (customerSchedule.containsKey(customerID)) {
            HashMap<String, HashMap<String, Integer>> schedule = customerSchedule.get(customerID);
            dataToSend += "Customer ID: " + customerID + "\n";
            System.out.println("Customer: " + customerID);

            for (Map.Entry<String, HashMap<String, Integer>> innerMapEntry : schedule.entrySet()) {
                String movie_name = innerMapEntry.getKey();
                HashMap<String, Integer> nameMap = innerMapEntry.getValue();
                dataToSend += "\tMovie Name: " + movie_name + "\n";

                for (Map.Entry<String, Integer> appointment : nameMap.entrySet()) {
                    String movie_id = appointment.getKey();
                    Integer noft = appointment.getValue();
                    dataToSend += "\t\tMovie ID: " + movie_id + ", tickets Booked: " + noft + "\n";
                    System.out.println("\t\tMovie ID: " + movie_id + ", tickets Booked: " + noft);
                }
            }
        }


        return dataToSend;
    }

    //  @Override
    public String cancelMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets) {
        String flag = "";
        if (!movieID.substring(0, 3).equals(customerID.substring(0, 3))) {
            this.currentPort = serverPort.get(movieID.substring(0, 3));
            String response = sendRequestToServer(this.currentPort, 3, customerID, movieName, movieID, numberOfTickets);

            if(response.equals("Your tickets have been successfully cancelled")){
                HashMap<String, HashMap<String, Integer>> addMap = this.customerSchedule.get(customerID);
                HashMap<String, Integer> innerMap = addMap.get(movieName);
                if (innerMap.get(movieID) - numberOfTickets == 0) {
                    customerData.get(movieName).remove(movieID);
                    customerSchedule.get(customerID).get(movieName).remove(movieID);


                    return "Your tickets have been successfully cancelled";
                } else {
                    customerData.get(movieName).get(movieID).replace(customerID, innerMap.get(movieID) - numberOfTickets);
                    customerSchedule.get(customerID).get(movieName).replace(movieID, innerMap.get(movieID) - numberOfTickets);


                    return "Your tickets have been successfully cancelled";
                }

            }else{
                return response;
            }

        } else {
            if (customerSchedule.containsKey(customerID)) {
                HashMap<String, HashMap<String, Integer>> addMap = this.customerSchedule.get(customerID);
                if (addMap.containsKey(movieName)) {
                    HashMap<String, Integer> innerMap = addMap.get(movieName);
                    if (innerMap.containsKey(movieID)) {
                        if (numberOfTickets < innerMap.get(movieID)) {
                            if (innerMap.get(movieID) - numberOfTickets == 0) {
                                customerData.get(movieName).remove(movieID);
                                movieData.get(movieName).replace(movieID, movieData.get(movieName).get(movieID) + (innerMap.get(movieID) - numberOfTickets));
                                customerSchedule.get(customerID).get(movieName).remove(movieID);
                                return "Your tickets have been successfully cancelled";
                            } else {
                                customerData.get(movieName).get(movieID).replace(customerID, innerMap.get(movieID) - numberOfTickets);
                                movieData.get(movieName).replace(movieID, movieData.get(movieName).get(movieID) + (innerMap.get(movieID) - numberOfTickets));
                                customerSchedule.get(customerID).get(movieName).replace(movieID, innerMap.get(movieID) - numberOfTickets);
                                return "Your tickets have been successfully cancelled";
                            }

                        } else {
                            return "The number of tickets you want to cancel is more exceeds your booking";
                        }
                    } else {
                        return "Please enter the correct Movie ID";
                    }
                } else {
                    return "Please enter the correct movie name";
                }
            } else {
                return "There are no bookings available for this customer ID";
            }
        }


    }

    public int checkDate(String movieID) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("ddMMyy");
        Date cd = new Date();
        String cd_1 = formatter.format(cd);
        Date currentDate = new SimpleDateFormat("ddMMyy").parse(cd_1);
        Date userDate = new SimpleDateFormat("ddMMyy").parse(movieID.substring(4, movieID.length()));
        Calendar c = Calendar.getInstance();
        c.setTime(currentDate);
        c.add(Calendar.DATE, 7);


        if(userDate.before(currentDate)){
            return -1;
        }


        if (c.getTime().compareTo(userDate) < 0) {
            return 7;
        } else {
            return 0;
        }


    }

    public String receiveFromServerListShows(String movieName){
        String stringToSend="";
        if(this.movieData.containsKey(movieName)){

            HashMap<String, Integer> addMap = this.movieData.get(movieName);
            for ( String key : addMap.keySet() ) {
                stringToSend+=key+" ";
            }
        }

        return stringToSend;
    }
    public String receiveFromServerBookTickets(String customerID, String movieID, String movieName, int numberOfTickets){
        String stringToSend="0";
        System.out.println(movieData.entrySet());
        if(movieData.containsKey(movieName)) {
            HashMap<String, Integer> addMap = movieData.get(movieName);
            if (addMap.containsKey(movieID)) {
                if (addMap.get(movieID) >= numberOfTickets) {
                    this.customerData.put(movieName, new HashMap<String, HashMap<String, Integer>>());
                    this.customerData.get(movieName).put(movieID, new HashMap<String, Integer>());
                    this.customerData.get(movieName).get(movieID).put(customerID, numberOfTickets);

                    if (this.customerSchedule.get(customerID) == null) {

                        this.customerSchedule.put(customerID, new HashMap<String, HashMap<String, Integer>>());
                        this.customerSchedule.get(customerID).put(movieName, new HashMap<String, Integer>());
                        this.customerSchedule.get(customerID).get(movieName).put(movieID, numberOfTickets);
                    } else {

                        this.customerSchedule.get(customerID).put(movieName, new HashMap<String, Integer>());
                        this.customerSchedule.get(customerID).get(movieName).put(movieID, numberOfTickets);
                    }
                    this.movieData.get(movieName).replace(movieID, addMap.get(movieID) - numberOfTickets);
                    stringToSend = "0";

                } else {
                    stringToSend = "1 "+this.movieData.get(movieName).get(movieID);
                }
            } else {
                stringToSend = "4";
            }
        }
        else{
            stringToSend="2";
        }
        return stringToSend;
    }

    public String receiveFromServerCancelTickets(String userID, String movieID, String movieName, int numberOfTickets) {
        String stringToSend = "";
        if (this.customerSchedule.containsKey(userID)) {
            HashMap<String, HashMap<String, Integer>> addMap = this.customerSchedule.get(userID);
            if (addMap.containsKey(movieName)) {
                HashMap<String, Integer> innerMap = addMap.get(movieName);
                if (innerMap.containsKey(movieID)) {
                    if (numberOfTickets < innerMap.get(movieID)) {
                        if (innerMap.get(movieID) - numberOfTickets == 0) {
                            this.customerData.get(movieName).remove(movieID);
                            this.movieData.get(movieName).replace(movieID, this.movieData.get(movieName).get(movieID) + (innerMap.get(movieID) - numberOfTickets));
                            this.customerSchedule.get(userID).get(movieName).remove(movieID);
                            stringToSend = "Your tickets have been successfully cancelled";
                        } else {
                            this.customerData.get(movieName).get(movieID).replace(userID, innerMap.get(movieID) - numberOfTickets);
                            this.movieData.get(movieName).replace(movieID, this.movieData.get(movieName).get(movieID) + (innerMap.get(movieID) - numberOfTickets));
                            this.customerSchedule.get(userID).get(movieName).replace(movieID, innerMap.get(movieID) - numberOfTickets);
                            stringToSend = "Your tickets have been successfully cancelled";
                        }

                    } else {
                        stringToSend = "The number of tickets you want to cancel is more exceeds your booking";
                    }
                } else {
                    stringToSend = "Please enter the correct Movie ID";
                }
            } else {
                stringToSend = "Please enter the correct movie name";
            }
        } else {
            stringToSend = "There are no bookings available for this customer ID";
        }
        return stringToSend;
    }

    public static ArrayList sortDates(ArrayList arr){
        Collections.sort(arr, new Comparator<String>() {
            SimpleDateFormat format = new SimpleDateFormat("yyMMdd");

            @Override
            public int compare(String date1, String date2) {
                try {
                    String datePart1 = date1.substring(1);
                    String datePart2 = date2.substring(1);

                    Date dateOne = format.parse(datePart1);
                    Date dateTwo = format.parse(datePart2);

                    int dateCompare = dateOne.compareTo(dateTwo);
                    if (dateCompare != 0) {
                        return dateCompare;
                    } else {
                        char time1 = date1.charAt(0);
                        char time2 = date2.charAt(0);
                        if (time1 == 'M') {
                            if (time2 == 'M') {
                                return 0;
                            } else {
                                return -1;
                            }
                        } else if (time1 == 'A') {
                            if (time2 == 'M') {
                                return 1;
                            } else if (time2 == 'A') {
                                return 0;
                            } else {
                                return -1;
                            }
                        } else {
                            if (time2 == 'M' || time2 == 'A') {
                                return 1;
                            } else {
                                return 0;
                            }
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return 0;
            }
        });

        return arr;
    }


}
