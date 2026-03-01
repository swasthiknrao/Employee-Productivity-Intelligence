package com.productivity.service;

import com.productivity.dao.*;
import com.productivity.model.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Productivity scoring, insights, and dashboard aggregates.
 * Formula: score = w1 * task_completion_rate + w2 * focus_ratio + w3 * collaboration_score (each 0-1, weights sum to 1).
 */
public class ProductivityService {

    private static final double W1 = 0.4;
    private static final double W2 = 0.35;
    private static final double W3 = 0.25;
    private static final int COLLAB_CAP = 20;
    private static final int SCORE_DAYS = 7;

    private final EmployeeDao employeeDao = new EmployeeDao();
    private final TaskDao taskDao = new TaskDao();
    private final WorkSessionDao workSessionDao = new WorkSessionDao();
    private final ActivityTypeDao activityTypeDao = new ActivityTypeDao();
    private final CommunicationDao communicationDao = new CommunicationDao();
    private final ProductivitySnapshotDao snapshotDao = new ProductivitySnapshotDao();

    public void computeAndSaveSnapshotForEmployee(int employeeId, LocalDate forDate) throws SQLException {
        LocalDateTime windowStart = forDate.minusDays(SCORE_DAYS).atStartOfDay();
        LocalDateTime windowEnd = forDate.plusDays(1).atStartOfDay();

        int totalTasks = taskDao.countByEmployeeSince(employeeId, windowStart);
        int completedTasks = taskDao.countCompletedByEmployeeSince(employeeId, windowStart);
        double taskRate = totalTasks == 0 ? 0.5 : (double) completedTasks / totalTasks;

        List<WorkSession> sessions = workSessionDao.findByEmployeeIdAndDateRange(employeeId, windowStart, windowEnd);
        Set<Integer> focusTypeIds = activityTypeDao.findAll().stream()
                .filter(ActivityType::isFocusType)
                .map(ActivityType::getId)
                .collect(Collectors.toSet());
        long totalMinutes = 0;
        long focusMinutes = 0;
        for (WorkSession ws : sessions) {
            if (ws.getStartTime() == null || ws.getEndTime() == null) continue;
            long mins = ChronoUnit.MINUTES.between(ws.getStartTime(), ws.getEndTime());
            if (mins < 0) mins = 0;
            totalMinutes += mins;
            if (focusTypeIds.contains(ws.getActivityTypeId())) {
                focusMinutes += mins;
            }
        }
        double focusRatio = totalMinutes == 0 ? 0.5 : (double) focusMinutes / totalMinutes;

        int collabCount = communicationDao.countByEmployeeIdInPeriod(employeeId, windowStart, windowEnd);
        double collabScore = Math.min(1.0, (double) collabCount / COLLAB_CAP);

        double score = W1 * taskRate * 100 + W2 * focusRatio * 100 + W3 * collabScore * 100;
        score = Math.min(100, Math.max(0, score));

        ProductivitySnapshot s = new ProductivitySnapshot();
        s.setEmployeeId(employeeId);
        s.setSnapshotDate(forDate);
        s.setScore(score);
        s.setTasksDone(completedTasks);
        s.setFocusMinutes((int) focusMinutes);
        s.setCollaborationCount(collabCount);
        s.setRawMetricsJson(String.format("{\"taskRate\":%.2f,\"focusRatio\":%.2f,\"collab\":%d}", taskRate, focusRatio, collabCount));
        snapshotDao.upsert(s);
    }

    public void computeAndSaveSnapshotsForAll(LocalDate forDate) throws SQLException {
        for (Employee e : employeeDao.findAll()) {
            computeAndSaveSnapshotForEmployee(e.getId(), forDate);
        }
    }

    public List<ProductivitySnapshot> getTopEmployeesByScore(LocalDate date, int limit) throws SQLException {
        return snapshotDao.findTopByScore(date, limit);
    }

    public List<ProductivitySnapshot> getSnapshotsForDate(LocalDate date) throws SQLException {
        return snapshotDao.findBySnapshotDate(date);
    }

    public ProductivitySnapshot getSnapshotForEmployee(int employeeId, LocalDate date) throws SQLException {
        return snapshotDao.findByEmployeeAndDate(employeeId, date);
    }

    public List<ProductivitySnapshot> getSnapshotHistoryForEmployee(int employeeId, int limit) throws SQLException {
        return snapshotDao.findByEmployeeId(employeeId, limit);
    }

    /** Department summary: avg score, total tasks done for a given date. */
    public Map<Integer, DeptSummary> getDepartmentSummaries(LocalDate date) throws SQLException {
        List<ProductivitySnapshot> all = snapshotDao.findBySnapshotDate(date);
        List<Employee> employees = employeeDao.findAll();
        Map<Integer, List<ProductivitySnapshot>> byDept = new HashMap<>();
        for (Employee e : employees) {
            byDept.computeIfAbsent(e.getDepartmentId(), k -> new ArrayList<>());
        }
        for (ProductivitySnapshot s : all) {
            Employee e = employees.stream().filter(emp -> emp.getId() == s.getEmployeeId()).findFirst().orElse(null);
            if (e != null) {
                byDept.get(e.getDepartmentId()).add(s);
            }
        }
        Map<Integer, DeptSummary> result = new HashMap<>();
        for (Map.Entry<Integer, List<ProductivitySnapshot>> entry : byDept.entrySet()) {
            List<ProductivitySnapshot> list = entry.getValue();
            if (list.isEmpty()) continue;
            double avg = list.stream().mapToDouble(ProductivitySnapshot::getScore).average().orElse(0);
            int tasksDone = list.stream().mapToInt(ProductivitySnapshot::getTasksDone).sum();
            result.put(entry.getKey(), new DeptSummary(avg, tasksDone, list.size()));
        }
        return result;
    }

    public static class DeptSummary {
        public final double avgScore;
        public final int totalTasksDone;
        public final int employeeCount;

        public DeptSummary(double avgScore, int totalTasksDone, int employeeCount) {
            this.avgScore = avgScore;
            this.totalTasksDone = totalTasksDone;
            this.employeeCount = employeeCount;
        }
    }

    /** Rules-based insights. */
    public List<String> getInsights(LocalDate asOfDate) throws SQLException {
        List<String> insights = new ArrayList<>();
        Map<Integer, DeptSummary> deptSummaries = getDepartmentSummaries(asOfDate);
        DepartmentDao departmentDao = new DepartmentDao();
        List<Department> departments = departmentDao.findAll();

        for (Department dept : departments) {
            DeptSummary sum = deptSummaries.get(dept.getId());
            if (sum == null) continue;
            String deptName = (dept.getName() != null) ? dept.getName() : "Dept " + dept.getId();
            if (sum.avgScore < 40 && sum.employeeCount > 0) {
                insights.add("Bottleneck: Low productivity in " + deptName + " (avg score " + String.format("%.0f", sum.avgScore) + ").");
            }
            if (sum.totalTasksDone == 0 && sum.employeeCount > 0) {
                insights.add("Bottleneck: Low task completion in " + deptName + ".");
            }
        }

        List<Employee> employees = employeeDao.findAll();
        Set<Integer> focusTypeIds = activityTypeDao.findAll().stream()
                .filter(ActivityType::isFocusType)
                .map(ActivityType::getId)
                .collect(Collectors.toSet());
        LocalDateTime from = asOfDate.minusDays(SCORE_DAYS).atStartOfDay();
        LocalDateTime to = asOfDate.plusDays(1).atStartOfDay();

        for (Employee e : employees) {
            List<WorkSession> sessions = workSessionDao.findByEmployeeIdAndDateRange(e.getId(), from, to);
            long totalMinutes = 0;
            long meetingMinutes = 0;
            for (WorkSession ws : sessions) {
                if (ws.getStartTime() == null || ws.getEndTime() == null) continue;
                long mins = ChronoUnit.MINUTES.between(ws.getStartTime(), ws.getEndTime());
                if (mins < 0) mins = 0;
                totalMinutes += mins;
                if (ws.getActivityTypeId() == 2) meetingMinutes += mins; // meeting
            }
            if (totalMinutes > 0 && (double) meetingMinutes / totalMinutes > 0.5) {
                String empName = (e.getName() != null) ? e.getName() : "Employee " + e.getId();
                insights.add("Consider reducing meeting load for " + empName + " (high meeting-to-focus ratio).");
            }
        }

        for (Department dept : departments) {
            List<Employee> deptEmps = employeeDao.findByDepartmentId(dept.getId());
            int totalComms = 0;
            for (Employee emp : deptEmps) {
                totalComms += communicationDao.countByEmployeeIdInPeriod(emp.getId(), from, to);
            }
            String deptName = (dept.getName() != null) ? dept.getName() : "Dept " + dept.getId();
            if (deptEmps.size() > 1 && totalComms < 3) {
                insights.add("Collaboration may be low in " + deptName + " (few communications in the period).");
            }
        }

        return insights;
    }

    public List<WorkSession> getRecentSessionsForEmployee(int employeeId, int limit) throws SQLException {
        return workSessionDao.findAllByEmployeeId(employeeId, limit);
    }

    public List<Task> getRecentTasksForEmployee(int employeeId) throws SQLException {
        return taskDao.findByEmployeeId(employeeId);
    }

    /** Work rhythm: hour bucket with most focus time (simplified). */
    public String getWorkRhythmForEmployee(int employeeId, int days) throws SQLException {
        LocalDateTime from = LocalDate.now().minusDays(days).atStartOfDay();
        LocalDateTime to = LocalDateTime.now();
        List<WorkSession> sessions = workSessionDao.findByEmployeeIdAndDateRange(employeeId, from, to);
        Set<Integer> focusIds = activityTypeDao.findAll().stream()
                .filter(ActivityType::isFocusType)
                .map(ActivityType::getId)
                .collect(Collectors.toSet());
        int[] hourBuckets = new int[24];
        for (WorkSession ws : sessions) {
            if (!focusIds.contains(ws.getActivityTypeId())) continue;
            if (ws.getStartTime() == null || ws.getEndTime() == null) continue;
            long mins = ChronoUnit.MINUTES.between(ws.getStartTime(), ws.getEndTime());
            if (mins <= 0) continue;
            int hour = ws.getStartTime().getHour();
            if (hour >= 0 && hour < 24) {
                hourBuckets[hour] += mins;
            }
        }
        int maxHour = 0;
        for (int h = 1; h < 24; h++) {
            if (hourBuckets[h] > hourBuckets[maxHour]) maxHour = h;
        }
        if (hourBuckets[maxHour] == 0) return "No focus sessions in the last " + days + " days.";
        return "Most focus hours: " + maxHour + ":00 - " + (maxHour + 1) + ":00 (last " + days + " days).";
    }
}
