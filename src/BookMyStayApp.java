import java.util.*;

/**
 * BookMyStayApp
 *
 * Demonstrates booking history tracking and reporting for confirmed reservations.
 *
 * Book My Stay Application
 *
 * Author: Srujan Uppalapu
 * Version: 8.0
 */

/* Reservation Class */
class Reservation {

    private String guestName;
    private String roomType;
    private String assignedRoomId;
    private double basePrice;

    public Reservation(String guestName, String roomType, double basePrice) {
        this.guestName = guestName;
        this.roomType = roomType;
        this.basePrice = basePrice;
        this.assignedRoomId = null;
    }

    public String getGuestName() { return guestName; }
    public String getRoomType() { return roomType; }
    public void setAssignedRoomId(String roomId) { this.assignedRoomId = roomId; }
    public String getAssignedRoomId() { return assignedRoomId; }
    public double getBasePrice() { return basePrice; }

    public void displayReservation() {
        System.out.println("Guest: " + guestName +
                " | Room Type: " + roomType +
                (assignedRoomId != null ? " | Assigned Room ID: " + assignedRoomId : " | Not Assigned"));
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

/* Booking History Class */
class BookingHistory {

    private List<Reservation> confirmedBookings;

    public BookingHistory() {
        confirmedBookings = new ArrayList<>();
    }

    public void addReservation(Reservation reservation) {
        confirmedBookings.add(reservation);
    }

    public List<Reservation> getAllReservations() {
        return Collections.unmodifiableList(confirmedBookings);
    }
}

/* Booking Service – handles allocation and history tracking */
class BookingService {

    private Queue<Reservation> bookingQueue;
    private RoomInventory inventory;
    private Map<String, Set<String>> allocatedRoomIds;
    private int roomIdCounter;
    private BookingHistory bookingHistory;

    public BookingService(Queue<Reservation> bookingQueue, RoomInventory inventory, BookingHistory history) {
        this.bookingQueue = bookingQueue;
        this.inventory = inventory;
        this.allocatedRoomIds = new HashMap<>();
        this.roomIdCounter = 100;
        this.bookingHistory = history;
    }

    public void processBookings() {
        System.out.println("\nProcessing booking requests...\n");

        while (!bookingQueue.isEmpty()) {
            Reservation reservation = bookingQueue.poll();
            String type = reservation.getRoomType();

            if (inventory.allocateRoom(type)) {
                String roomId = generateUniqueRoomId(type);
                reservation.setAssignedRoomId(roomId);
                bookingHistory.addReservation(reservation);
                System.out.println("Booking confirmed for " + reservation.getGuestName());
            } else {
                System.out.println("No available rooms for " + reservation.getGuestName() + " (" + type + ")");
            }

            reservation.displayReservation();
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

/* Booking Report Service */
class BookingReportService {

    private BookingHistory bookingHistory;

    public BookingReportService(BookingHistory bookingHistory) {
        this.bookingHistory = bookingHistory;
    }

    public void generateReport() {
        System.out.println("\n========== Booking History Report ==========");
        List<Reservation> reservations = bookingHistory.getAllReservations();
        if (reservations.isEmpty()) {
            System.out.println("No confirmed bookings yet.");
            return;
        }
        double totalRevenue = 0;
        for (Reservation r : reservations) {
            r.displayReservation();
            totalRevenue += r.getBasePrice();
        }
        System.out.println("-------------------------------------------");
        System.out.println("Total confirmed bookings: " + reservations.size());
        System.out.println("Total revenue (base price only): $" + totalRevenue);
        System.out.println("===========================================");
    }
}

/* Main Application */
public class BookMyStayApp {

    public static void main(String[] args) {

        System.out.println("====================================");
        System.out.println("        Book My Stay App");
        System.out.println("           Version 8.0");
        System.out.println("====================================");

        // Initialize booking queue
        Queue<Reservation> bookingQueue = new LinkedList<>();
        bookingQueue.offer(new Reservation("Alice", "Single Room", 100));
        bookingQueue.offer(new Reservation("Bob", "Suite Room", 300));
        bookingQueue.offer(new Reservation("Charlie", "Double Room", 180));
        bookingQueue.offer(new Reservation("David", "Suite Room", 300));
        bookingQueue.offer(new Reservation("Eve", "Suite Room", 300)); // Exceeds availability

        // Initialize inventory and history
        RoomInventory inventory = new RoomInventory();
        BookingHistory bookingHistory = new BookingHistory();

        inventory.displayInventory();

        // Process bookings
        BookingService bookingService = new BookingService(bookingQueue, inventory, bookingHistory);
        bookingService.processBookings();

        // Display inventory after allocation
        inventory.displayInventory();

        // Generate booking report
        BookingReportService reportService = new BookingReportService(bookingHistory);
        reportService.generateReport();
    }
}