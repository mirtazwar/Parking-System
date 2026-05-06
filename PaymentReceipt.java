package parkingsystem;

public class PaymentReceipt implements Recipt {
    private int paymentId;
    private double amountPaid;

    public PaymentReceipt(int paymentId, double amountPaid) {
        this.paymentId = paymentId;
        this.amountPaid = amountPaid;
    }

    @Override
    public void generateReceipt() {
        System.out.println("\n--- Payment Receipt ---");
        System.out.println("Payment ID: " + paymentId);
        System.out.println("Amount Paid: $" + amountPaid);
        System.out.println("------------------------");
    }
}

