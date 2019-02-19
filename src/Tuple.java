import java.text.SimpleDateFormat;
import java.util.Date;

public class Tuple implements Comparable<Tuple> {
    public int claimID;
    public Date claimDate;
    public int clientID;
    public String clientName;
    public String clientAddress;
    public String clientEmail;
    public byte itemID;
    public float amountDamage;
    public float amountPaid;

    public Tuple(int claimID, Date claimDate, int clientID, String clientName, String clientAddress,
                 String clientEmail, byte itemID, float amountDamage, float amountPaid) {
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




//    //tuple parser
//    public String toString() {
//        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-mm-dd");
//        return String.format("%1$8d", claimID) +
//                dateFormatter.format(claimDate) +
//                String.format("%1$09d", clientID) +
//                String.format("%1$-25s", clientName) +
//                String.format("%1$-150s", clientAddress) +
//                String.format("%1$-28s", clientEmail) +
//                String.format("%1$02d", itemID) +
//                String.format("%1$09.2f", amountDamage) +
//                String.format("%1$09.2f", amountPaid);
//        return String.format("%1$09d", clientID) + String.format("%1$09.2f", amountPaid);
//    }

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
