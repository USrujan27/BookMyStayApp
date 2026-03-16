import java.util.*;
import java.util.concurrent.*;

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
    public boolean isCancelled() { return cancelled; }
    public void cancel() { cancelled = true; }

    public void displayReservation() {
        System.out.println("Guest: " + guestName +
                " | Room Type: " + roomType +
                (assignedRoomId != null ? " | Assigned Room ID: " + assignedRoomId : " | Not Assigned") +
                (cancelled ? " | CANCELLED" : ""));
    }
}

class RoomInventory {
    private final Map<String, Integer> inventory = new HashMap<>();
    private final Map<String, Set<String>> allocatedRoomIds = new HashMap<>();
    private int roomIdCounter = 100;

    public RoomInventory() {
        inventory.put("Single Room", 5);
        inventory.put("Double Room", 3);
        inventory.put("Suite Room", 2);
        for (String type : inventory.keySet()) {
            allocatedRoomIds.put(type, new HashSet<>());
        }
    }

    public synchronized String allocateRoom(String roomType) throws Exception {
        if (!inventory.containsKey(roomType)) throw new Exception("Invalid room type: " + roomType);
        int available = inventory.get(roomType);
        if (available <= 0) throw new Exception("No available rooms for: " + roomType);

        String roomId;
        do {
            roomId = roomType.substring(0, 2).toUpperCase() + roomIdCounter++;
        } while (allocatedRoomIds.get(roomType).contains(roomId));

        allocatedRoomIds.get(roomType).add(roomId);
        inventory.put(roomType, available - 1);
        return roomId;
    }

    public synchronized void releaseRoom(String roomType, String roomId) {
        inventory.put(roomType, inventory.get(roomType) + 1);
        allocatedRoomIds.get(roomType).remove(roomId);
    }

    public synchronized void displayInventory() {
        System.out.println("\nCurrent Room Inventory:");
        for (Map.Entry<String, Integer> entry : inventory.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue() + " rooms available");
        }
    }
}

class BookingHistory {
    private final List<Reservation> confirmedBookings = Collections.synchronizedList(new ArrayList<>());
    public void addReservation(Reservation reservation) { confirmedBookings.add(reservation); }
    public List<Reservation> getAllReservations() {
        synchronized (confirmedBookings) { return new ArrayList<>(confirmedBookings); }
    }
}

class BookingTask implements Runnable {
    private final Reservation reservation;
    private final RoomInventory inventory;
    private final BookingHistory history;

    public BookingTask(Reservation reservation, RoomInventory inventory, BookingHistory history) {
        this.reservation = reservation;
        this.inventory = inventory;
        this.history = history;
    }

    @Override
    public void run() {
        try {
            String roomId = inventory.allocateRoom(reservation.getRoomType());
            reservation.setAssignedRoomId(roomId);
            history.addReservation(reservation);
            System.out.println("Booking confirmed for " + reservation.getGuestName() + " | Room ID: " + roomId);
        } catch (Exception e) {
            System.out.println("Booking failed for " + reservation.getGuestName() + ": " + e.getMessage());
        }
    }
}

public class BookMyStayApp {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("====================================");
        System.out.println("        Book My Stay App");
        System.out.println("           Version 11.0");
        System.out.println("====================================");

        RoomInventory inventory = new RoomInventory();
        BookingHistory bookingHistory = new BookingHistory();

        inventory.displayInventory();

        List<Reservation> requests = Arrays.asList(
                new Reservation("Alice", "Single Room", 100),
                new Reservation("Bob", "Suite Room", 300),
                new Reservation("Charlie", "Double Room", 180),
                new Reservation("David", "Suite Room", 300),
                new Reservation("Eve", "Single Room", 100),
                new Reservation("Frank", "Double Room", 180)
        );

        ExecutorService executor = Executors.newFixedThreadPool(3);
        for (Reservation res : requests) executor.submit(new BookingTask(res, inventory, bookingHistory));

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        inventory.displayInventory();

        System.out.println("\n========== Booking History ==========");
        for (Reservation r : bookingHistory.getAllReservations()) r.displayReservation();
    }
}