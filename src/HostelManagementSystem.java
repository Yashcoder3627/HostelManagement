import java.sql.*;
import java.util.Scanner;

public class HostelManagementSystem {

    // Update these credentials according to your local MySQL setup
    private static final String DB_URL = "jdbc:mysql://localhost:3306/hostel_db";
    private static final String USER = "root";
    private static final String PASS = "Yash2312@"; // Change to your MySQL password

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            System.out.println("Connected to hostel_db successfully!");

            while (true) {
                System.out.println("\n=== KIET HOSTEL MANAGEMENT SYSTEM ===");
                System.out.println("1. Insert Hostel Student Records (Min 3 records)");
                System.out.println("2. Conditional Retrieval & Sorting (Fee > 50,000)");
                System.out.println("3. Update Hostel Fee");
                System.out.println("4. Search and Modify Hostel Details (Room Number)");
                System.out.println("5. Aggregate Query (Total Fee) & Conditional Deletion");
                System.out.println("6. Exit");
                System.out.print("Enter your choice: ");

                int choice = sc.nextInt();
                sc.nextLine(); // Consume newline

                switch (choice) {
                    case 1:
                        insertStudents(conn, sc);
                        break;
                    case 2:
                        retrieveAndSort(conn, sc);
                        break;
                    case 3:
                        updateFee(conn, sc);
                        break;
                    case 4:
                        searchAndModifyRoom(conn, sc);
                        break;
                    case 5:
                        aggregateAndDelete(conn, sc);
                        break;
                    case 6:
                        System.out.println("Exiting application...");
                        return;
                    default:
                        System.out.println("Invalid choice! Please try again.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
        }
    }

    // (a) Insert Hostel Student Records using PreparedStatement & handle duplicates
    private static void insertStudents(Connection conn, Scanner sc) {
        System.out.print("How many student records do you want to insert? (Enter at least 3): ");
        int count = sc.nextInt();
        sc.nextLine();

        String insertSQL = "INSERT INTO HostelStudent (student_id, student_name, roll_number, hostel_name, room_number, fee) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            for (int i = 1; i <= count; i++) {
                System.out.println("\nEnter details for Student " + i + ":");
                System.out.print("Student ID: ");
                int id = sc.nextInt();
                sc.nextLine();

                System.out.print("Student Name: ");
                String name = sc.nextLine();

                System.out.print("Roll Number: ");
                String roll = sc.nextLine();

                System.out.print("Hostel Name: ");
                String hostel = sc.nextLine();

                System.out.print("Room Number: ");
                String room = sc.nextLine();

                System.out.print("Hostel Fee: ");
                double fee = sc.nextDouble();
                sc.nextLine();

                pstmt.setInt(1, id);
                pstmt.setString(2, name);
                pstmt.setString(3, roll);
                pstmt.setString(4, hostel);
                pstmt.setString(5, room);
                pstmt.setDouble(6, fee);

                try {
                    int rows = pstmt.executeUpdate();
                    if (rows > 0) {
                        System.out.println("Student ID " + id + " inserted successfully.");
                    }
                } catch (SQLIntegrityConstraintViolationException e) {
                    System.out.println("Error: Duplicate Student ID (" + id + ")! Record skipped.");
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error during insertion: " + e.getMessage());
        }
    }

    // (b) Conditional Retrieval (fee > 50,000) and Sorting (descending by fee)
    private static void retrieveAndSort(Connection conn, Scanner sc) {
        System.out.print("Enter Hostel Name to search: ");
        String hostelName = sc.nextLine();

        String query = "SELECT * FROM HostelStudent WHERE hostel_name = ? AND fee > 50000 ORDER BY fee DESC";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, hostelName);
            ResultSet rs = pstmt.executeQuery();

            boolean found = false;
            System.out.println("\n--- Students in " + hostelName + " with Fee > ₹50,000 (Sorted Descending) ---");
            while (rs.next()) {
                found = true;
                System.out.printf("ID: %d | Name: %s | Roll: %s | Hostel: %s | Room: %s | Fee: %.2f%n",
                        rs.getInt("student_id"),
                        rs.getString("student_name"),
                        rs.getString("roll_number"),
                        rs.getString("hostel_name"),
                        rs.getString("room_number"),
                        rs.getDouble("fee"));
            }
            if (!found) {
                System.out.println("No matching records found for hostel '" + hostelName + "' with fee > ₹50,000.");
            }
        } catch (SQLException e) {
            System.out.println("SQL Error during retrieval: " + e.getMessage());
        }
    }

    // (c) Update Hostel Fee
    private static void updateFee(Connection conn, Scanner sc) {
        System.out.print("Enter Student ID to update fee: ");
        int id = sc.nextInt();
        System.out.print("Enter new Hostel Fee: ");
        double newFee = sc.nextDouble();
        sc.nextLine();

        String updateSQL = "UPDATE HostelStudent SET fee = ? WHERE student_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {
            pstmt.setDouble(1, newFee);
            pstmt.setInt(2, id);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Fee updated successfully for Student ID: " + id);
            } else {
                System.out.println("Student record with ID " + id + " does not exist.");
            }
        } catch (SQLException e) {
            System.out.println("SQL Error during fee update: " + e.getMessage());
        }
    }

    // (d) Search and Modify Hostel Details (Room Number)
    private static void searchAndModifyRoom(Connection conn, Scanner sc) {
        System.out.print("Enter Student ID: ");
        int id = sc.nextInt();
        sc.nextLine();

        String selectSQL = "SELECT * FROM HostelStudent WHERE student_id = ?";
        String updateSQL = "UPDATE HostelStudent SET room_number = ? WHERE student_id = ?";

        try (PreparedStatement selectStmt = conn.prepareStatement(selectSQL)) {
            selectStmt.setInt(1, id);
            ResultSet rs = selectStmt.executeQuery();

            if (rs.next()) {
                System.out.println("Current Details -> Name: " + rs.getString("student_name")
                        + ", Hostel: " + rs.getString("hostel_name")
                        + ", Current Room: " + rs.getString("room_number"));

                System.out.print("Enter new Room Number: ");
                String newRoom = sc.nextLine();

                try (PreparedStatement updateStmt = conn.prepareStatement(updateSQL)) {
                    updateStmt.setString(1, newRoom);
                    updateStmt.setInt(2, id);
                    updateStmt.executeUpdate();
                    System.out.println("Room number updated successfully!");
                }

                // Fetch and display updated details
                ResultSet updatedRs = selectStmt.executeQuery();
                if (updatedRs.next()) {
                    System.out.println("--- Updated Student Details ---");
                    System.out.printf("ID: %d | Name: %s | Roll: %s | Hostel: %s | Room: %s | Fee: %.2f%n",
                            updatedRs.getInt("student_id"),
                            updatedRs.getString("student_name"),
                            updatedRs.getString("roll_number"),
                            updatedRs.getString("hostel_name"),
                            updatedRs.getString("room_number"),
                            updatedRs.getDouble("fee"));
                }
            } else {
                System.out.println("Student record with ID " + id + " does not exist.");
            }
        } catch (SQLException e) {
            System.out.println("SQL Error during search/modify: " + e.getMessage());
        }
    }

    // (e) Aggregate Query (SUM) and Conditional Deletion (fee > 0)
    private static void aggregateAndDelete(Connection conn, Scanner sc) {
        String sumSQL = "SELECT SUM(fee) AS total_fee FROM HostelStudent";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sumSQL)) {
            if (rs.next()) {
                double totalFee = rs.getDouble("total_fee");
                System.out.printf("Total Hostel Fee Collected: ₹%.2f%n", totalFee);
            }
        } catch (SQLException e) {
            System.out.println("SQL Error calculating total fee: " + e.getMessage());
        }

        System.out.print("Enter Student ID to delete: ");
        int id = sc.nextInt();
        sc.nextLine();

        // Check if student exists and fee > 0
        String checkSQL = "SELECT fee FROM HostelStudent WHERE student_id = ?";
        String deleteSQL = "DELETE FROM HostelStudent WHERE student_id = ? AND fee > 0";

        try (PreparedStatement checkStmt = conn.prepareStatement(checkSQL)) {
            checkStmt.setInt(1, id);
            ResultSet rs = checkStmt.executeQuery();

            if (!rs.next()) {
                System.out.println("Cannot delete: Student ID " + id + " does not exist.");
            } else {
                double fee = rs.getDouble("fee");
                if (fee > 0) {
                    try (PreparedStatement delStmt = conn.prepareStatement(deleteSQL)) {
                        delStmt.setInt(1, id);
                        delStmt.executeUpdate();
                        System.out.println("Student ID " + id + " deleted successfully (Fee paid: ₹" + fee + ").");
                    }
                } else {
                    System.out.println("Cannot delete: Hostel fee is not paid (Fee = ₹" + fee + ").");
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error during conditional deletion: " + e.getMessage());
        }
    }
}
