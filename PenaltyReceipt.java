/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parkingsystem;

/**
 *
 * @author Walton
 */
public class PenaltyReceipt implements Recipt{
        private int penaltyId;
    private String reason;
    private double penaltyAmount;

    public PenaltyReceipt(int penaltyId, String reason, double penaltyAmount) {
        this.penaltyId = penaltyId;
        this.reason = reason;
        this.penaltyAmount = penaltyAmount;
    }

    @Override
    public void generateReceipt() {
        System.out.println("\n--- PENALTY RECEIPT ---");
        System.out.println("Penalty ID: " + penaltyId);
        System.out.println("Reason: " + reason);
        System.out.println("Penalty Amount: $" + penaltyAmount);
        System.out.println("------------------------\n");
    }
}
