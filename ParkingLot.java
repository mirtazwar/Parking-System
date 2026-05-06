package parkingsystem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ParkingLot {
    private int lotId;
    private int availableSpots;
    private int totalSpots;
    private int[] hourlyUsage = new int[24]; // Primitive array 
    private ParkingSlot[] slots; // Object array 

    public ParkingLot(int lotId) {
        this.lotId = lotId;
        loadParkingLotFromDatabase();
        loadHourlyUsageFromDatabase();
        loadSlotsFromDatabase();
    }

    public int getLotId() {
        return lotId;
    }

    public int getAvailableSpots() {
        return availableSpots;
    }

    public int getTotalSpots() {
        return totalSpots;
    }
    public void reloadParkingLotData() {
        loadParkingLotFromDatabase();
        System.out.println("Parking lot data reloaded. Total spots: " + totalSpots + ", Available spots: " + availableSpots);
    }
    
        public int[] getHourlyUsage() {
        return hourlyUsage;
    }

    public ParkingSlot[] getSlots() {
        return slots;
    }
    // Initialize new slots in the database
    public void initializeSlots(int newTotalSpots) {
        if (newTotalSpots <= totalSpots) return;

        String insertQuery = "INSERT INTO parking_slots (lot_id, is_available) VALUES (?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertQuery)) {

            for (int i = totalSpots + 1; i <= newTotalSpots; i++) {
                stmt.setInt(1, lotId);
                stmt.setBoolean(2, true);
                stmt.addBatch();
            }
            stmt.executeBatch();
            System.out.println("Initialized new slots from " + (totalSpots + 1) + " to " + newTotalSpots);
        } catch (SQLException e) {
            System.out.println("Error initializing new parking slots.");
            e.printStackTrace();
        }
    }
public String setTotalSpots(int newTotalSpots) {
    if (newTotalSpots < totalSpots - availableSpots) {
        return "New total spots cannot be less than currently booked spots.";
    }

    if (newTotalSpots > totalSpots) {
        // Add new slots to the database
        initializeSlots(newTotalSpots);
    }

    // Update the total and available spots in the database
    String updateQuery = "UPDATE parking_lot SET total_spots = ?, available_spots = ? WHERE lot_id = ?";
    try (Connection conn = DatabaseUtil.getConnection();
         PreparedStatement stmt = conn.prepareStatement(updateQuery)) {

        int newAvailableSpots = newTotalSpots - (totalSpots - availableSpots);
        stmt.setInt(1, newTotalSpots);
        stmt.setInt(2, newAvailableSpots);
        stmt.setInt(3, lotId);

        int rowsUpdated = stmt.executeUpdate();
        if (rowsUpdated > 0) {
            this.totalSpots = newTotalSpots;
            this.availableSpots = newAvailableSpots;
            return "Parking lot updated successfully.\nNew Total Spots: " + newTotalSpots + "\nAvailable Spots: " + newAvailableSpots;
        } else {
            return "Failed to update the parking lot.";
        }
    } catch (SQLException e) {
        e.printStackTrace();
        return "Error updating parking lot: " + e.getMessage();
    }
}


   
    private void loadParkingLotFromDatabase() {
        String query = "SELECT total_spots, available_spots FROM parking_lot WHERE lot_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, lotId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                this.totalSpots = rs.getInt("total_spots");
                this.availableSpots = rs.getInt("available_spots");
            }
        } catch (SQLException e) {
            System.out.println("Error loading parking lot data.");
            e.printStackTrace();
        }
    }

    
    private void loadHourlyUsageFromDatabase() {
        String query = "SELECT hour, spots FROM hourly_parking_usage WHERE lot_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, lotId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int hour = rs.getInt("hour");
                int spots = rs.getInt("spots");
                if (hour >= 0 && hour < 24) {
                    hourlyUsage[hour] = spots;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error loading hourly usage data.");
            e.printStackTrace();
        }
    }

    // Update hourly parking usage in the database
    private void updateHourlyUsageInDatabase(int hour, int spots) {
        String query = "INSERT INTO hourly_parking_usage (lot_id, hour, spots) VALUES (?, ?, ?) " +
                       "ON DUPLICATE KEY UPDATE spots = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, lotId);
            stmt.setInt(2, hour);
            stmt.setInt(3, spots);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating hourly usage data.");
            e.printStackTrace();
        }
    }
  private void loadSlotsFromDatabase() {
    String query = "SELECT slot_id, is_available FROM parking_slots WHERE lot_id = ?";
    try (Connection conn = DatabaseUtil.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {

        stmt.setInt(1, lotId);
        ResultSet rs = stmt.executeQuery();
        slots = new ParkingSlot[totalSpots];
        int i = 0;

        while (rs.next()) {
            int slotId = rs.getInt("slot_id");
            boolean isAvailable = rs.getBoolean("is_available");
            slots[i] = new ParkingSlot(slotId, isAvailable);
            i++;
        }
    } catch (SQLException e) {
        System.out.println("Error loading parking slots.");
        e.printStackTrace();
    }
}


 public int bookSlot(int vehicleId, String userId) {
    reloadParkingLotData();
    String selectQuery = "SELECT slot_id FROM parking_slots WHERE is_available = TRUE AND lot_id = ? LIMIT 1 FOR UPDATE";
    String updateSlotQuery = "UPDATE parking_slots SET is_available = FALSE, vehicle_id = ? WHERE slot_id = ?";
    String updateUserQuery = "UPDATE users SET current_slot_id = ? WHERE user_id = ?";
    String decrementAvailableSpots = "UPDATE parking_lot SET available_spots = available_spots - 1 WHERE lot_id = ?";
    String updateHourlyUsage = "INSERT INTO hourly_parking_usage (lot_id, hour, spots) VALUES (?, ?, 1) " +
                               "ON DUPLICATE KEY UPDATE spots = spots + 1";

    try (Connection conn = DatabaseUtil.getConnection()) {
        conn.setAutoCommit(false);

        try (PreparedStatement selectStmt = conn.prepareStatement(selectQuery);
             PreparedStatement updateSlotStmt = conn.prepareStatement(updateSlotQuery);
             PreparedStatement updateUserStmt = conn.prepareStatement(updateUserQuery);
             PreparedStatement decrementStmt = conn.prepareStatement(decrementAvailableSpots);
             PreparedStatement hourlyStmt = conn.prepareStatement(updateHourlyUsage)) {

            selectStmt.setInt(1, lotId);
            ResultSet rs = selectStmt.executeQuery();

            if (rs.next()) {
                int slotId = rs.getInt("slot_id");

                // Update parking_slots table
                updateSlotStmt.setInt(1, vehicleId);
                updateSlotStmt.setInt(2, slotId);
                updateSlotStmt.executeUpdate();

                // Update users table
                updateUserStmt.setInt(1, slotId);
                updateUserStmt.setString(2, userId);
                updateUserStmt.executeUpdate();

                // Update parking_lot table
                decrementStmt.setInt(1, lotId);
                decrementStmt.executeUpdate();

                // Update hourly_parking_usage table
                int currentHour = java.time.LocalDateTime.now().getHour();
                hourlyStmt.setInt(1, lotId);
                hourlyStmt.setInt(2, currentHour);
                hourlyStmt.executeUpdate();

                conn.commit();
                return slotId;
            } else {
                conn.rollback();
                System.out.println("No available slots.");
                return -1;
            }
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    } catch (SQLException e) {
        System.out.println("Error booking slot.");
        e.printStackTrace();
        return -1;
    }
   
}




    // Free a parking slot
public void freeSlot(String userId) {
    reloadParkingLotData();
    String getUserSlotQuery = "SELECT current_slot_id FROM users WHERE user_id = ?";
    String updateSlotQuery = "UPDATE parking_slots SET is_available = TRUE, vehicle_id = NULL WHERE slot_id = ?";
    String updateUserQuery = "UPDATE users SET current_slot_id = NULL WHERE user_id = ?";
    String incrementAvailableSpots = "UPDATE parking_lot SET available_spots = available_spots + 1 WHERE lot_id = ?";

    try (Connection conn = DatabaseUtil.getConnection();
         PreparedStatement getUserSlotStmt = conn.prepareStatement(getUserSlotQuery);
         PreparedStatement updateSlotStmt = conn.prepareStatement(updateSlotQuery);
         PreparedStatement updateUserStmt = conn.prepareStatement(updateUserQuery);
         PreparedStatement incrementStmt = conn.prepareStatement(incrementAvailableSpots)) {

        getUserSlotStmt.setString(1, userId);
        ResultSet rs = getUserSlotStmt.executeQuery();

        if (rs.next()) {
            int slotId = rs.getInt("current_slot_id");

            if (slotId <= 0) {
                System.out.println("No slot is currently booked by this user.");
                return;
            }

            // Update parking_slots table
            updateSlotStmt.setInt(1, slotId);
            int rowsUpdated = updateSlotStmt.executeUpdate();

            if (rowsUpdated > 0) {
                // Update users table
                updateUserStmt.setString(1, userId);
                updateUserStmt.executeUpdate();

                // Update parking_lot table
                incrementStmt.setInt(1, lotId);
                incrementStmt.executeUpdate();

                System.out.println("Slot " + slotId + " freed and availability updated.");
            } else {
                System.out.println("Error: Slot ID " + slotId + " not found or already available.");
            }
        } else {
            System.out.println("User not found or no slot booked.");
        }
    } catch (SQLException e) {
        System.out.println("Error freeing slot in database.");
        e.printStackTrace();
    }
}

public String getHourlyUsageReport() {
    StringBuilder report = new StringBuilder();
    int[] hourlyUsage = getHourlyUsage();
    for (int i = 0; i < hourlyUsage.length; i++) {
        report.append("Hour ").append(i).append(": ").append(hourlyUsage[i]).append(" vehicles parked\n");
    }
    return report.toString();
}

public String getParkingSlotsReport() {
    loadSlotsFromDatabase();
    StringBuilder slotReport = new StringBuilder();
    ParkingSlot[] slots = getSlots();
    for (ParkingSlot slot : slots) {
        slotReport.append("Slot ").append(slot.getSlotId())
                  .append(": ")
                  .append(slot.isAvailable() ? "Available" : "Occupied")
                  .append("\n");
    }

    return slotReport.toString(); 
}

}
