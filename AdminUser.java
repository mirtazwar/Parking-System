package parkingsystem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.sql.*;

public class AdminUser extends User {
    private int adminCode;

    public AdminUser(String userId, String userName, String phoneNumber, String password, String userType) {
        super(userId, userName, phoneNumber, password, userType);
    }


    public int getAdminCode() {
        return adminCode;
    }

    public void setAdminCode(int adminCode) {
        this.adminCode = adminCode;
    }


public String issuePenalty(ClientUser client, String reason, double amount) {
    if (client == null) {
        return "Error: Client user not found.";
    }

    String query = "INSERT INTO penalties (user_id, reason, amount, is_paid) VALUES (?, ?, ?, ?)";
    try (Connection conn = DatabaseUtil.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {

        stmt.setString(1, client.getUserId()); 
        stmt.setString(2, reason);            
        stmt.setDouble(3, amount);            
        stmt.setBoolean(4, false);            

        int rowsAffected = stmt.executeUpdate();

        if (rowsAffected > 0) {
             return generatePenaltyReceipt(client, reason, amount);
        } else {
            return "Failed to issue penalty.";
        }
    } catch (SQLException e) {
        e.printStackTrace();
        return "Error: Unable to issue penalty. " + e.getMessage();
    }
}
public ClientUser searchClientByUsername(String username) {
    String query = "SELECT * FROM users WHERE user_name = ? AND user_type = 'client'";
    try (Connection conn = DatabaseUtil.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {

        stmt.setString(1, username);
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                String userId = rs.getString("user_id");
                String userName = rs.getString("user_name");
                String phoneNumber = rs.getString("phone_number");
                String email = rs.getString("email");
                String password = rs.getString("password");
                ClientUser client = new ClientUser(userId, email, userName, phoneNumber, password, "client", -1, 0.0, null);
                client.loadVehiclesFromDatabase();
                return client;
            } else {
                System.out.println("No client found with username: " + username);
            }
        }
    } catch (SQLException e) {
        System.out.println("Error searching for client in database.");
        e.printStackTrace();
    }
    return null; 
}


public String searchUser(String userName) {
    String query = "SELECT user_id, user_name, phone_number, email, user_type FROM users WHERE user_name LIKE ?";
    StringBuilder userDetails = new StringBuilder();

    try (Connection conn = DatabaseUtil.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {
        stmt.setString(1, "%" + userName + "%");

        try (ResultSet rs = stmt.executeQuery()) {
            boolean hasResults = false;

            while (rs.next()) {
                hasResults = true;
                String userId = rs.getString("user_id");
                String name = rs.getString("user_name");
                String phone = rs.getString("phone_number");
                String email = rs.getString("email");
                String userType = rs.getString("user_type");

                userDetails.append("--- User Profile ---\n")
                           .append("User ID: ").append(userId).append("\n")
                           .append("Username: ").append(name).append("\n")
                           .append("Phone Number: ").append(phone).append("\n")
                           .append("Email: ").append(email).append("\n")
                           .append("User Type: ").append(userType).append("\n\n");
            }

            if (!hasResults) {
                userDetails.append("No user found with the username: ").append(userName);
            }
        }
    } catch (SQLException e) {
        userDetails.append("Error: Unable to search for user in the database.\n").append(e.getMessage());
        e.printStackTrace();
    }

    return userDetails.toString();
}


public String searchVehicle(String licenseNumber) {
    String query = "SELECT v.vehicle_id, v.license_number, v.color, v.vehicle_type, v.model, v.fuel_type, v.cc, u.user_name " +
                   "FROM vehicles v " +
                   "INNER JOIN users u ON v.user_id = u.user_id " +
                   "WHERE v.license_number = ?";
    StringBuilder vehicleDetails = new StringBuilder();

    try (Connection conn = DatabaseUtil.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {
        stmt.setString(1, licenseNumber); 
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                int vehicleId = rs.getInt("vehicle_id");
                String ownerName = rs.getString("user_name");
                String color = rs.getString("color");
                String vehicleType = rs.getString("vehicle_type");
                String model = rs.getString("model");
                String fuelType = rs.getString("fuel_type");
                double cc = rs.getDouble("cc");

                vehicleDetails.append("--- Vehicle Found ---\n")
                              .append("Vehicle ID: ").append(vehicleId).append("\n")
                              .append("License Number: ").append(licenseNumber).append("\n")
                              .append("Color: ").append(color).append("\n")
                              .append("Vehicle Type: ").append(vehicleType).append("\n")
                              .append("Model: ").append(model).append("\n")
                              .append("Fuel Type: ").append(fuelType != null ? fuelType : "N/A").append("\n")
                              .append("CC: ").append(cc > 0 ? cc : "N/A").append("\n")
                              .append("Owner: ").append(ownerName);
            } else {
                return "No vehicle found with license number: " + licenseNumber;
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
        return "Error: Unable to fetch vehicle from database.";
    }

    return vehicleDetails.toString();
}

public String updateTotalSlots(ParkingLot parkingLot, int newTotalSlots) {
    if (parkingLot == null) {
        return "Parking lot is not valid.";
    }
    if (newTotalSlots <= 0) {
        return "Invalid number of slots. Must be greater than 0.";
    }
    return parkingLot.setTotalSpots(newTotalSlots);
}




public void viewAvailableSlots(ParkingLot parkingLot) {
    String query = "SELECT available_spots FROM parking_lot WHERE lot_id = ?";
    try (Connection conn = DatabaseUtil.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {

        stmt.setInt(1, parkingLot.getLotId());
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            int availableSpots = rs.getInt("available_spots");
            System.out.println("Available Slots: " + availableSpots);
        }
    } catch (SQLException e) {
        System.out.println("Error fetching available slots.");
        e.printStackTrace();
    }
}


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AdminUser)) return false;
        AdminUser that = (AdminUser) o;
        return userId.equals(that.userId) &&
                phoneNumber.equals(that.phoneNumber) &&
                Objects.equals(userName, that.userName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, phoneNumber, userName);
    }
    
@Override
public String viewProfile() {
    return super.viewProfile() + "\nAdmin Code: " + adminCode;
}
  public String generatePenaltyReceipt(ClientUser client, String reason, double amount) {
    if (client == null) {
        return "Error: No client selected.";
    }

    StringBuilder receipt = new StringBuilder();
    receipt.append("---- Penalty Receipt ----\n");
    receipt.append("Client Name: ").append(client.getUserName()).append("\n");
    receipt.append("Client ID: ").append(client.getUserId()).append("\n");
    receipt.append("Reason: ").append(reason).append("\n");
    receipt.append("Amount: RM ").append(String.format("%.2f", amount)).append("\n");
    receipt.append("--------------------------\n");
    receipt.append("Thank you!");

    return receipt.toString();
}

    
  
}
