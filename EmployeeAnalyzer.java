import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EmployeeAnalyzer {
    public static void main(String[] args) {
        String inputFile = "employee_data.csv";
        List<Employee> employees = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            String line;
            boolean isFirstLine = true; // Added to skip the header row
            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Skip the header row
                }

                String[] parts = line.split(",");
                if (parts.length == 3) {
                    String name = parts[0];
                    Date date = new SimpleDateFormat("yyyy-MM-dd").parse(parts[1]);
                    double hoursWorked = Double.parseDouble(parts[2]);
                    employees.add(new Employee(name, date, hoursWorked));
                }
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            return;
        }

        List<String> consecutiveEmployees = new ArrayList<>();
        List<String> shortBreakEmployees = new ArrayList<>();
        List<String> longShiftEmployees = new ArrayList<>();

        int consecutiveDays = 7;
        long minTimeBetweenShifts = 36000000; // 10 hour in milliseconds
        double minSingleShiftHours = 14.0;

        for (int i = 1; i < employees.size(); i++) {
            Employee current = employees.get(i);
            Employee previous = employees.get(i - 1);

            if (current.name.equals(previous.name)) {
                long timeBetweenShifts = current.date.getTime() - previous.date.getTime();

                if (timeBetweenShifts >= 3600000 && timeBetweenShifts < minTimeBetweenShifts) {
                    shortBreakEmployees.add(current.name);
                }
                

                if (current.hoursWorked > minSingleShiftHours) {
                    longShiftEmployees.add(current.name);
                }
            }

            // Check for consecutive days
            int consecutiveCount = 1;
            for (int j = i - 1; j >= 0; j--) {
                if (employees.get(j).name.equals(current.name) &&
                        (current.date.getTime()
                                - employees.get(j).date.getTime() <= (consecutiveCount * 24 * 60 * 60 * 1000))) {
                    consecutiveCount++;
                } else {
                    break;
                }
            }

            if (consecutiveCount >= consecutiveDays) {
                consecutiveEmployees.add(current.name);
            }
        }

        try (FileWriter writer = new FileWriter("output.txt")) {
            writer.write("Employees with 7 consecutive days:\n");
            for (String name : consecutiveEmployees) {
                writer.write(name + '\n');
            }
            writer.write("\nEmployees with less than 10 hours between shifts but greater than 1 hour:\n");
            for (String name : shortBreakEmployees) {
                writer.write(name + shortBreakEmployees+'\n');
            }
            writer.write("\nEmployees who worked for more than 14 hours in a single shift:\n");
            for (String name : longShiftEmployees) {
                writer.write(name + '\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Analysis completed. Results are written to output.txt.");
    }
}

class Employee {
    String name;
    Date date;
    double hoursWorked;

    Employee(String name, Date date, double hoursWorked) {
        this.name = name;
        this.date = date;
        this.hoursWorked = hoursWorked;
    }
}