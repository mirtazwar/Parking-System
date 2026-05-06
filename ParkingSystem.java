
package parkingsystem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.sql.*;

public class ParkingSystem {
    private static final int MAX_ADMINS = 5; 
    private static AdminUser[] adminUsers = new AdminUser[MAX_ADMINS];
    private static int adminCount = 0;
    private static ArrayList<ClientUser> clientUsers = new ArrayList<>();
    private static ParkingLot parkingLot = new ParkingLot(1);
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        addAdmin(new AdminUser("admin1", "admin1", "1234567890", "admin1", "admin1"));
        addAdmin(new AdminUser("admin2", "admin2", "0987654321", "admin2", "admin2"));

        while (true) {
            System.out.println("\nWelcome to the Parking System\n");
            System.out.println("1. User Registration");
            System.out.println("2. Login");
            System.out.print("Enter your choice: ");

            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                   // registerUser(scanner);
                    break;
                case 2:
                    login(scanner);
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void addAdmin(AdminUser admin) {
        if (adminCount < MAX_ADMINS) {
            adminUsers[adminCount++] = admin;
        } else {
            System.out.println("Maximum number of admins reached.");
        }
    }

public static void registerUser(String userId, String userName, String phoneNumber, String password, String userType) {
    // Validate user type
    if (!userType.equalsIgnoreCase("Admin") && !userType.equalsIgnoreCase("Client")) {
        throw new IllegalArgumentException("User Type must be either 'Admin' or 'Client'.");
    }

    // Create user object
    UserFactory factory = UserFactoryManager.getUserFactory(userType);
    User user = factory.createUser(userId, userName, phoneNumber, password, userType);

   
    try (Connection conn = DatabaseUtil.getConnection()) {
        String query = "INSERT INTO users (user_id, user_name, phone_number, password, user_type) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, user.getUserId());
            stmt.setString(2, user.getUserName());
            stmt.setString(3, user.getPhoneNumber());
            stmt.setString(4, user.getPassword());
            stmt.setString(5, user.getUserType());
            stmt.executeUpdate();
            System.out.println("User registered successfully!");
        }
    } catch (SQLException e) {
        System.out.println("Error: Unable to register user.");
        e.printStackTrace();
        throw new RuntimeException("Database error: Unable to register user.");
    }
}

    private static void login(Scanner scanner) {
        System.out.println("\n1. User Login");
        System.out.println("2. Admin Login");
        System.out.println("3. Go Back to Main Menu");
        System.out.print("Enter your choice: ");
        int decision = scanner.nextInt();

        switch (decision) {
            case 1:
                System.out.println("Login is handled through the GUI.");
                break;
            case 2:
                adminLogin(scanner, parkingLot);
                break;
            case 3:
                System.out.println("Returning to Main Menu...");
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }
public static User userLogin(String username, String password) {
    try (Connection conn = DatabaseUtil.getConnection()) {
        String query = "SELECT * FROM users WHERE user_name = ? AND password = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String userId = rs.getString("user_id");
                    String userType = rs.getString("user_type");

                    if ("Admin".equalsIgnoreCase(userType)) {
                        return new AdminUser(userId, username, rs.getString("phone_number"), password, userType);
                    } else {
                        ClientUser user = new ClientUser(
                            userId, rs.getString("email"), username, rs.getString("phone_number"),
                            password, userType, -1, 0.0, null
                        );
                        user.loadVehiclesFromDatabase(); // Load user's vehicles
                        return user;
                    }
                }
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null; // Return null if authentication fails
}

public static ParkingLot getParkingLot() {
    return parkingLot;
}



    private static void userMenu(Scanner scanner, ClientUser user) {
        while (true) {
            System.out.println("\nUser Menu");
            System.out.println("1. Book a Slot");
            System.out.println("2. Free a Slot");
            System.out.println("3. Add a Vehicle");
            System.out.println("4. View Profile");
            System.out.println("5. Show Penalty/Ticket");
            System.out.println("6. Payment Penalty");
            System.out.println("7. Logout");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();

            switch (choice) {
               case 1:
    if (user.getVehicle().isEmpty()) {
        System.out.println("No vehicles registered. Please add a vehicle first.");
        //addVehicle(scanner, user);
        user.loadVehiclesFromDatabase(); // Refresh the list of vehicles
    } else {
        System.out.println("Select a vehicle for booking:");
        List<Vehicle> vehicles = user.getVehicle();
        for (int i = 0; i < vehicles.size(); i++) {
            System.out.println((i + 1) + ". " + vehicles.get(i).getLicenseNumber());
        }
        System.out.print("Enter the number of the vehicle: ");
        int vehicleChoice = scanner.nextInt();
        if (vehicleChoice > 0 && vehicleChoice <= vehicles.size()) {
            Vehicle selectedVehicle = vehicles.get(vehicleChoice - 1);
            user.bookSlot(parkingLot, selectedVehicle); 
        } else {
            System.out.println("Invalid vehicle selection.");
        }
    }
    break;


                case 2:
                    user.freeSlot(parkingLot);
                    break;
                case 3:
                    //addVehicle(scanner, user);
                    break;
                case 4:
                    System.out.println(user.viewProfile());
                    break;
                case 5:
                    user.showPenalty();
                    break;
                case 6:
                    payment(scanner, user);
                    break;
                case 7:
                    System.out.println("Logging out...");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    public static void payment(Scanner scanner, ClientUser user) {
        int decision;
        System.out.println("Please Choose a payment method\n1.Online Payment\n");
        decision = scanner.nextInt();
        System.out.println("Connecting To Your Bank Payment Gateway...Please Wait\n");
        if (decision == 1) {
            System.out.println("Enter your Ticket/Penalty id");
            int id = scanner.nextInt();
            System.out.println("Enter Payment: ");
            double pay = scanner.nextDouble();
            user.payPenalty(id, pay);
        }
    }


    private static void adminLogin(Scanner scanner, ParkingLot parkingLot) {
        System.out.println("Admin User Name: admin1, Admin Password: admin1");
        System.out.println("Enter Admin Name: ");
        String adminName = scanner.next();
        System.out.print("Enter Password: ");
        String adminPassword = scanner.next();

        for (AdminUser admin : adminUsers) {
            if (admin != null && admin.getUserName().equals(adminName) && admin.getPassword().equals(adminPassword)) {
                System.out.println("Admin Login Successful!\n");
                adminMenu(scanner, admin, parkingLot);
                return;
            }
        }
        System.out.println("Invalid admin credentials. Please try again.");
    }

    private static void adminMenu(Scanner scanner, AdminUser admin, ParkingLot parkingLot) {
        while (true) {
            System.out.println("\nAdmin Menu");
            System.out.println("1. Issue Penalty");
            System.out.println("2. View Registered Users");
            System.out.println("3. Update Parking Lot Slots");
            System.out.println("4. Search User By Username");
            System.out.println("5. Search Vehicle by Vehicle Registration Number");
            System.out.println("6. View Hourly Parking Usage");
            System.out.println("7. View Parking Slot Availability");
            System.out.println("8. Logout");
            System.out.print("Enter your choice: ");

            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    System.out.print("Enter Client Username: ");
                    scanner.nextLine(); 
                    String clientUsername = scanner.nextLine();
                    ClientUser client = getClientFromDatabase(clientUsername);
                    if (client == null) {
                        System.out.println("Client not found!");
                        break;

                    } 
                    System.out.print("Enter Reason for Penalty: ");
                    String reason = scanner.nextLine();

                    System.out.print("Enter Penalty Amount: ");
                    double amount = scanner.nextDouble();

                    admin.issuePenalty(client, reason, amount);
                    break;
                    
                case 2:
                    viewRegisteredUsers();
                    break;
                case 3:
                System.out.print("Enter the new total number of slots: ");
    int newTotalSlots = scanner.nextInt();
    admin.updateTotalSlots(parkingLot, newTotalSlots);
    parkingLot.reloadParkingLotData(); // Refresh parking lot data after updating slots
    break;

                  
                case 4:
                    System.out.println("Enter Username");
                    scanner.nextLine();
                    String username = scanner.nextLine();
                    admin.searchUser(username);
                    break;
                case 5:
                 System.out.println("Enter Vehicle Registration Number");
                 
                 scanner.nextLine();
                 String registerNum = scanner.nextLine();
                 admin.searchVehicle(registerNum);
                    break;
                case 6: 
                    //parkingLot.viewHourlyUsage(parkingLot);
                    break;
                case 7: 
                    //parkingLot.viewParkingSlots(parkingLot);
                    break;
                 case 8:
                    System.out.println("Logging out...");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

public void searchVehicle(String licenseNumber) {
    String query = "SELECT v.vehicle_id, v.license_number, v.color, v.vehicle_type, v.model, v.fuel_type, v.cc, u.user_name " +
                   "FROM vehicles v " +
                   "INNER JOIN users u ON v.user_id = u.user_id " +
                   "WHERE v.license_number = ?";

    try (Connection conn = DatabaseUtil.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {

        stmt.setString(1, licenseNumber); //bind

        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                int vehicleId = rs.getInt("vehicle_id");
                String ownerName = rs.getString("user_name");
                String color = rs.getString("color");
                String vehicleType = rs.getString("vehicle_type");
                String model = rs.getString("model");
                String fuelType = rs.getString("fuel_type");
                double cc = rs.getDouble("cc");
                System.out.println("\n--- Vehicle Found ---");
                System.out.println("Vehicle ID: " + vehicleId);
                System.out.println("License Number: " + licenseNumber);
                System.out.println("Color: " + color);
                System.out.println("Vehicle Type: " + vehicleType);
                System.out.println("Model: " + model);
                System.out.println("Fuel Type: " + (fuelType != null ? fuelType : "N/A"));
                System.out.println("CC: " + (cc > 0 ? cc : "N/A"));
                System.out.println("Owner: " + ownerName);
            } else {
                System.out.println("No vehicle found with license number: " + licenseNumber);
            }
        }
    } catch (SQLException e) {
        System.out.println("Error: Unable to fetch vehicle from database.");
        e.printStackTrace();
    }
}

    public static Map<String, List<ClientUser>> userMap() {
        Map<String, List<ClientUser>> NewUserMap = new HashMap<>();
        for (ClientUser users : clientUsers) {
            NewUserMap.computeIfAbsent(users.getUserName(), k -> new ArrayList<>()).add(users);
        }
        return NewUserMap;
    }
    private static ClientUser getClientFromDatabase(String username) {
    String query = "SELECT * FROM users WHERE user_name = ?";
    try (Connection conn = DatabaseUtil.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {
        stmt.setString(1, username);

        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return new ClientUser(
                    rs.getString("user_id"),
                    rs.getString("email"),
                    rs.getString("user_name"),
                    rs.getString("phone_number"),
                    rs.getString("password"),
                    rs.getString("user_type"),
                    -1, 0.0, null
                );
            }
        }
    } catch (SQLException e) {
        System.out.println("Error: Unable to fetch user from database.");
        e.printStackTrace();
    }
    return null; 
}


private static void viewRegisteredUsers() {
    String query = "SELECT user_id, user_name, phone_number, email FROM users";
    try (Connection conn = DatabaseUtil.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query);
         ResultSet rs = stmt.executeQuery()) {

        boolean hasUsers = false;

        System.out.println("--------------Registered Users--------------");
        while (rs.next()) {
            hasUsers = true;
            String userId = rs.getString("user_id");
            String userName = rs.getString("user_name");
            String phoneNumber = rs.getString("phone_number");
            String email = rs.getString("email");
            System.out.println("\n--- User Profile ---");
            System.out.println("User ID: " + userId);
            System.out.println("Username: " + userName);
            System.out.println("Phone Number: " + phoneNumber);
            System.out.println("Email: " + email);
        }

        if (!hasUsers) {
            System.out.println("No registered users found.");
        }

    } catch (SQLException e) {
        System.out.println("Error fetching registered users from the database.");
        e.printStackTrace();
    }
}
   private static void updateParkingSlots(Scanner scanner, ParkingLot parkingLot) {
    System.out.println("Current available slots: " + parkingLot.getAvailableSpots());
    System.out.print("Enter the new number of slots: ");
    int newSlots = scanner.nextInt();

    if (newSlots < 0) {
        System.out.println("Number of slots cannot be negative.");
    } else {
        parkingLot.setTotalSpots(newSlots);
        System.out.println("Parking lot slots updated successfully!");
    }
}

}
