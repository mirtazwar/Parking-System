
package parkingsystem;

/**
 *
 * @author Walton
 */
public class ReceiptFactory {
	public static Recipt createReceipt(Object...params) {
		return new PenaltyReceipt(
				
				(int)params[0],
				(String)params[1],
				(double) params[2]  
				);
	}
}
