import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

public class FileWriter extends BufferedWriter {
    public int ioCounter = 0;
    public boolean isNewBatch = true;

    public FileWriter(File file) throws IOException {
        super(new java.io.FileWriter(file));
    }

    //write one batch back to file
    public void writeOneBatch(List<Tuple> oneBatch, Byte tupleNumInOneBlock) {
        try {
            if (oneBatch != null) {
                if (oneBatch.size() % tupleNumInOneBlock == 0)
                    ioCounter += Math.floorDiv(oneBatch.size(), tupleNumInOneBlock);
                else
                    ioCounter += Math.floorDiv(oneBatch.size(), tupleNumInOneBlock) + 1;
                for (Tuple tuple : oneBatch) {
                    if (isNewBatch) {
                        this.write(tupleParser(tuple));
                        isNewBatch = false;
                    } else {
                        this.newLine();
                        this.write(tupleParser(tuple));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //parse tuple to string format
    public String tupleParser(Tuple tuple) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-mm-dd");
        return String.format("%1$8d", tuple.claimID) +
                dateFormatter.format(tuple.claimDate) +
                String.format("%1$09d", tuple.clientID) +
                String.format("%1$-25s", tuple.clientName) +
                String.format("%1$-150s", tuple.clientAddress) +
                String.format("%1$-28s", tuple.clientEmail) +
                String.format("%1$02d", tuple.itemID) +
                String.format("%1$09.2f", tuple.amountDamage) +
                String.format("%1$09.2f", tuple.amountPaid);
    }

    public void writeTuple(Tuple tuple) {
        try {
            if (tuple != null) {
                this.write(tuple.toString());
                this.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
