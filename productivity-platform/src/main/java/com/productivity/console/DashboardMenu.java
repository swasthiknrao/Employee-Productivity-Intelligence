package com.productivity.console;

import com.productivity.model.ProductivitySnapshot;
import com.productivity.model.Task;
import com.productivity.model.WorkSession;
import com.productivity.service.ProductivityService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class DashboardMenu {

    private final Scanner scanner;
    private final ProductivityService productivityService = new ProductivityService();

    public DashboardMenu(Scanner scanner) {
        this.scanner = scanner;
    }

    public void run() {
        while (true) {
            System.out.println("\n--- Dashboard ---");
            System.out.println("1. Compute productivity scores (all employees, today)");
            System.out.println("2. Team view (top employees + department summary)");
            System.out.println("3. Individual view (score, trend, tasks, focus vs meeting)");
            System.out.println("4. Work rhythm (peak focus hours)");
            System.out.println("0. Back");
            System.out.print("Choice: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    computeScores();
                    break;
                case "2":
                    teamView();
                    break;
                case "3":
                    individualView();
                    break;
                case "4":
                    workRhythm();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    public void runInsights() {
        System.out.println("\n--- Insights (Bottleneck & recommendations) ---");
        try {
            LocalDate today = LocalDate.now();
            List<String> insights = productivityService.getInsights(today);
            if (insights.isEmpty()) {
                System.out.println("No insights at this time.");
                return;
            }
            for (String s : insights) {
                System.out.println("  * " + s);
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void computeScores() {
        try {
            LocalDate today = LocalDate.now();
            productivityService.computeAndSaveSnapshotsForAll(today);
            System.out.println("Productivity scores computed for " + today + ".");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void teamView() {
        try {
            LocalDate today = LocalDate.now();
            List<ProductivitySnapshot> top = productivityService.getTopEmployeesByScore(today, 10);
            if (top.isEmpty()) {
                System.out.println("No snapshot data. Run 'Compute productivity scores' first.");
                return;
            }
            System.out.println("\n--- Top employees by score (" + today + ") ---\n");
            System.out.println("Rank | Employee         | Score  | Bar");
            System.out.println("-----|------------------|--------|------------------");
            int rank = 1;
            for (ProductivitySnapshot s : top) {
                String bar = scoreBar(s.getScore());
                System.out.printf("%-4d | %-16s | %5.1f  | %s%n", rank++, s.getEmployeeName() != null ? s.getEmployeeName() : "?", s.getScore(), bar);
            }
            System.out.println("\n--- Department summary ---\n");
            var summaries = productivityService.getDepartmentSummaries(today);
            summaries.forEach((deptId, sum) -> {
                System.out.printf("  Dept ID %d: avg score %.1f, tasks done %d, employees %d%n", deptId, sum.avgScore, sum.totalTasksDone, sum.employeeCount);
            });
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void individualView() {
        System.out.print("Employee ID: ");
        String idStr = scanner.nextLine().trim();
        int empId = parseInt(idStr, 0);
        if (empId <= 0) {
            System.out.println("Invalid ID.");
            return;
        }
        try {
            LocalDate today = LocalDate.now();
            ProductivitySnapshot current = productivityService.getSnapshotForEmployee(empId, today);
            if (current == null) {
                System.out.println("No snapshot for today. Run 'Compute productivity scores' first, or no data for this employee.");
                return;
            }
            System.out.println("\n--- " + (current.getEmployeeName() != null ? current.getEmployeeName() : "Employee " + empId) + " ---");
            System.out.println("Score: " + String.format("%.1f", current.getScore()) + "  " + scoreBar(current.getScore()));
            List<ProductivitySnapshot> history = productivityService.getSnapshotHistoryForEmployee(empId, 2);
            if (history.size() >= 2) {
                double prev = history.get(1).getScore();
                double trend = current.getScore() - prev;
                System.out.println("Trend: " + (trend >= 0 ? "↑" : "↓") + " " + String.format("%.1f", Math.abs(trend)) + " vs previous period");
            }
            System.out.println("Tasks done (period): " + current.getTasksDone() + ", Focus min: " + current.getFocusMinutes() + ", Collaboration: " + current.getCollaborationCount());
            List<Task> tasks = productivityService.getRecentTasksForEmployee(empId);
            System.out.println("\nRecent tasks:");
            int n = Math.min(5, tasks.size());
            for (int i = 0; i < n; i++) {
                Task t = tasks.get(i);
                System.out.println("  - " + (t.getTitle() != null ? t.getTitle() : "(no title)") + " [" + t.getStatus() + "]");
            }
            List<WorkSession> sessions = productivityService.getRecentSessionsForEmployee(empId, 10);
            long focusMin = 0, meetingMin = 0, totalMin = 0;
            for (WorkSession ws : sessions) {
                if (ws.getStartTime() == null || ws.getEndTime() == null) continue;
                long mins = java.time.temporal.ChronoUnit.MINUTES.between(ws.getStartTime(), ws.getEndTime());
                if (mins < 0) mins = 0;
                totalMin += mins;
                String actName = ws.getActivityTypeName();
                if (actName != null && ("coding".equalsIgnoreCase(actName) || "research".equalsIgnoreCase(actName) || "documentation".equalsIgnoreCase(actName))) {
                    focusMin += mins;
                } else if (actName != null && "meeting".equalsIgnoreCase(actName)) {
                    meetingMin += mins;
                }
            }
            if (totalMin > 0) {
                System.out.println("\nFocus vs meeting (recent): " + focusMin + " min focus, " + meetingMin + " min meeting");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void workRhythm() {
        System.out.print("Employee ID: ");
        int empId = parseInt(scanner.nextLine().trim(), 0);
        if (empId <= 0) {
            System.out.println("Invalid ID.");
            return;
        }
        System.out.print("Last N days [7]: ");
        String d = scanner.nextLine().trim();
        int days = d.isEmpty() ? 7 : parseInt(d, 7);
        days = Math.max(1, Math.min(days, 365));
        try {
            String rhythm = productivityService.getWorkRhythmForEmployee(empId, days);
            System.out.println(rhythm);
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static String scoreBar(double score) {
        int filled = (int) Math.round(score / 5);
        filled = Math.min(20, Math.max(0, filled));
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < 20; i++) {
            sb.append(i < filled ? "#" : " ");
        }
        sb.append("] ").append(String.format("%.0f%%", score));
        return sb.toString();
    }

    private static int parseInt(String s, int defaultVal) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }
}
