package parkingsystem;
public class Car extends Vehicle{


	private String carModel;
	private String fuelType;
	
public Car(int vehicleId, String licenseNumber, String color, String carModel, String fuelType) {
    super(vehicleId, licenseNumber, color); 
    this.carModel = carModel;
    this.fuelType = fuelType;
}

	public String getCarModel() {
		return carModel;
	}

	public void setCarModel(String carModel) {
		this.carModel = carModel;
	}

	public String getFuelType() {
		return fuelType;
	}

	public void setFuelType(String fuelType) {
		this.fuelType = fuelType;
	}
	public void carDetails() {
		System.out.println(super.getDetails()+" Car Model: "+this.getCarModel()+" FuelType: "+this.getFuelType());
	}
	
}
