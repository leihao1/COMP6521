import java.text.SimpleDateFormat;
import java.util.Date;

public class Tuple implements Comparable<Tuple> {
    private int claimID;
    private Date claimDate;
    private int clientID;
    private String clientName;
    private String clientAddress;
    private String clientEmail;
    private short itemID;
    private double amountDamage;
    private double amountPaid;

    public Tuple(int claimID, Date claimDate, int clientID, String clientName, String clientAddress,
                 String clientEmail, short itemID, double amountDamage, double amountPaid) {
        super();
        this.claimID = claimID;
        this.claimDate = claimDate;
        this.clientID = clientID;
        this.clientName = clientName;
        this.clientAddress = clientAddress;
        this.clientEmail = clientEmail;
        this.itemID = itemID;
        this.amountDamage = amountDamage;
        this.amountPaid = amountPaid;
    }


    public int getClientID() {
        return clientID;
    }
    public double getAmountPaid() {
        return amountPaid;
    }


    /**
     * Write tuples back to disk as string format
     * @return string format of tuple
     */
    @Override
    public String toString() {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-mm-dd");
        return String.format("%1$8d", claimID) +
                dateFormatter.format(claimDate) +
                String.format("%1$09d", clientID) +
                String.format("%1$-25s", clientName) +
                String.format("%1$-150s", clientAddress) +
                String.format("%1$-28s", clientEmail) +
                String.format("%1$02d", itemID) +
                String.format("%1$09.2f", amountDamage) +
                String.format("%1$09.2f", amountPaid);
    }

    @Override
    public int compareTo(Tuple other) {
        if (this.clientID < other.clientID)
            return -1;
        else if (this.clientID > other.clientID)
            return 1;
        else
            return 0;
    }
}
