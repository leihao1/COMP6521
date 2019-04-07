import java.util.Date;

public class Tuple {
    public int claimID;
    public Date claimDate;
    public int clientID;
    public String clientName;
    public String clientAddress;
    public String clientEmail;
    public byte itemID;
    public float amountDamage;
    public double amountPaid;

    public Tuple(int claimID, Date claimDate, int clientID, String clientName, String clientAddress,
                 String clientEmail, byte itemID, float amountDamage, double amountPaid) {
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

}
