package parkingsystem;
import java.util.HashMap;
import java.util.*;
import java.sql.*;

public class ClientUser extends User {
    private List<Vehicle> vehicles = new ArrayList<>();
    private List<Penalty> penalties = new ArrayList<>();
    private List<Payment> payments = new ArrayList<>();
    private double parkingDuration;
    private int slotId = -1;
   
  

public ClientUser(String userId, String email, String userName, String phoneNumber, String password, String userType,
                  int slotId, double parkingDuration, Vehicle vehicle) {
    super(userId, userName, phoneNumber, password, userType);
    this.slotId = slotId;
    this.parkingDuration = parkingDuration;
    this.vehicles = new ArrayList<>(); 
   this.penalties=new ArrayList<>();
   this.payments=new  ArrayList<>();
     this.slotId = loadSlotIdFromDatabase(userId);
}
private int loadSlotIdFromDatabase(String userId) {
    String query = "SELECT current_slot_id FROM users WHERE user_id = ?";
    try (Connection conn = DatabaseUtil.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {
        stmt.setString(1, userId);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            int dbSlotId = rs.getInt("current_slot_id");
            return rs.wasNull() ? -1 : dbSlotId; 
        }
    } catch (SQLException e) {
        System.out.println("Error loading slot ID from database.");
        e.printStackTrace();
    }
    return -1; // Default to -1 if no slot is booked or an error occurs
}

	public int getSlotId() {
		return slotId;
	}
	public void setSlotInt(int slotId) {
		this.slotId = slotId;
	}
	public double getParkingDuration() {
		return parkingDuration;
	}
	public void setParkingDuration(double parkingDuration) {
		this.parkingDuration = parkingDuration;
	}

	public List<Vehicle> getVehicle(){
		return vehicles;
	}
	public void addPenalty(Penalty penalty) { 
	    penalties.add(penalty); 
	    System.out.println("Penalty added: " + penalty.getPenaltyId());
	}
 public boolean bookSlot(ParkingLot parkingLot, Vehicle vehicle) {
    if (parkingLot == null) {
        System.out.println("Error: Parking lot instance is not available.");
        return false;
    }

    if (vehicle == null) {
        System.out.println("Error: Vehicle is not selected or null.");
        return false;
    }

    if (slotId != -1) { // If a slot is already booked
        System.out.println("Error: You already have a booked slot with ID: " + slotId);
        return false;
    }

    try {
        int bookedSlotId = parkingLot.bookSlot(vehicle.getVehicleId(), this.getUserId());
        if (bookedSlotId != -1) {
            this.slotId = bookedSlotId; // Update local slot ID
            System.out.println("Slot " + bookedSlotId + " booked successfully for vehicle: " + vehicle.getLicenseNumber());
            return true;
        } else {
            System.out.println("No available slots.");
            return false;
        }
    } catch (Exception e) {
        System.out.println("Error occurred while booking a slot: " + e.getMessage());
        e.printStackTrace();
        return false;
    }
}
public void addVehicle(Vehicle vehicle) {
    String checkQuery = "SELECT COUNT(*) FROM vehicles WHERE license_number = ?";
    String insertQuery = "INSERT INTO vehicles (license_number, color, user_id, vehicle_type, model, fuel_type, cc) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?)";
    try (Connection conn = DatabaseUtil.getConnection();
         PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
         PreparedStatement insertStmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
        checkStmt.setString(1, vehicle.getLicenseNumber());
        ResultSet rs = checkStmt.executeQuery();
        if (rs.next() && rs.getInt(1) > 0) {
            System.out.println("Error: A vehicle with this license number already exists.");
            return;
        }

        // Insert the vehicle
        insertStmt.setString(1, vehicle.getLicenseNumber());
        insertStmt.setString(2, vehicle.getColor());
        insertStmt.setString(3, this.getUserId());
        insertStmt.setString(4, vehicle instanceof Car ? "Car" : "Bike");
        insertStmt.setString(5, vehicle instanceof Car ? ((Car) vehicle).getCarModel() : ((Bike) vehicle).getBikeModel());
        if (vehicle instanceof Car) {
            insertStmt.setString(6, ((Car) vehicle).getFuelType());
            insertStmt.setNull(7, java.sql.Types.DOUBLE);
        } else if (vehicle instanceof Bike) {
            insertStmt.setNull(6, java.sql.Types.VARCHAR);
            insertStmt.setDouble(7, ((Bike) vehicle).getCc());
        }

        insertStmt.executeUpdate();

        // Retrieve the generated vehicle ID
        try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                int vehicleId = generatedKeys.getInt(1);
                vehicle.setVehicleId(vehicleId);
                vehicles.add(vehicle);
                System.out.println("Vehicle added successfully: " + vehicle.getLicenseNumber());
            }
        }
    } catch (SQLException e) {
        System.out.println("Error: Unable to register vehicle.");
        e.printStackTrace();
    }
}


public String showPenalty() {
    String query = "SELECT penalty_id, reason, amount, is_paid FROM penalties WHERE user_id = ?";
    StringBuilder penaltyDetails = new StringBuilder();
    

    try (Connection conn = DatabaseUtil.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {

        stmt.setString(1, this.getUserId());

        try (ResultSet rs = stmt.executeQuery()) {
            boolean hasPenalties = false;

            while (rs.next()) {
                hasPenalties = true;
                int penaltyId = rs.getInt("penalty_id");
                String reason = rs.getString("reason");
                double amount = rs.getDouble("amount");
                boolean isPaid = rs.getBoolean("is_paid");

                penaltyDetails.append("Penalty ID: ").append(penaltyId).append("\n")
                              .append("Reason: ").append(reason).append("\n")
                              .append("Amount: $").append(amount).append("\n")
                              .append("Paid: ").append(isPaid ? "Yes" : "No").append("\n\n");
            }

            if (!hasPenalties) {
                penaltyDetails.append("You have no penalties.");
            }
        }
    } catch (SQLException e) {
        System.out.println("Error fetching penalties from the database.");
        e.printStackTrace();
        return "Error fetching penalties from the database.";
    }

    return penaltyDetails.toString();
}


@Override 
    public String viewProfile() {
        
        StringBuilder profile = new StringBuilder(super.viewProfile());
        profile.append("\nRegistered Vehicles:\n");

    try (Connection conn = DatabaseUtil.getConnection()) {
        String query = "SELECT * FROM vehicles WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, this.getUserId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    profile.append("Vehicle ID: ").append(rs.getInt("vehicle_id"))
                           .append(", License Number: ").append(rs.getString("license_number"))
                           .append(", Color: ").append(rs.getString("color"))
                           .append(", Type: ").append(rs.getString("vehicle_type"))
                           .append(", Model: ").append(rs.getString("model"));
                    if ("Car".equalsIgnoreCase(rs.getString("vehicle_type"))) {
                        profile.append(", Fuel Type: ").append(rs.getString("fuel_type"));
                    } else if ("Bike".equalsIgnoreCase(rs.getString("vehicle_type"))) {
                        profile.append(", CC: ").append(rs.getDouble("cc"));
                    }
                    profile.append("\n");
                }
            }
        }
    } catch (SQLException e) {
        System.out.println("Error: Unable to fetch vehicles.");
        e.printStackTrace();
    }

    return profile.toString();
}
public boolean freeSlot(ParkingLot parkingLot) {
    if (parkingLot == null) {
        System.out.println("Error: Parking lot is not available.");
        return false;
    }

    if (slotId <= 0) { // If no slot is booked
        System.out.println("No slot is currently booked.");
        return false;
    }

    try {
        parkingLot.freeSlot(this.getUserId()); // Free slot in the database
        System.out.println("Slot " + slotId + " freed successfully for user: " + this.getUserName());
        slotId = -1; // Reset local slot ID
        return true;
    } catch (Exception e) {
        System.out.println("Error occurred while freeing the slot: " + e.getMessage());
        e.printStackTrace();
        return false;
    }
}

public String payPenalty(int penaltyId, double amountPaid) {
    String queryPenalty = "SELECT amount, is_paid FROM penalties WHERE penalty_id = ? AND user_id = ?";
    try (Connection conn = DatabaseUtil.getConnection();
         PreparedStatement penaltyQueryStmt = conn.prepareStatement(queryPenalty)) {
        
        penaltyQueryStmt.setInt(1, penaltyId);
        penaltyQueryStmt.setString(2, this.getUserId());

        try (ResultSet rs = penaltyQueryStmt.executeQuery()) {
            if (rs.next()) {
                double penaltyAmount = rs.getDouble("amount");
                boolean isPaid = rs.getBoolean("is_paid");

                if (isPaid) {
                    return "This penalty has already been paid.";
                }

                if (amountPaid != penaltyAmount) {
                    return "The amount entered does not match the penalty amount. Please enter the exact amount.";
                }

                String updatePenalty = "UPDATE penalties SET is_paid = TRUE WHERE penalty_id = ?";
                try (PreparedStatement penaltyStmt = conn.prepareStatement(updatePenalty)) {
                    penaltyStmt.setInt(1, penaltyId);
                    int rowsUpdated = penaltyStmt.executeUpdate();

                    if (rowsUpdated > 0) {
                        String insertPayment = "INSERT INTO payments (penalty_id, amount_paid, payment_status) VALUES (?, ?, ?)";
                        try (PreparedStatement paymentStmt = conn.prepareStatement(insertPayment, Statement.RETURN_GENERATED_KEYS)) {
                            paymentStmt.setInt(1, penaltyId);
                            paymentStmt.setDouble(2, amountPaid);
                            paymentStmt.setBoolean(3, true);
                            paymentStmt.executeUpdate();

                            try (ResultSet generatedKeys = paymentStmt.getGeneratedKeys()) {
                                if (generatedKeys.next()) {
                                    int paymentId = generatedKeys.getInt(1);
                                    Payment payment = new Payment(paymentId, penaltyId, amountPaid, true);
                                    payments.add(payment);

                                    // Return the receipt
                                    return payment.generateReceipt();
                                }
                            }
                        }
                    } else {
                        return "Error: Unable to update penalty status.";
                    }
                }
            } else {
                return "Penalty not found or does not belong to the current user.";
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
        return "Error: Unable to process payment.";
    }
    return "Unexpected error occurred.";
}



public void loadVehiclesFromDatabase() {
    String query = "SELECT * FROM vehicles WHERE user_id = ?";
    try (Connection conn = DatabaseUtil.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {

        stmt.setString(1, this.getUserId());
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int vehicleId = rs.getInt("vehicle_id");
                String licenseNumber = rs.getString("license_number");
                String color = rs.getString("color");
                String vehicleType = rs.getString("vehicle_type");
                String model = rs.getString("model");

                Vehicle vehicle;
                if ("Car".equalsIgnoreCase(vehicleType)) {
                    String fuelType = rs.getString("fuel_type");
                    vehicle = new Car(vehicleId, color, licenseNumber, model, fuelType);
                } else if ("Bike".equalsIgnoreCase(vehicleType)) {
                    double cc = rs.getDouble("cc");
                    vehicle = new Bike(vehicleId, licenseNumber, color, model, cc);
                } else {
                    System.out.println("Unknown vehicle type: " + vehicleType);
                    continue;
                }

                vehicles.add(vehicle);
            }
        }
    } catch (SQLException e) {
        System.out.println("Error loading vehicles for user: " + this.getUserId());
        e.printStackTrace();
    }
}
	
}