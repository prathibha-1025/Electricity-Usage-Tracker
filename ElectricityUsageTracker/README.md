# Electricity Usage Tracker (Java Console Project)

A beginner-friendly Java console application based on the supplied project specification.

## Requirements
- JDK 21 (Java 17+ may also work with this code)
- Any Java IDE or a terminal

## Files
- `src/User.java` — abstract parent class
- `src/Operator.java` — built-in operator
- `src/Resident.java` — resident profile and cumulative readings
- `src/UsageService.java` — registration, validation, calculations, and reports
- `src/FileManager.java` — reads and writes project files
- `src/Main.java` — console menus and input
- `data/residents.txt` — saved resident records
- `data/report.txt` — generated usage report

## Run from the project folder
```bash
javac -d out src/*.java
java -cp out Main
```

Keep the working directory at the project root so the application can find the `data` folder.

## Record format
Each resident is stored on one line:
`id|name|meterNumber|opening,month1,month2,month3`

A value of `-1` means that month has not been recorded. The project tracks electricity units only; it does not calculate bills.

## Main menu
1. Operator menu
2. Resident menu
0. Exit

The operator can register residents, record monthly readings in order, view all residents, and save a report. Residents can view their own profile and usage by ID.

## Example
Opening reading: 1200
Month 1 reading: 1280 => 80 units
Month 2 reading: 1370 => 90 units
Total recorded usage: 170 units
