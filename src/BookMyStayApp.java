import java.util.*;

/**
 * BookMyStayApp
 *
 * Demonstrates reservation confirmation and room allocation with
 * uniqueness enforcement and inventory consistency.
 *
 * Book My Stay Application
 *
 * @author Srujan Uppalapu
 * @version 6.0
 */

/* Reservation Class */
class Reservation {

    private String guestName;
    private String roomType;
    private String assignedRoomId;

    public Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
        this.assignedRoomId = null;
    }

    public String getGuestName() {
        return guestName;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setAssignedRoomId(String roomId) {
        this.assignedRoomId = roomId;
    }

    public void displayConfirmation() {
        if (assignedRoomId != null) {
            System.out.println("Guest: " + guestName + " | Room Type: " + roomType
                    + " | Assigned Room ID: " + assignedRoomId);
        } else {
            System.out.println("Guest: " + guestName + " | Room Type: " + roomType
                    + " | Status: Pending / Not Available");
        }
    }
}

/* Room Inventory Class */
class RoomInventory {

    private Map<String, Integer> inventory;

    public RoomInventory() {
        inventory = new HashMap<>();
        inventory.put("Single Room", 5);
        inventory.put("Double Room", 3);
        inventory.put("Suite Room", 2);
    }

    public int getAvailability(String roomType) {
        return inventory.getOrDefault(roomType, 0);
    }

    public boolean allocateRoom(String roomType) {
        int available = inventory.getOrDefault(roomType, 0);
        if (available > 0) {
            inventory.put(roomType, available - 1);
            return true;
        }
        return false;
    }

    public void displayInventory() {
        System.out.println("\nCurrent Room Inventory:");
        for (Map.Entry<String, Integer> entry : inventory.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue() + " rooms available");
        }
    }
}

/* Booking Service – handles allocation */
class BookingService {

    private Queue<Reservation> bookingQueue;
    private RoomInventory inventory;
    private Map<String, Set<String>> allocatedRoomIds;
    private int roomIdCounter;

    public BookingService(Queue<Reservation> bookingQueue, RoomInventory inventory) {
        this.bookingQueue = bookingQueue;
        this.inventory = inventory;
        this.allocatedRoomIds = new HashMap<>();
        this.roomIdCounter = 100; // Starting room ID number
    }

    public void processBookings() {
        System.out.println("\nProcessing booking requests...\n");

        while (!bookingQueue.isEmpty()) {
            Reservation reservation = bookingQueue.poll();
            String type = reservation.getRoomType();

            if (inventory.allocateRoom(type)) {
                // Generate unique room ID
                String roomId = generateUniqueRoomId(type);
                reservation.setAssignedRoomId(roomId);

                System.out.println("Booking confirmed for " + reservation.getGuestName());
            } else {
                System.out.println("No available rooms for " + reservation.getGuestName() + " (" + type + ")");
            }

            reservation.displayConfirmation();
        }
    }

    private String generateUniqueRoomId(String roomType) {
        allocatedRoomIds.putIfAbsent(roomType, new HashSet<>());
        String roomId;
        do {
            roomId = roomType.substring(0, 2).toUpperCase() + roomIdCounter++;
        } while (allocatedRoomIds.get(roomType).contains(roomId));

        allocatedRoomIds.get(roomType).add(roomId);
        return roomId;
    }
}

/* Main Application Class */
public class BookMyStayApp {

    public static void main(String[] args) {

        System.out.println("====================================");
        System.out.println("        Book My Stay App");
        System.out.println("           Version 6.0");
        System.out.println("====================================");

        // Initialize booking request queue
        Queue<Reservation> bookingQueue = new LinkedList<>();
        bookingQueue.offer(new Reservation("Alice", "Single Room"));
        bookingQueue.offer(new Reservation("Bob", "Suite Room"));
        bookingQueue.offer(new Reservation("Charlie", "Double Room"));
        bookingQueue.offer(new Reservation("David", "Suite Room"));
        bookingQueue.offer(new Reservation("Eve", "Suite Room")); // Will exceed availability

        // Initialize inventory
        RoomInventory inventory = new RoomInventory();
        inventory.displayInventory();

        // Process bookings
        BookingService bookingService = new BookingService(bookingQueue, inventory);
        bookingService.processBookings();

        // Display inventory after allocation
        inventory.displayInventory();

        System.out.println("\nRoom allocation completed successfully.");
    }
}