package parkingsystem;
public class Bike extends Vehicle{

	private String bikeModel;
	private double cc;
        public Bike(int vehicleId, String licenseNumber, String color, String bikeModel, double cc) {
            super(vehicleId, licenseNumber, color); 
            this.bikeModel = bikeModel;
            this.cc = cc;
}

	public String getBikeModel() {
		return bikeModel;
	}
	public void setBikeModel(String bikeModel) {
		this.bikeModel = bikeModel;
	}
	public double getCc() {
		return cc;
	}
	public void setCc(double cc) {
		this.cc = cc;
	}
	public void bikeDetails() {
		System.out.println(super.getDetails()+" Bike Model: "+this.getBikeModel()+" CC: "+this.getCc());
	}
	
}
