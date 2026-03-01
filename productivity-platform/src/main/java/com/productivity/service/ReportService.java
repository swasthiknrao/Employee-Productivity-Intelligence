package com.productivity.service;

import com.productivity.dao.*;
import com.productivity.model.*;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Text summaries and CSV export for reports.
 */
public class ReportService {

    private final ProductivitySnapshotDao snapshotDao = new ProductivitySnapshotDao();
    private final TaskDao taskDao = new TaskDao();
    private final EmployeeDao employeeDao = new EmployeeDao();
    private final DepartmentDao departmentDao = new DepartmentDao();
    private final ProductivityService productivityService = new ProductivityService();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    public String buildDailySummary(LocalDate date) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("--- Productivity Summary for ").append(date).append(" ---\n\n");
        List<ProductivitySnapshot> snapshots = snapshotDao.findBySnapshotDate(date);
        if (snapshots.isEmpty()) {
            sb.append("No snapshot data. Run 'Compute productivity scores' from Dashboard first.\n");
            return sb.toString();
        }
        for (ProductivitySnapshot s : snapshots) {
            sb.append(String.format("  %s: score %.1f, tasks done %d, focus %d min, collab %d%n",
                    s.getEmployeeName() != null ? s.getEmployeeName() : "(id " + s.getEmployeeId() + ")",
                    s.getScore(), s.getTasksDone(), s.getFocusMinutes(), s.getCollaborationCount()));
        }
        sb.append("\n");
        return sb.toString();
    }

    public String buildWeeklySummary(LocalDate endDate) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("--- Weekly Productivity Summary (ending ").append(endDate).append(") ---\n\n");
        for (int i = 0; i <= 6; i++) {
            LocalDate d = endDate.minusDays(i);
            List<ProductivitySnapshot> snapshots = snapshotDao.findBySnapshotDate(d);
            if (snapshots.isEmpty()) continue;
            sb.append("  ").append(d).append(": ");
            double avg = snapshots.stream().mapToDouble(ProductivitySnapshot::getScore).average().orElse(0);
            sb.append(String.format("avg score %.1f (%d employees)%n", avg, snapshots.size()));
        }
        sb.append("\n");
        return sb.toString();
    }

    public String buildDepartmentReport(LocalDate date) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("--- Department Report for ").append(date).append(" ---\n\n");
        var summaries = productivityService.getDepartmentSummaries(date);
        List<Department> depts = departmentDao.findAll();
        for (Department d : depts) {
            ProductivityService.DeptSummary sum = summaries.get(d.getId());
            if (sum == null) {
                sb.append("  ").append(d.getName() != null ? d.getName() : "Dept " + d.getId()).append(": no data\n");
            } else {
                sb.append(String.format("  %s: avg score %.1f, tasks done %d, employees %d%n",
                        d.getName() != null ? d.getName() : "Dept " + d.getId(), sum.avgScore, sum.totalTasksDone, sum.employeeCount));
            }
        }
        sb.append("\n");
        return sb.toString();
    }

    public void exportSnapshotsToCsv(String filePath, LocalDate from, LocalDate to) throws Exception {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be empty.");
        }
        filePath = filePath.trim();
        try (PrintWriter out = new PrintWriter(new FileWriter(filePath, StandardCharsets.UTF_8))) {
            out.println("employee_id,employee_name,snapshot_date,score,tasks_done,focus_minutes,collaboration_count");
            LocalDate d = from;
            while (!d.isAfter(to)) {
                List<ProductivitySnapshot> list = snapshotDao.findBySnapshotDate(d);
                for (ProductivitySnapshot s : list) {
                    out.printf("%d,\"%s\",%s,%.2f,%d,%d,%d%n",
                            s.getEmployeeId(), escapeCsv(s.getEmployeeName() != null ? s.getEmployeeName() : ""), d, s.getScore(),
                            s.getTasksDone(), s.getFocusMinutes(), s.getCollaborationCount());
                }
                d = d.plusDays(1);
            }
        }
    }

    public void exportTasksToCsv(String filePath, int limit) throws Exception {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be empty.");
        }
        filePath = filePath.trim();
        int safeLimit = Math.max(1, Math.min(limit, 100_000));
        try (PrintWriter out = new PrintWriter(new FileWriter(filePath, StandardCharsets.UTF_8))) {
            out.println("id,employee_id,employee_name,title,status,created_at,completed_at,estimated_mins");
            List<Task> list = taskDao.findAll(safeLimit);
            for (Task t : list) {
                out.printf("%d,%d,\"%s\",\"%s\",%s,%s,%s,%s%n",
                        t.getId(), t.getEmployeeId(), escapeCsv(t.getEmployeeName() != null ? t.getEmployeeName() : ""),
                        escapeCsv(t.getTitle() != null ? t.getTitle() : ""),
                        t.getStatus(), fmt(t.getCreatedAt()), fmt(t.getCompletedAt()), t.getEstimatedMins() != null ? t.getEstimatedMins() : "");
            }
        }
    }

    private static String escapeCsv(String s) {
        if (s == null) return "";
        return s.replace("\"", "\"\"");
    }

    private static String fmt(LocalDateTime dt) {
        return dt != null ? dt.toString() : "";
    }
}
