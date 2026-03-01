package com.productivity.console;

import com.productivity.dao.AuditLogDao;
import com.productivity.service.ReportService;

import java.time.LocalDate;
import java.util.Scanner;

public class ReportMenu {

    private final Scanner scanner;
    private final ReportService reportService = new ReportService();
    private final AuditLogDao auditLogDao = new AuditLogDao();
    private static final String AUDIT_ROLE = "admin";

    public ReportMenu(Scanner scanner) {
        this.scanner = scanner;
    }

    public void run() {
        while (true) {
            System.out.println("\n--- Reports & Export ---");
            System.out.println("1. Daily productivity summary (console)");
            System.out.println("2. Weekly productivity summary (console)");
            System.out.println("3. Department report (console)");
            System.out.println("4. Export snapshots to CSV");
            System.out.println("5. Export tasks to CSV");
            System.out.println("0. Back");
            System.out.print("Choice: ");
            String choice = scanner.nextLine();
            if (choice != null) choice = choice.trim();
            switch (choice != null ? choice : "") {
                case "1":
                    dailySummary();
                    break;
                case "2":
                    weeklySummary();
                    break;
                case "3":
                    departmentReport();
                    break;
                case "4":
                    exportSnapshots();
                    break;
                case "5":
                    exportTasks();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private void dailySummary() {
        System.out.print("Date (YYYY-MM-DD) or Enter for today: ");
        String d = scanner.nextLine().trim();
        LocalDate date = d.isEmpty() ? LocalDate.now() : parseDate(d);
        if (date == null) {
            System.out.println("Invalid date.");
            return;
        }
        try {
            String report = reportService.buildDailySummary(date);
            System.out.println(report);
            try { auditLogDao.log(AUDIT_ROLE, "view_daily_summary", "report", date.toString()); } catch (Exception ignored) { }
        } catch (Exception e) {
            System.out.println("Error: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }
    }

    private void weeklySummary() {
        System.out.print("End date (YYYY-MM-DD) or Enter for today: ");
        String d = scanner.nextLine().trim();
        LocalDate date = d.isEmpty() ? LocalDate.now() : parseDate(d);
        if (date == null) {
            System.out.println("Invalid date.");
            return;
        }
        try {
            String report = reportService.buildWeeklySummary(date);
            System.out.println(report);
            try { auditLogDao.log(AUDIT_ROLE, "view_weekly_summary", "report", date.toString()); } catch (Exception ignored) { }
        } catch (Exception e) {
            System.out.println("Error: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }
    }

    private void departmentReport() {
        System.out.print("Date (YYYY-MM-DD) or Enter for today: ");
        String d = scanner.nextLine().trim();
        LocalDate date = d.isEmpty() ? LocalDate.now() : parseDate(d);
        if (date == null) {
            System.out.println("Invalid date.");
            return;
        }
        try {
            String report = reportService.buildDepartmentReport(date);
            System.out.println(report);
            try { auditLogDao.log(AUDIT_ROLE, "view_department_report", "report", date.toString()); } catch (Exception ignored) { }
        } catch (Exception e) {
            System.out.println("Error: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }
    }

    private void exportSnapshots() {
        System.out.print("Output file path (e.g. C:/temp/snapshots.csv): ");
        String path = scanner.nextLine();
        if (path != null) path = path.trim();
        if (path == null || path.isEmpty()) {
            System.out.println("Path required.");
            return;
        }
        System.out.print("From date (YYYY-MM-DD) [today-7]: ");
        String fromStr = scanner.nextLine().trim();
        LocalDate from = fromStr.isEmpty() ? LocalDate.now().minusDays(7) : parseDate(fromStr);
        System.out.print("To date (YYYY-MM-DD) [today]: ");
        String toStr = scanner.nextLine().trim();
        LocalDate to = toStr.isEmpty() ? LocalDate.now() : parseDate(toStr);
        if (from == null || to == null || from.isAfter(to)) {
            System.out.println("Invalid date range.");
            return;
        }
        try {
            reportService.exportSnapshotsToCsv(path, from, to);
            System.out.println("Exported to " + path);
            try { auditLogDao.log(AUDIT_ROLE, "export_snapshots", "file", path); } catch (Exception ignored) { }
        } catch (Exception e) {
            System.out.println("Error: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }
    }

    private void exportTasks() {
        System.out.print("Output file path (e.g. C:/temp/tasks.csv): ");
        String path = scanner.nextLine();
        if (path != null) path = path.trim();
        if (path == null || path.isEmpty()) {
            System.out.println("Path required.");
            return;
        }
        System.out.print("Max tasks [500]: ");
        String limitStr = scanner.nextLine().trim();
        int limit = limitStr.isEmpty() ? 500 : parseInt(limitStr, 500);
        limit = Math.max(1, Math.min(limit, 10_000));
        try {
            reportService.exportTasksToCsv(path, limit);
            System.out.println("Exported to " + path);
            try { auditLogDao.log(AUDIT_ROLE, "export_tasks", "file", path); } catch (Exception ignored) { }
        } catch (Exception e) {
            System.out.println("Error: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }
    }

    private static LocalDate parseDate(String s) {
        if (s == null || s.isEmpty()) return null;
        try {
            return LocalDate.parse(s);
        } catch (Exception e) {
            return null;
        }
    }

    private static int parseInt(String s, int defaultVal) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }
}
