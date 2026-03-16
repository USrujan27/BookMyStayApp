import java.util.*;

/**
 * BookMyStayApp
 *
 * Demonstrates booking, validation, and cancellation with inventory rollback.
 *
 * Author: Srujan Uppalapu
 * Version: 10.0
 */

/* Custom Exception for invalid booking requests */
class InvalidBookingException extends Exception {
    public InvalidBookingException(String message) {
        super(message);
    }
}

/* Reservation Class */
class Reservation {
    private String guestName;
    private String roomType;
    private String assignedRoomId;
    private double basePrice;
    private boolean cancelled;

    public Reservation(String guestName, String roomType, double basePrice) {
        this.guestName = guestName;
        this.roomType = roomType;
        this.basePrice = basePrice;
        this.assignedRoomId = null;
        this.cancelled = false;
    }

    public String getGuestName() { return guestName; }
    public String getRoomType() { return roomType; }
    public void setAssignedRoomId(String roomId) { this.assignedRoomId = roomId; }
    public String getAssignedRoomId() { return assignedRoomId; }
    public double getBasePrice() { return basePrice; }
    public boolean isCancelled() { return cancelled; }
    public void cancel() { cancelled = true; }

    public void displayReservation() {
        System.out.println("Guest: " + guestName +
                " | Room Type: " + roomType +
                (assignedRoomId != null ? " | Assigned Room ID: " + assignedRoomId : " | Not Assigned") +
                (cancelled ? " | CANCELLED" : ""));
    }
}

/* Room Inventory with validation and rollback support */
class RoomInventory {
    private Map<String, Integer> inventory;
    private Map<String, Stack<String>> releasedRoomIds;

    public RoomInventory() {
        inventory = new HashMap<>();
        releasedRoomIds = new HashMap<>();
        inventory.put("Single Room", 5);
        inventory.put("Double Room", 3);
        inventory.put("Suite Room", 2);
        for (String type : inventory.keySet()) {
            releasedRoomIds.put(type, new Stack<>());
        }
    }

    public boolean allocateRoom(String roomType, String roomId) throws InvalidBookingException {
        if (!inventory.containsKey(roomType)) {
            throw new InvalidBookingException("Invalid room type: " + roomType);
        }
        int available = inventory.get(roomType);
        if (available <= 0) {
            throw new InvalidBookingException("No available rooms for: " + roomType);
        }
        inventory.put(roomType, available - 1);
        return true;
    }

    public void releaseRoom(String roomType, String roomId) {
        inventory.put(roomType, inventory.get(roomType) + 1);
        releasedRoomIds.get(roomType).push(roomId);
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

    public Reservation findReservationByGuest(String guestName) {
        for (Reservation r : confirmedBookings) {
            if (r.getGuestName().equalsIgnoreCase(guestName) && !r.isCancelled()) {
                return r;
            }
        }
        return null;
    }
}

/* Booking Service with allocation */
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
            try {
                String roomId = generateUniqueRoomId(reservation.getRoomType());
                inventory.allocateRoom(reservation.getRoomType(), roomId);
                reservation.setAssignedRoomId(roomId);
                bookingHistory.addReservation(reservation);
                System.out.println("Booking confirmed for " + reservation.getGuestName());
            } catch (InvalidBookingException e) {
                System.out.println("Booking failed for " + reservation.getGuestName() + ": " + e.getMessage());
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

/* Cancellation Service */
class CancellationService {
    private RoomInventory inventory;
    private BookingHistory history;

    public CancellationService(RoomInventory inventory, BookingHistory history) {
        this.inventory = inventory;
        this.history = history;
    }

    public void cancelBooking(String guestName) {
        Reservation reservation = history.findReservationByGuest(guestName);
        if (reservation == null) {
            System.out.println("Cancellation failed: No active reservation found for " + guestName);
            return;
        }
        reservation.cancel();
        inventory.releaseRoom(reservation.getRoomType(), reservation.getAssignedRoomId());
        System.out.println("Reservation for " + guestName + " has been cancelled and inventory updated.");
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
            if (!r.isCancelled()) totalRevenue += r.getBasePrice();
        }
        System.out.println("-------------------------------------------");
        System.out.println("Total confirmed bookings: " + reservations.stream().filter(r -> !r.isCancelled()).count());
        System.out.println("Total revenue (base price only): $" + totalRevenue);
        System.out.println("===========================================");
    }
}

/* Main Application */
public class BookMyStayApp {

    public static void main(String[] args) {
        System.out.println("====================================");
        System.out.println("        Book My Stay App");
        System.out.println("           Version 10.0");
        System.out.println("====================================");

        Queue<Reservation> bookingQueue = new LinkedList<>();
        bookingQueue.offer(new Reservation("Alice", "Single Room", 100));
        bookingQueue.offer(new Reservation("Bob", "Suite Room", 300));
        bookingQueue.offer(new Reservation("Charlie", "Double Room", 180));
        bookingQueue.offer(new Reservation("David", "Suite Room", 300));

        RoomInventory inventory = new RoomInventory();
        BookingHistory bookingHistory = new BookingHistory();

        inventory.displayInventory();

        BookingService bookingService = new BookingService(bookingQueue, inventory, bookingHistory);
        bookingService.processBookings();

        inventory.displayInventory();

        // Cancellation examples
        CancellationService cancellationService = new CancellationService(inventory, bookingHistory);
        cancellationService.cancelBooking("Bob");     // valid cancellation
        cancellationService.cancelBooking("Eve");     // invalid cancellation

        inventory.displayInventory();

        BookingReportService reportService = new BookingReportService(bookingHistory);
        reportService.generateReport();
    }
}