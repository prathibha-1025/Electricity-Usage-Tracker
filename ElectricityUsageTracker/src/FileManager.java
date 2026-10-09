import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class FileManager {
    private final Path dataDirectory = Path.of("data");
    private final Path residentsFile = dataDirectory.resolve("residents.txt");
    private final Path reportFile = dataDirectory.resolve("report.txt");

    public FileManager() throws IOException {
        Files.createDirectories(dataDirectory);
        if (!Files.exists(residentsFile)) {
            Files.createFile(residentsFile);
        }
    }

    public List<Resident> loadResidents() throws IOException {
        List<Resident> loaded = new ArrayList<>();
        Set<Integer> ids = new HashSet<>();
        Set<String> meters = new HashSet<>();
        int lineNumber = 0;

        try (BufferedReader reader = Files.newBufferedReader(residentsFile, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String[] fields = line.split("\\\\|", -1);
                try {
                    if (fields.length != 4) throw new IllegalArgumentException("Expected 4 fields");
                    int id = Integer.parseInt(fields[0]);
                    String name = fields[1];
                    String meter = normalizeMeter(fields[2]);
                    String[] readingParts = fields[3].split(",", -1);
                    if (id <= 0 || name.isBlank() || name.contains("|") ||
                            !ids.add(id) || !meters.add(meter) || readingParts.length != 4) {
                        throw new IllegalArgumentException("Invalid or duplicate record");
                    }

                    int[] readings = new int[4];
                    boolean missingStarted = false;
                    for (int i = 0; i < 4; i++) {
                        readings[i] = Integer.parseInt(readingParts[i]);
                        if (i == 0) {
                            if (readings[i] < 0 || readings[i] > 1_000_000) {
                                throw new IllegalArgumentException("Opening reading out of range");
                            }
                        } else if (readings[i] == -1) {
                            missingStarted = true;
                        } else {
                            if (readings[i] < 0 || readings[i] > 1_000_000 ||
                                    missingStarted || readings[i] < readings[i - 1]) {
                                throw new IllegalArgumentException("Invalid reading sequence");
                            }
                        }
                    }
                    loaded.add(new Resident(id, name, meter, readings));
                } catch (RuntimeException ex) {
                    throw new IOException("Invalid data in " + residentsFile + " at line "
                            + lineNumber + ": " + ex.getMessage(), ex);
                }
            }
        }
        return loaded;
    }

    public void saveResidents(List<Resident> residents) throws IOException {
        Path temp = dataDirectory.resolve("residents.tmp");
        try (BufferedWriter writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
            for (Resident resident : residents) {
                int[] readings = resident.getReadings();
                writer.write(resident.getId() + "|" + safeText(resident.getName()) + "|"
                        + safeText(resident.getMeterNumber()) + "|" + readings[0] + ","
                        + readings[1] + "," + readings[2] + "," + readings[3]);
                writer.newLine();
            }
        }
        replaceFile(temp, residentsFile);
    }

    public void saveReport(List<Resident> residents) throws IOException {
        Path temp = dataDirectory.resolve("report.tmp");
        int totalUsage = 0;
        int missingMonths = 0;
        try (BufferedWriter writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
            writer.write("ELECTRICITY USAGE REPORT");
            writer.newLine();
            writer.write("========================");
            writer.newLine();
            for (Resident resident : residents) {
                writer.write("Resident " + resident.getId() + " - " + resident.getName()
                        + " | Meter: " + resident.getMeterNumber());
                writer.newLine();
                for (int month = 1; month <= 3; month++) {
                    int usage = resident.getMonthUsage(month);
                    if (usage == -1) {
                        writer.write("  Month " + month + ": Not recorded");
                        missingMonths++;
                    } else {
                        writer.write("  Month " + month + ": " + usage + " units");
                        totalUsage += usage;
                    }
                    writer.newLine();
                }
                writer.write("  Total recorded usage: " + resident.calculateUsage() + " units");
                writer.newLine();
                writer.newLine();
            }
            writer.write("Total usage across residents: " + totalUsage + " units");
            writer.newLine();
            writer.write("Missing resident-month readings: " + missingMonths);
            writer.newLine();
        }
        replaceFile(temp, reportFile);
    }

    public static String normalizeMeter(String meter) {
        return meter.trim().toUpperCase(Locale.ROOT);
    }

    private String safeText(String value) {
        if (value == null || value.isBlank() || value.contains("|")
                || value.contains("\\n") || value.contains("\\r")) {
            throw new IllegalArgumentException("Text fields cannot be blank or contain | or line breaks");
        }
        return value.trim();
    }

    private void replaceFile(Path temp, Path target) throws IOException {
        try {
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
