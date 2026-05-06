package parkingsystem;
public class Penalty {
	private int penaltyId;
	private String reason;
	private boolean IsPaid;
	private double penaltyAmount;
    public Penalty(int penaltyId, double penaltyAmount, String reason) {
        this.penaltyId = penaltyId;
        this.penaltyAmount = penaltyAmount;
        this.reason = reason;
        this.IsPaid = false;
    }
	public double getPenaltyAmount() {
		return penaltyAmount;
	}
	public void setPenaltyAmount(double penaltyAmount) {
		this.penaltyAmount = penaltyAmount;
	}
	public int getPenaltyId() {
		return penaltyId;
	}
	public void setPenaltyId(int  penaltyId) {
		this.penaltyId=penaltyId;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	public boolean getIsPaid() {
		return IsPaid;
	}
	public void setIsPaid(boolean isPaid) {
		IsPaid = isPaid;
	}
	public String generatePenaltyReport() {
		return "--PENALTY REPORT--\n"+"Penalty ID: "+this.getPenaltyId()+"\nPenalty Reason: "+this.getReason()
		+"\nPenalty Amount: "+this.getPenaltyAmount()+"\nPaid:"+this.getIsPaid();
		
	}
}
