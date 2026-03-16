import java.util.*;

/**
 * BookMyStayApp
 *
 * Demonstrates add-on service selection for existing reservations.
 *
 * Book My Stay Application
 *
 * Author: Srujan Uppalapu
 * Version: 7.0
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

/* Add-On Service Class */
class AddOnService {

    private String serviceName;
    private double cost;

    public AddOnService(String serviceName, double cost) {
        this.serviceName = serviceName;
        this.cost = cost;
    }

    public String getServiceName() { return serviceName; }
    public double getCost() { return cost; }

    public void displayService() {
        System.out.println(serviceName + " ($" + cost + ")");
    }
}

/* Add-On Service Manager */
class AddOnServiceManager {

    private Map<String, List<AddOnService>> reservationServices;

    public AddOnServiceManager() {
        reservationServices = new HashMap<>();
    }

    public void addService(Reservation reservation, AddOnService service) {
        reservationServices.putIfAbsent(reservation.getAssignedRoomId(), new ArrayList<>());
        reservationServices.get(reservation.getAssignedRoomId()).add(service);
        System.out.println("Added service " + service.getServiceName() + " for " + reservation.getGuestName());
    }

    public void displayServices(Reservation reservation) {
        List<AddOnService> services = reservationServices.get(reservation.getAssignedRoomId());
        if (services == null || services.isEmpty()) {
            System.out.println(reservation.getGuestName() + " has no add-on services.");
            return;
        }
        System.out.println("Add-on services for " + reservation.getGuestName() + ":");
        double totalCost = 0;
        for (AddOnService s : services) {
            s.displayService();
            totalCost += s.getCost();
        }
        System.out.println("Total Add-On Cost: $" + totalCost);
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
        this.roomIdCounter = 100;
    }

    public void processBookings() {
        System.out.println("\nProcessing booking requests...\n");

        while (!bookingQueue.isEmpty()) {
            Reservation reservation = bookingQueue.poll();
            String type = reservation.getRoomType();

            if (inventory.allocateRoom(type)) {
                String roomId = generateUniqueRoomId(type);
                reservation.setAssignedRoomId(roomId);
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

/* Main Application */
public class BookMyStayApp {

    public static void main(String[] args) {

        System.out.println("====================================");
        System.out.println("        Book My Stay App");
        System.out.println("           Version 7.0");
        System.out.println("====================================");

        // Initialize booking queue
        Queue<Reservation> bookingQueue = new LinkedList<>();
        bookingQueue.offer(new Reservation("Alice", "Single Room", 100));
        bookingQueue.offer(new Reservation("Bob", "Suite Room", 300));
        bookingQueue.offer(new Reservation("Charlie", "Double Room", 180));

        // Initialize inventory
        RoomInventory inventory = new RoomInventory();
        inventory.displayInventory();

        // Process bookings
        BookingService bookingService = new BookingService(bookingQueue, inventory);
        bookingService.processBookings();
        inventory.displayInventory();

        // Initialize Add-On Service Manager
        AddOnServiceManager serviceManager = new AddOnServiceManager();

        // Create some services
        AddOnService breakfast = new AddOnService("Breakfast", 20);
        AddOnService spa = new AddOnService("Spa", 50);
        AddOnService airportPickup = new AddOnService("Airport Pickup", 30);

        // Assign services to reservations
        serviceManager.addService(new Reservation("Alice", "Single Room", 100) {{ setAssignedRoomId("SI100"); }}, breakfast);
        serviceManager.addService(new Reservation("Bob", "Suite Room", 300) {{ setAssignedRoomId("SU101"); }}, spa);
        serviceManager.addService(new Reservation("Bob", "Suite Room", 300) {{ setAssignedRoomId("SU101"); }}, airportPickup);

        // Display services for each reservation
        System.out.println();
        serviceManager.displayServices(new Reservation("Alice", "Single Room", 100) {{ setAssignedRoomId("SI100"); }});
        serviceManager.displayServices(new Reservation("Bob", "Suite Room", 300) {{ setAssignedRoomId("SU101"); }});
        serviceManager.displayServices(new Reservation("Charlie", "Double Room", 180) {{ setAssignedRoomId("DO102"); }});
    }
}