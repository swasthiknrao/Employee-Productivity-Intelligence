package com.productivity.console;

import java.util.Scanner;

public class MenuDriver {

    private final Scanner scanner = new Scanner(System.in);
    private final EmployeeMenu employeeMenu = new EmployeeMenu(scanner);
    private final ActivityMenu activityMenu = new ActivityMenu(scanner);
    private final DashboardMenu dashboardMenu = new DashboardMenu(scanner);
    private final ReportMenu reportMenu = new ReportMenu(scanner);
    private final SetupMenu setupMenu = new SetupMenu(scanner);

    public void run() {
        System.out.println("\n=== Employee Productivity Intelligence Platform ===\n");
        while (true) {
            try {
                printMainMenu();
                String choice = scanner.nextLine();
                if (choice != null) {
                    choice = choice.trim();
                } else {
                    choice = "";
                }
                switch (choice) {
                    case "1":
                        employeeMenu.run();
                        break;
                    case "2":
                        activityMenu.run();
                        break;
                    case "3":
                        dashboardMenu.run();
                        break;
                    case "4":
                        dashboardMenu.runInsights();
                        break;
                    case "5":
                        reportMenu.run();
                        break;
                    case "6":
                        setupMenu.run();
                        break;
                    case "0":
                        System.out.println("Goodbye.");
                        return;
                    default:
                        System.out.println("Invalid option. Try again.");
                        break;
                }
            } catch (Throwable t) {
                System.out.println("Unexpected error: " + t.getMessage());
                t.printStackTrace();
            }
        }
    }

    private void printMainMenu() {
        System.out.println("--- Main Menu ---");
        System.out.println("1. Employees & Departments");
        System.out.println("2. Activities & Tasks");
        System.out.println("3. Dashboard");
        System.out.println("4. Insights");
        System.out.println("5. Reports & Export");
        System.out.println("6. Setup");
        System.out.println("0. Exit");
        System.out.print("Choice: ");
    }
}
