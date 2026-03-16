import java.util.LinkedList;
import java.util.Queue;

/**
 * BookMyStayApp
 *
 * Demonstrates booking request handling using a Queue
 * to maintain First-Come-First-Served order.
 *
 * Book My Stay Application
 *
 * @author Srujan Uppalapu
 * @version 5.0
 */

/* Reservation Class */
class Reservation {

    private String guestName;
    private String roomType;

    public Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
    }

    public String getGuestName() {
        return guestName;
    }

    public String getRoomType() {
        return roomType;
    }

    public void displayReservation() {
        System.out.println("Guest: " + guestName + " | Requested Room: " + roomType);
    }
}

/* Booking Request Queue */
class BookingRequestQueue {

    private Queue<Reservation> requestQueue;

    public BookingRequestQueue() {
        requestQueue = new LinkedList<>();
    }

    /* Add booking request */
    public void addRequest(Reservation reservation) {
        requestQueue.offer(reservation);
        System.out.println("Booking request added for " + reservation.getGuestName());
    }

    /* Display queued requests */
    public void displayRequests() {

        System.out.println("\nCurrent Booking Requests (FIFO Order):");

        for (Reservation r : requestQueue) {
            r.displayReservation();
        }
    }
}

/* Main Application Class */
public class BookMyStayApp {

    public static void main(String[] args) {

        System.out.println("====================================");
        System.out.println("        Book My Stay App");
        System.out.println("           Version 5.0");
        System.out.println("====================================");

        // Initialize booking queue
        BookingRequestQueue bookingQueue = new BookingRequestQueue();

        // Guest booking requests
        Reservation r1 = new Reservation("Alice", "Single Room");
        Reservation r2 = new Reservation("Bob", "Suite Room");
        Reservation r3 = new Reservation("Charlie", "Double Room");

        // Add requests to queue
        bookingQueue.addRequest(r1);
        bookingQueue.addRequest(r2);
        bookingQueue.addRequest(r3);

        // Display queue
        bookingQueue.displayRequests();

        System.out.println("\nRequests stored successfully. Waiting for allocation system.");
    }
}