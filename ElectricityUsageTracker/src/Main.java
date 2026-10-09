import java.io.IOException;
import java.util.Scanner;

public class Main {
    private final Scanner scanner = new Scanner(System.in);
    private final UsageService service;

    public Main(UsageService service) {
        this.service = service;
    }

    public static void main(String[] args) {
        try {
            FileManager fileManager = new FileManager();
            UsageService service = new UsageService(fileManager);
            new Main(service).showMenu();
        } catch (IOException ex) {
            System.out.println("File error: " + ex.getMessage());
            System.out.println("Please correct the data file before restarting.");
        }
    }

    public void showMenu() {
        while (true) {
            System.out.println("\\n=== ELECTRICITY USAGE TRACKER ===");
            System.out.println("1. Operator menu");
            System.out.println("2. Resident menu");
            System.out.println("0. Exit");
            int choice = readInt("Choose: ");
            switch (choice) {
                case 1 -> operatorMenu();
                case 2 -> residentMenu();
                case 0 -> {
                    System.out.println("Thank you. Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private void operatorMenu() {
        Operator operator = new Operator();
        while (true) {
            System.out.println("\\n--- OPERATOR MENU ---");
            System.out.println("1. Register resident and opening reading");
            System.out.println("2. View residents");
            System.out.println("3. Record next month reading");
            System.out.println("4. View usage and save report");
            System.out.println("0. Back");
            int choice = readInt("Choose: ");
            try {
                switch (choice) {
                    case 1 -> registerResident();
                    case 2 -> service.showResidents();
                    case 3 -> recordReading();
                    case 4 -> service.showReport();
                    case 0 -> {
                        return;
                    }
                    default -> System.out.println("Invalid choice. Try again.");
                }
            } catch (IOException | IllegalArgumentException ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }
    }

    private void residentMenu() {
        while (true) {
            int id = readInt("\\nEnter resident ID (0 to go back): ");
            if (id == 0) return;
            Resident resident = service.findResident(id);
            if (resident == null) {
                System.out.println("Resident ID not found. Try again.");
                continue;
            }
            while (true) {
                System.out.println("\\n--- RESIDENT MENU ---");
                System.out.println("1. View my profile");
                System.out.println("2. View my readings and usage");
                System.out.println("0. Back");
                int choice = readInt("Choose: ");
                switch (choice) {
                    case 1 -> resident.showProfile();
                    case 2 -> service.showUsage(resident);
                    case 0 -> {
                        return;
                    }
                    default -> System.out.println("Invalid choice. Try again.");
                }
            }
        }
    }

    private void registerResident() throws IOException {
        System.out.print("Resident name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Meter number: ");
        String meter = scanner.nextLine().trim();
        int opening = readInt("Opening cumulative reading (0-1000000): ");
        Resident resident = service.registerResident(name, meter, opening);
        System.out.println("Resident " + resident.getId() + " saved.");
    }

    private void recordReading() throws IOException {
        int id = readInt("Resident ID: ");
        int monthIndexResidentId = id;
        Resident resident = service.findResident(monthIndexResidentId);
        if (resident == null) {
            System.out.println("Resident ID not found.");
            return;
        }
        int month = resident.getCompletedMonths() + 1;
        int reading = readInt("Month " + month + " cumulative reading (0-1000000): ");
        int previous = resident.getReadings()[month - 1];
        service.recordReading(id, reading);
        System.out.println("Month " + month + " saved. Units used: " + (reading - previous));
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException ex) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }
}
