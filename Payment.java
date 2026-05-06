package parkingsystem;
public class Payment {
    private int paymentId;
    private boolean paymentStatus;
    private double amountPaid;
    private int penaltyId;
     private Recipt receipt; // Composition with the Recipt interfacetion
	public Payment(int paymentId,int penaltyId,double amountPaid,boolean paymentStatus) {
		this.paymentId=paymentId;
		this.penaltyId=penaltyId;
		this.amountPaid=amountPaid;
		this.paymentStatus=paymentStatus;;
                
                this.receipt = new PaymentReceipt(paymentId, amountPaid);
                
	}
	public int getPenaltyId() {
		return penaltyId;
	}
	public void setPenaltyId(int penaltyId) {
		this.penaltyId = penaltyId;
	}
	public double getAmountPaid() {
		return amountPaid;
	}
	public void setAmountPaid(double amountPaid) {
		this.amountPaid = amountPaid;
	}
	public int getPaymentId() {
		return paymentId;
	}
	public void setPaymentId(int paymentId) {
		this.paymentId = paymentId;
	}
	public boolean isPaymentStatus() {
		return paymentStatus;
	}
	public void setPaymentStatus(boolean paymentStatus) {
		this.paymentStatus = paymentStatus;
	}
public String generateReceipt() {
    StringBuilder receipt = new StringBuilder();
    receipt.append("PAYMENT RECEIPT\n");
    receipt.append("Payment ID: ").append(paymentId).append("\n");
    receipt.append("Total Amount Paid: ").append(amountPaid).append("\n");
    receipt.append("Payment Status: ").append(paymentStatus ? "Success" : "Failed").append("\n");
    return receipt.toString(); // Return the receipt as a String
}

}
