import java.util.Arrays;

public class Resident extends User {
    private final String meterNumber;
    private final int[] readings;

    public Resident(int id, String name, String meterNumber, int[] readings) {
        super(id, name);
        this.meterNumber = meterNumber;
        this.readings = readings.clone();
    }

    public String getMeterNumber() {
        return meterNumber;
    }

    public int[] getReadings() {
        return readings.clone();
    }

    public int getNextReadingIndex() {
        for (int i = 1; i < readings.length; i++) {
            if (readings[i] == -1) {
                return i;
            }
        }
        return -1;
    }

    public boolean addReading(int reading) {
        int index = getNextReadingIndex();
        if (index == -1 || reading < readings[index - 1] || reading < 0 || reading > 1_000_000) {
            return false;
        }
        readings[index] = reading;
        return true;
    }

    public int getCompletedMonths() {
        int count = 0;
        for (int i = 1; i < readings.length; i++) {
            if (readings[i] == -1) break;
            count++;
        }
        return count;
    }

    public int getMonthUsage(int monthIndex) {
        if (monthIndex < 1 || monthIndex > 3 || readings[monthIndex] == -1) {
            return -1;
        }
        return readings[monthIndex] - readings[monthIndex - 1];
    }

    public int calculateUsage() {
        int total = 0;
        for (int i = 1; i < readings.length; i++) {
            if (readings[i] == -1) break;
            total += readings[i] - readings[i - 1];
        }
        return total;
    }

    @Override
    public void showProfile() {
        System.out.println("Resident ID: " + getId());
        System.out.println("Name: " + getName());
        System.out.println("Meter number: " + meterNumber);
    }

    @Override
    public String toString() {
        return "Resident{id=" + getId() + ", name='" + getName()
                + "', meterNumber='" + meterNumber + "', readings="
                + Arrays.toString(readings) + "}";
    }
}
