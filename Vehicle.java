/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parkingsystem;

public class Vehicle {
	private int vehicleId;
	private String color;
	private String licenseNumber;
        
	public Vehicle(int vehicleId,String licenseNumber,String color) {
		this.vehicleId=vehicleId;
		this.licenseNumber=licenseNumber;
		this.color=color;
	}
	
	public int getVehicleId() {
		return vehicleId;
	}
	public void setVehicleId(int vehicleId) {
		this.vehicleId=vehicleId;
	}
	
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color=color;
	}
	public String getLicenseNumber() {
		return licenseNumber;
	}
	public void setLicenseNumber(String licenseNumber) {
		this.licenseNumber=licenseNumber;
	}
	public String getDetails() {
		return "Vehicle Details: \n"+"Vehicle ID "+this.getVehicleId() + "Vehicle License Number: "+
	this.getLicenseNumber()+"Vehicle License Number: "+this.getColor();
	}
}
