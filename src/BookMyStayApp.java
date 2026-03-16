import java.io.*;
import java.util.*;
import java.util.concurrent.*;

// Reservation class must implement Serializable for persistence
class Reservation implements Serializable {
    private static final long serialVersionUID = 1L;

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

// RoomInventory with Serializable
class RoomInventory implements Serializable {
    private static final long serialVersionUID = 1L;

    private Map<String, Integer> inventory = new HashMap<>();
    private Map<String, Set<String>> allocatedRoomIds = new HashMap<>();
    private int roomIdCounter = 100;

    public RoomInventory() {
        inventory.put("Single Room", 5);
        inventory.put("Double Room", 3);
        inventory.put("Suite Room", 2);
        for (String type : inventory.keySet()) allocatedRoomIds.put(type, new HashSet<>());
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

// BookingHistory implements Serializable
class BookingHistory implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<Reservation> confirmedBookings = new ArrayList<>();

    public void addReservation(Reservation reservation) { confirmedBookings.add(reservation); }
    public List<Reservation> getAllReservations() { return new ArrayList<>(confirmedBookings); }
}

// Runnable task for booking
class BookingTask implements Runnable {
    private Reservation reservation;
    private RoomInventory inventory;
    private BookingHistory history;

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

// Persistence Service
class PersistenceService {
    private static final String FILE_NAME = "bookmystay_state.ser";

    public static void saveState(RoomInventory inventory, BookingHistory history) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(inventory);
            oos.writeObject(history);
            System.out.println("\nSystem state saved to " + FILE_NAME);
        } catch (IOException e) {
            System.out.println("Error saving state: " + e.getMessage());
        }
    }

    public static Object[] loadState() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return null;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            RoomInventory inventory = (RoomInventory) ois.readObject();
            BookingHistory history = (BookingHistory) ois.readObject();
            System.out.println("\nSystem state restored from " + FILE_NAME);
            return new Object[]{inventory, history};
        } catch (Exception e) {
            System.out.println("Error loading state: " + e.getMessage());
            return null;
        }
    }
}

// Main Application
public class BookMyStayApp {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("====================================");
        System.out.println("        Book My Stay App");
        System.out.println("           Version 12.0");
        System.out.println("====================================");

        // Attempt to restore previous state
        Object[] restoredState = PersistenceService.loadState();
        RoomInventory inventory = restoredState != null ? (RoomInventory) restoredState[0] : new RoomInventory();
        BookingHistory bookingHistory = restoredState != null ? (BookingHistory) restoredState[1] : new BookingHistory();

        inventory.displayInventory();

        // Simulate booking requests
        List<Reservation> requests = Arrays.asList(
                new Reservation("Alice", "Single Room", 100),
                new Reservation("Bob", "Suite Room", 300),
                new Reservation("Charlie", "Double Room", 180)
        );

        ExecutorService executor = Executors.newFixedThreadPool(3);
        for (Reservation r : requests) executor.submit(new BookingTask(r, inventory, bookingHistory));

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        inventory.displayInventory();

        System.out.println("\n========== Booking History ==========");
        for (Reservation r : bookingHistory.getAllReservations()) r.displayReservation();

        // Save system state for future recovery
        PersistenceService.saveState(inventory, bookingHistory);
    }
}