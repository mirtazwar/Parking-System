package parkingsystem;

public class ParkingSlot {
    private int slotId;
    private boolean isAvailable;

    public ParkingSlot(int slotId, boolean isAvailable) {
        this.slotId = slotId;
        this.isAvailable = isAvailable;
    }

    public int getSlotId() {
        return slotId;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        this.isAvailable = available;
    }
}
