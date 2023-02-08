package Client;

import Interface.AdminInterface;
import Interface.CustomerInterface;

import java.rmi.Naming;
import java.sql.SQLOutput;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try {
          //  AdminInterface adminInterface = (AdminInterface) Naming.lookup("Hello");
            String message;
            Scanner sc = new Scanner(System.in);
            String movieName="";
            String movieID="";
            System.out.println("Enter User ID:");
            String userID=sc.nextLine();
            String userType=checkUserID(userID);
            if(userType==null){

            } else if (userType.equals("Admin")) {
                String location=checkLocation(userID);
                System.out.println(location);
                AdminInterface adminInterface = (AdminInterface) Naming.lookup(location);

              //  System.out.println( adminInterface.say("hello"));
                while(true){
                    System.out.println(displayAdminMenu());
                    int choice=sc.nextInt();
                    sc.nextLine();
                    switch(choice){
                        case 1:
                            System.out.println("Enter Movie ID : ");
                            movieID=sc.nextLine();
                            System.out.println("Enter movie name : ");
                            movieName=sc.nextLine();
                            System.out.println("Enter capacity : ");
                            int capacity=sc.nextInt();
                            if(movieID.substring(0,3).equals(userID.substring(0,3))){
                                System.out.println(adminInterface.addMovieSlots(movieID,movieName,capacity));
                            }else{
                                System.out.println("Please enter the correct ID for the "+location);
                            }
                            break;
                        case 2:
                            System.out.println("Enter the movie name you would like to remove");
                            movieName=sc.nextLine();
                            System.out.println("Enter the movie ID for " +movieName);
                            movieID=sc.nextLine();
                            System.out.println(adminInterface.removeMovieSlots(movieID,movieName));
                            break;
                        case 3:
                            System.out.println("Enter movie name");
                            movieName=sc.nextLine();
                            System.out.println(adminInterface.listMovieShowsAvailability(movieName));
                            break;
                        case 4:
                            System.out.println("Enter the Customer ID");
                            String customerID=sc.nextLine();
                            if(checkUserID(customerID).equals("Customer")){
                                System.out.println("Enter Movie ID : ");
                                movieID=sc.nextLine();
                                System.out.println("Enter movie name : ");
                                movieName=sc.nextLine();
                                System.out.println("Enter number of tickets you would like to book : ");
                                int numberOfTickets=sc.nextInt();
                                System.out.println(adminInterface.bookMovieTickets(customerID, movieID,movieName,numberOfTickets,checkSameServer(movieID,userID)));
                            }else{
                                System.out.println("Please enter a valid customer ID");
                            }
                            break;
                        case 5:

                            System.out.println("Enter the Customer ID");
                            String customerID_s=sc.nextLine();
                            if(checkUserID(customerID_s).equals("Customer")){
                                System.out.println(adminInterface.getBookingSchedule(customerID_s));
                            }else{
                                System.out.println("Please enter a valid customer ID");
                            }
                            break;
                        case 6:
                            System.out.println("Enter the Customer ID");
                            String customerID_c=sc.nextLine();
                            if(checkUserID(customerID_c).equals("Customer")){
                                System.out.println("----------------- YOUR BOOKING SCHEDULE --------------------");
                                System.out.println(adminInterface.getBookingSchedule(customerID_c));
                                System.out.println("------------------------------------------------------------");
                                System.out.println("Please enter the movie name you want to cancel the tickets for:-");
                                movieName=sc.nextLine();
                                System.out.println("Please enter movie ID for the same");
                                movieID=sc.nextLine();
                                System.out.println("Please enter the number of tickets you want to cancel");
                                int numberOfTickets = sc.nextInt();
                                System.out.println(adminInterface.cancelMovieTickets(customerID_c,movieID,movieName,numberOfTickets));
                                System.out.println("");

                            }else{
                                System.out.println("Please enter a valid customer ID");
                            }
                            break;
                        case 7:
                            System.exit(0);
                    }
                }


            } else if (userType.equals("Customer")) {

                String location=checkLocation(userID);
//                System.out.println(location);
                CustomerInterface customerInterface = (CustomerInterface) Naming.lookup(location);
                while(true){
                    System.out.println(displayCustomerMenu());
                    int choice=sc.nextInt();
                    switch(choice){
                        case 1:
                            System.out.println("Enter Movie ID : ");
                            sc.nextLine();
                            movieID=sc.nextLine();
                            System.out.println("Enter movie name : ");
                            movieName=sc.nextLine();
                            System.out.println("Enter number of tickets you would like to book : ");
                            int numberOfTickets=sc.nextInt();
                            System.out.println(customerInterface.bookMovieTickets(userID, movieID,movieName,numberOfTickets,checkSameServer(movieID,userID)));

                            break;
                        case 2:
                            System.out.println(customerInterface.getBookingSchedule(userID));

                            break;
                        case 3:
                            System.out.println("----------------- YOUR BOOKING SCHEDULE --------------------");
                            System.out.println(customerInterface.getBookingSchedule(userID));
                            System.out.println("------------------------------------------------------------");
                            System.out.println("Please enter the movie name you want to cancel the tickets for:-");
                            sc.nextLine();
                            movieName=sc.nextLine();
                            System.out.println("Please enter movie ID for the same");
                            movieID=sc.nextLine();
                            System.out.println("Please enter the number of tickets you want to cancel");
                            numberOfTickets=sc.nextInt();
                            System.out.println(customerInterface.cancelMovieTickets(userID,movieID,movieName,numberOfTickets));
                            System.out.println("");
                            break;
                        case 4:
                            System.exit(0);
                            break;
                    }
                }


            }


        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static String checkUserID(String userID){

        if(userID.charAt(3)=='A'){
            return "Admin";
        }else if(userID.charAt(3)=='C'){
            return "Customer";
        }else{
            return null;
        }

    }

    public static String checkLocation(String userID){
        if(userID.substring(0,3).equals("ATW")){
            return "Atwater";
        }else if(userID.substring(0,3).equals("VER")){
            return "Verdun";
        } else if (userID.substring(0,3).equals("OUT")) {
            return "Outremont";
        }else{
            return null;
        }

    }
    public static String displayAdminMenu(){
        return "1.Add Movie Slots\n"+
                "2.Remove Movie Slots\n"+
                "3.List movie shows availability\n"+
                "4.Book movie tickets\n"+
                "5.Get booking schedule\n"+
                "6.Cancel movie booking\n"+
                "7.Exit\n";
    }

    public static String displayCustomerMenu(){
        return "1.Book movie tickets\n"+
                "2.Get booking schedule\n"+
                "3.Cancel movie booking\n"+
                "4.Exit\n";
    }

    public static boolean checkSameServer(String movieName,String userID){
        if(userID.substring(0,3)==movieName.substring(0,3)){
            return true;
        }else {
            return false;
        }
    }

}
