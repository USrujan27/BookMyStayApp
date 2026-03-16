import java.util.HashMap;
import java.util.Map;

/**
 * BookMyStayApp
 *
 * Demonstrates centralized room inventory management using HashMap.
 * All room availability is stored in a single structure to maintain
 * a consistent system state.
 *
 * Book My Stay Application
 *
 * @author Srujan Uppalapu
 * @version 3.1
 */

/* Room Inventory Class */
class RoomInventory {

    private HashMap<String, Integer> inventory;

    /**
     * Constructor initializes room availability
     */
    public RoomInventory() {

        inventory = new HashMap<>();

        inventory.put("Single Room", 5);
        inventory.put("Double Room", 3);
        inventory.put("Suite Room", 2);
    }

    /**
     * Get availability of a room type
     */
    public int getAvailability(String roomType) {
        return inventory.getOrDefault(roomType, 0);
    }

    /**
     * Update room availability
     */
    public void updateAvailability(String roomType, int newCount) {
        inventory.put(roomType, newCount);
    }

    /**
     * Display full inventory
     */
    public void displayInventory() {

        System.out.println("\nCurrent Room Inventory:");

        for (Map.Entry<String, Integer> entry : inventory.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue() + " rooms available");
        }
    }
}

/* Main Application Class */
public class BookMyStayApp {

    public static void main(String[] args) {

        System.out.println("====================================");
        System.out.println("        Book My Stay App");
        System.out.println("           Version 3.1");
        System.out.println("====================================");

        // Initialize inventory
        RoomInventory inventory = new RoomInventory();

        // Show inventory
        inventory.displayInventory();

        // Example update
        System.out.println("\nUpdating Single Room availability...");
        inventory.updateAvailability("Single Room", 4);

        // Show updated inventory
        inventory.displayInventory();

        System.out.println("\nInventory setup completed successfully.");
    }
}