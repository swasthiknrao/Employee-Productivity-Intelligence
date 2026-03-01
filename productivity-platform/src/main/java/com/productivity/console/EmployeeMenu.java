package com.productivity.console;

import com.productivity.dao.DepartmentDao;
import com.productivity.dao.EmployeeDao;
import com.productivity.model.Department;
import com.productivity.model.Employee;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class EmployeeMenu {

    private final Scanner scanner;
    private final DepartmentDao departmentDao = new DepartmentDao();
    private final EmployeeDao employeeDao = new EmployeeDao();

    public EmployeeMenu(Scanner scanner) {
        this.scanner = scanner;
    }

    public void run() {
        while (true) {
            System.out.println("\n--- Employees & Departments ---");
            System.out.println("1. List departments");
            System.out.println("2. Add department");
            System.out.println("3. List employees");
            System.out.println("4. Add employee");
            System.out.println("5. Edit employee");
            System.out.println("0. Back");
            System.out.print("Choice: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    listDepartments();
                    break;
                case "2":
                    addDepartment();
                    break;
                case "3":
                    listEmployees();
                    break;
                case "4":
                    addEmployee();
                    break;
                case "5":
                    editEmployee();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private void listDepartments() {
        try {
            List<Department> list = departmentDao.findAll();
            if (list.isEmpty()) {
                System.out.println("No departments.");
                return;
            }
            System.out.println("\nID | Name");
            System.out.println("---|-----");
            for (Department d : list) {
                System.out.println(d.getId() + "  | " + (d.getName() != null ? d.getName() : ""));
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void addDepartment() {
        System.out.print("Department name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Name required.");
            return;
        }
        Department d = new Department();
        d.setName(name);
        try {
            departmentDao.insert(d);
            System.out.println("Department added.");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void listEmployees() {
        try {
            List<Employee> list = employeeDao.findAll();
            if (list.isEmpty()) {
                System.out.println("No employees.");
                return;
            }
            System.out.println("\nID | Name           | Email              | Department   | Role");
            System.out.println("---|----------------|--------------------|--------------|--------");
            for (Employee e : list) {
                System.out.printf("%-3d| %-14s | %-18s | %-12s | %s%n",
                        e.getId(), truncate(e.getName(), 14), truncate(e.getEmail(), 18),
                        truncate(e.getDepartmentName(), 12), truncate(e.getRole(), 8));
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void addEmployee() {
        try {
            List<Department> depts = departmentDao.findAll();
            if (depts.isEmpty()) {
                System.out.println("Add a department first.");
                return;
            }
            System.out.print("Name: ");
            String name = scanner.nextLine().trim();
            if (name.isEmpty()) {
                System.out.println("Name is required.");
                return;
            }
            System.out.print("Email: ");
            String email = scanner.nextLine().trim();
            if (email.isEmpty()) {
                System.out.println("Email is required.");
                return;
            }
            System.out.print("Department ID: ");
            int deptId = parseInt(scanner.nextLine().trim(), 0);
            if (deptId <= 0) {
                System.out.println("Invalid department ID.");
                return;
            }
            boolean deptExists = depts.stream().anyMatch(d -> d.getId() == deptId);
            if (!deptExists) {
                System.out.println("Department ID " + deptId + " not found. Choose from list.");
                return;
            }
            System.out.print("Role (default employee): ");
            String role = scanner.nextLine().trim();
            if (role.isEmpty()) role = "employee";
            System.out.print("Join date (YYYY-MM-DD): ");
            LocalDate joinDate = parseDate(scanner.nextLine().trim());
            if (joinDate == null) {
                System.out.println("Invalid date. Use YYYY-MM-DD.");
                return;
            }
            Employee e = new Employee();
            e.setName(name);
            e.setEmail(email);
            e.setDepartmentId(deptId);
            e.setRole(role);
            e.setJoinDate(joinDate);
            e.setActive(true);
            employeeDao.insert(e);
            System.out.println("Employee added.");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void editEmployee() {
        System.out.print("Employee ID to edit: ");
        int id = parseInt(scanner.nextLine().trim(), 0);
        if (id <= 0) {
            System.out.println("Invalid ID.");
            return;
        }
        try {
            Employee e = employeeDao.findById(id);
            if (e == null) {
                System.out.println("Employee not found.");
                return;
            }
            System.out.print("Name [" + e.getName() + "]: ");
            String name = scanner.nextLine().trim();
            if (!name.isEmpty()) e.setName(name);
            System.out.print("Email [" + e.getEmail() + "]: ");
            String email = scanner.nextLine().trim();
            if (!email.isEmpty()) e.setEmail(email);
            System.out.print("Department ID [" + e.getDepartmentId() + "]: ");
            String deptStr = scanner.nextLine().trim();
            if (!deptStr.isEmpty()) e.setDepartmentId(parseInt(deptStr, e.getDepartmentId()));
            System.out.print("Role [" + e.getRole() + "]: ");
            String role = scanner.nextLine().trim();
            if (!role.isEmpty()) e.setRole(role);
            employeeDao.update(e);
            System.out.println("Updated.");
        } catch (SQLException ex) {
            System.out.println("Error: " + ex.getMessage());
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

    private static LocalDate parseDate(String s) {
        if (s == null || s.isEmpty()) return null;
        try {
            return LocalDate.parse(s);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
