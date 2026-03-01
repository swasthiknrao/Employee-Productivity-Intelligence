package com.productivity.console;

import com.productivity.dao.ActivityTypeDao;
import com.productivity.dao.CommunicationDao;
import com.productivity.dao.EmployeeDao;
import com.productivity.dao.TaskDao;
import com.productivity.dao.WorkSessionDao;
import com.productivity.model.ActivityType;
import com.productivity.model.Communication;
import com.productivity.model.Employee;
import com.productivity.model.Task;
import com.productivity.model.WorkSession;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class ActivityMenu {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final Scanner scanner;
    private final EmployeeDao employeeDao = new EmployeeDao();
    private final ActivityTypeDao activityTypeDao = new ActivityTypeDao();
    private final WorkSessionDao workSessionDao = new WorkSessionDao();
    private final TaskDao taskDao = new TaskDao();
    private final CommunicationDao communicationDao = new CommunicationDao();

    public ActivityMenu(Scanner scanner) {
        this.scanner = scanner;
    }

    public void run() {
        while (true) {
            System.out.println("\n--- Activities & Tasks ---");
            System.out.println("1. Log work session");
            System.out.println("2. List work sessions (by employee)");
            System.out.println("3. Create task");
            System.out.println("4. List tasks");
            System.out.println("5. Update task status");
            System.out.println("6. Log communication");
            System.out.println("7. List communications");
            System.out.println("0. Back");
            System.out.print("Choice: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    logWorkSession();
                    break;
                case "2":
                    listWorkSessions();
                    break;
                case "3":
                    createTask();
                    break;
                case "4":
                    listTasks();
                    break;
                case "5":
                    updateTaskStatus();
                    break;
                case "6":
                    logCommunication();
                    break;
                case "7":
                    listCommunications();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private void logWorkSession() {
        try {
            List<Employee> employees = employeeDao.findAll();
            if (employees.isEmpty()) {
                System.out.println("No employees.");
                return;
            }
            List<ActivityType> types = activityTypeDao.findAll();
            if (types.isEmpty()) {
                System.out.println("No activity types. Run schema/seed first.");
                return;
            }
            System.out.print("Employee ID: ");
            int empId = parseInt(scanner.nextLine().trim(), 0);
            if (empId <= 0) {
                System.out.println("Invalid employee ID.");
                return;
            }
            boolean empExists = employees.stream().anyMatch(e -> e.getId() == empId);
            if (!empExists) {
                System.out.println("Employee ID " + empId + " not found.");
                return;
            }
            System.out.print("Start (yyyy-MM-dd HH:mm): ");
            LocalDateTime start = parseDateTime(scanner.nextLine().trim());
            System.out.print("End (yyyy-MM-dd HH:mm): ");
            LocalDateTime end = parseDateTime(scanner.nextLine().trim());
            if (start == null || end == null) {
                System.out.println("Invalid date/time. Use yyyy-MM-dd HH:mm.");
                return;
            }
            if (!end.isAfter(start)) {
                System.out.println("End time must be after start time.");
                return;
            }
            System.out.println("Activity types: " + types.stream().map(t -> t.getId() + "=" + t.getName()).reduce((a, b) -> a + ", " + b).orElse(""));
            System.out.print("Activity type ID: ");
            int typeId = parseInt(scanner.nextLine().trim(), 1);
            boolean typeExists = types.stream().anyMatch(t -> t.getId() == typeId);
            if (!typeExists) {
                System.out.println("Activity type ID " + typeId + " not found.");
                return;
            }
            System.out.print("App name (optional): ");
            String app = scanner.nextLine().trim();
            System.out.print("Notes (optional): ");
            String notes = scanner.nextLine().trim();
            WorkSession w = new WorkSession();
            w.setEmployeeId(empId);
            w.setStartTime(start);
            w.setEndTime(end);
            w.setActivityTypeId(typeId);
            w.setAppName(app.isEmpty() ? null : app);
            w.setNotes(notes.isEmpty() ? null : notes);
            workSessionDao.insert(w);
            System.out.println("Work session logged.");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void listWorkSessions() {
        System.out.print("Employee ID: ");
        int empId = parseInt(scanner.nextLine().trim(), 0);
        if (empId <= 0) {
            System.out.println("Invalid ID.");
            return;
        }
        try {
            List<WorkSession> list = workSessionDao.findAllByEmployeeId(empId, 20);
            if (list.isEmpty()) {
                System.out.println("No sessions.");
                return;
            }
            System.out.println("\nStart            | End              | Activity   | App");
            System.out.println("-----------------|------------------|------------|-----");
            for (WorkSession w : list) {
                String startStr = (w.getStartTime() != null) ? w.getStartTime().format(DT) : "-";
                String endStr = (w.getEndTime() != null) ? w.getEndTime().format(DT) : "-";
                System.out.printf("%s | %s | %-10s | %s%n",
                        startStr, endStr,
                        w.getActivityTypeName() != null ? w.getActivityTypeName() : "-",
                        w.getAppName() != null ? w.getAppName() : "-");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void createTask() {
        try {
            List<Employee> employees = employeeDao.findAll();
            if (employees.isEmpty()) {
                System.out.println("No employees.");
                return;
            }
            System.out.print("Employee ID: ");
            int empId = parseInt(scanner.nextLine().trim(), 0);
            if (empId <= 0) {
                System.out.println("Invalid employee ID.");
                return;
            }
            boolean empExists = employees.stream().anyMatch(e -> e.getId() == empId);
            if (!empExists) {
                System.out.println("Employee ID " + empId + " not found.");
                return;
            }
            System.out.print("Title: ");
            String title = scanner.nextLine().trim();
            if (title.isEmpty()) {
                System.out.println("Title is required.");
                return;
            }
            System.out.print("Estimated minutes (optional): ");
            String estStr = scanner.nextLine().trim();
            Integer est = estStr.isEmpty() ? null : parseInt(estStr, 0);
            if (est != null && est < 0) est = null;
            Task t = new Task();
            t.setEmployeeId(empId);
            t.setTitle(title);
            t.setStatus(Task.Status.pending);
            t.setEstimatedMins(est);
            taskDao.insert(t);
            System.out.println("Task created.");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void listTasks() {
        try {
            List<Task> list = taskDao.findAll(50);
            if (list.isEmpty()) {
                System.out.println("No tasks.");
                return;
            }
            System.out.println("\nID  | Employee       | Title              | Status");
            System.out.println("----|----------------|--------------------|----------");
            for (Task t : list) {
                System.out.printf("%-4d| %-14s | %-18s | %s%n",
                        t.getId(), truncate(t.getEmployeeName(), 14), truncate(t.getTitle(), 18), t.getStatus());
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void updateTaskStatus() {
        System.out.print("Task ID: ");
        int id = parseInt(scanner.nextLine().trim(), 0);
        if (id <= 0) {
            System.out.println("Invalid ID.");
            return;
        }
        System.out.println("Status: 1=pending 2=in_progress 3=completed");
        System.out.print("New status (1/2/3): ");
        String st = scanner.nextLine().trim();
        Task.Status status;
        switch (st) {
            case "1": status = Task.Status.pending; break;
            case "2": status = Task.Status.in_progress; break;
            case "3": status = Task.Status.completed; break;
            default:
                System.out.println("Invalid status.");
                return;
        }
        try {
            Task t = taskDao.findById(id);
            if (t == null) {
                System.out.println("Task not found.");
                return;
            }
            t.setStatus(status);
            if (status == Task.Status.completed) {
                t.setCompletedAt(LocalDateTime.now());
            } else {
                t.setCompletedAt(null);
            }
            taskDao.update(t);
            System.out.println("Updated.");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void logCommunication() {
        try {
            List<Employee> employees = employeeDao.findAll();
            if (employees.size() < 2) {
                System.out.println("Need at least 2 employees.");
                return;
            }
            System.out.print("From employee ID: ");
            int fromId = parseInt(scanner.nextLine().trim(), 0);
            System.out.print("To employee ID: ");
            int toId = parseInt(scanner.nextLine().trim(), 0);
            if (fromId <= 0 || toId <= 0) {
                System.out.println("Invalid employee ID(s).");
                return;
            }
            if (fromId == toId) {
                System.out.println("From and To must be different employees.");
                return;
            }
            boolean fromExists = employees.stream().anyMatch(e -> e.getId() == fromId);
            boolean toExists = employees.stream().anyMatch(e -> e.getId() == toId);
            if (!fromExists || !toExists) {
                System.out.println("One or both employee IDs not found.");
                return;
            }
            System.out.print("Channel (email/chat) [chat]: ");
            String ch = scanner.nextLine().trim();
            if (ch.isEmpty()) ch = "chat";
            if (!ch.equalsIgnoreCase("email") && !ch.equalsIgnoreCase("chat")) {
                ch = "chat";
            }
            System.out.print("Sent at (yyyy-MM-dd HH:mm) or press Enter for now: ");
            String dtStr = scanner.nextLine().trim();
            LocalDateTime sent = dtStr.isEmpty() ? LocalDateTime.now() : parseDateTime(dtStr);
            if (sent == null) {
                System.out.println("Invalid date/time.");
                return;
            }
            Communication c = new Communication();
            c.setFromEmployeeId(fromId);
            c.setToEmployeeId(toId);
            c.setChannel(ch.toLowerCase());
            c.setSentAt(sent);
            communicationDao.insert(c);
            System.out.println("Communication logged.");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void listCommunications() {
        try {
            List<Communication> list = communicationDao.findAll(30);
            if (list.isEmpty()) {
                System.out.println("No communications.");
                return;
            }
            System.out.println("\nFrom            | To              | Channel | Sent");
            System.out.println("----------------|-----------------|---------|-------------------");
            for (Communication c : list) {
                System.out.printf("%-15s | %-15s | %-7s | %s%n",
                        truncate(c.getFromName(), 15), truncate(c.getToName(), 15),
                        c.getChannel(), c.getSentAt() != null ? c.getSentAt().format(DT) : "-");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max);
    }

    private static int parseInt(String s, int defaultVal) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    private static LocalDateTime parseDateTime(String s) {
        if (s == null || s.isEmpty()) return null;
        try {
            return LocalDateTime.parse(s, DT);
        } catch (Exception e) {
            return null;
        }
    }
}
