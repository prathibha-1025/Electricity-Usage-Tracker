import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UsageService {
    private final List<Resident> residents = new ArrayList<>();
    private final Set<String> meterNumbers = new HashSet<>();
    private final FileManager fileManager;
    private int nextId = 1;

    public UsageService(FileManager fileManager) throws IOException {
        this.fileManager = fileManager;
        List<Resident> loaded = fileManager.loadResidents();
        residents.addAll(loaded);
        for (Resident resident : residents) {
            meterNumbers.add(FileManager.normalizeMeter(resident.getMeterNumber()));
            nextId = Math.max(nextId, resident.getId() + 1);
        }
    }

    public Resident registerResident(String name, String meterNumber, int openingReading) throws IOException {
        name = name.trim();
        meterNumber = FileManager.normalizeMeter(meterNumber);
        if (name.isBlank() || name.contains("|") || name.contains("\\n") || name.contains("\\r")) {
            throw new IllegalArgumentException("Name cannot be blank or contain | or line breaks.");
        }
        if (meterNumbers.contains(meterNumber)) {
            throw new IllegalArgumentException("Meter number already exists.");
        }
        if (meterNumber.isBlank() || meterNumber.contains("|") || meterNumber.contains("\\n")) {
            throw new IllegalArgumentException("Invalid meter number.");
        }
        if (openingReading < 0 || openingReading > 1_000_000) {
            throw new IllegalArgumentException("Opening reading must be between 0 and 1000000.");
        }

        Resident resident = new Resident(nextId, name, meterNumber,
                new int[]{openingReading, -1, -1, -1});
        residents.add(resident);
        try {
            fileManager.saveResidents(residents);
            meterNumbers.add(meterNumber);
            nextId++;
            return resident;
        } catch (IOException ex) {
            residents.remove(resident);
            throw ex;
        }
    }

    public void recordReading(int residentId, int reading) throws IOException {
        Resident resident = findResident(residentId);
        if (resident == null) throw new IllegalArgumentException("Resident ID not found.");
        int[] oldReadings = resident.getReadings();
        if (resident.getNextReadingIndex() == -1) {
            throw new IllegalArgumentException("All three months are already recorded.");
        }
        if (reading < 0 || reading > 1_000_000) {
            throw new IllegalArgumentException("Reading must be between 0 and 1000000.");
        }
        int index = resident.getNextReadingIndex();
        if (reading < oldReadings[index - 1]) {
            throw new IllegalArgumentException("Reading cannot be lower than the previous reading.");
        }

        resident.addReading(reading);
        try {
            fileManager.saveResidents(residents);
        } catch (IOException ex) {
            // Restore the original values if saving fails.
            Resident restored = new Resident(resident.getId(), resident.getName(),
                    resident.getMeterNumber(), oldReadings);
            int position = residents.indexOf(resident);
            residents.set(position, restored);
            throw ex;
        }
    }

    public Resident findResident(int id) {
        for (Resident resident : residents) {
            if (resident.getId() == id) return resident;
        }
        return null;
    }

    public List<Resident> getResidents() {
        return new ArrayList<>(residents);
    }

    public void showResidents() {
        if (residents.isEmpty()) {
            System.out.println("No records found.");
            return;
        }
        for (Resident resident : residents) {
            System.out.println("ID: " + resident.getId() + " | Name: " + resident.getName()
                    + " | Meter: " + resident.getMeterNumber()
                    + " | Total usage: " + resident.calculateUsage() + " units");
        }
    }

    public void showUsage(Resident resident) {
        System.out.println("Usage for " + resident.getName() + " (ID " + resident.getId() + ")");
        for (int month = 1; month <= 3; month++) {
            int usage = resident.getMonthUsage(month);
            if (usage == -1) {
                System.out.println("Month " + month + ": Not recorded");
            } else {
                System.out.println("Month " + month + ": " + usage + " units");
            }
        }
        System.out.println("Total recorded units: " + resident.calculateUsage());
    }

    public void showReport() throws IOException {
        if (residents.isEmpty()) {
            System.out.println("No records found.");
        } else {
            for (Resident resident : residents) showUsage(resident);
        }
        fileManager.saveReport(residents);
        System.out.println("Report saved to data/report.txt");
    }
}
