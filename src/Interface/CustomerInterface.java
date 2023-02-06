package Interface;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CustomerInterface extends Remote {
    public String bookMovieTickets (String customerID,String movieID,String movieName,int numberOfTickets,Boolean serverName) throws RemoteException;
    public String getBookingSchedule (String customerID)  throws RemoteException;
    public String cancelMovieTickets (String customerID,String movieID, String movieName,int numberOfTickets) throws RemoteException;

}
