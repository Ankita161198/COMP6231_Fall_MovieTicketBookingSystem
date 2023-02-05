package Interface;

import java.rmi.*;
import java.text.ParseException;
import java.util.concurrent.ExecutionException;

public interface AdminInterface extends Remote {

    public String addMovieSlots(String movieID, String movieName, int bookingCapacity) throws RemoteException, ParseException;
    public String removeMovieSlots (String movieID,String movieName) throws RemoteException;
    public String listMovieShowsAvailability (String movieName) throws RemoteException, ExecutionException, InterruptedException;

    public String bookMovieTickets (String customerID,String movieID,String movieName,int numberOfTickets,String serverName) throws RemoteException;
    public String getBookingSchedule (String customerID)  throws RemoteException;
    public String cancelMovieTickets (String customerID,String movieID, String movieName,int numberOfTickets) throws RemoteException;


}
