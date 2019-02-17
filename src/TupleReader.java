import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TupleReader extends BufferedReader {
    private short tupleNumInOneBlock = 15;
    private boolean isFinished = false;
    private double preserveMemory = 0.0;
    private File file;//TODO: delete file to save memory
    private int ioCount = 0;

    public TupleReader(File file, float preserveMemPercentage) throws FileNotFoundException {
        super(new FileReader(file));
        this.preserveMemory = Utils.getMaxMemory() * preserveMemPercentage;
        this.file = file;
    }

    public List<Tuple> getNextBlock() {
        List<Tuple> oneBlock = new ArrayList<>(tupleNumInOneBlock);
        if (Utils.getFreeMemory() > preserveMemory) {
            for (int i = 0; i < tupleNumInOneBlock; i++) {
                Tuple oneTuple = getNextTuple();
                if (oneTuple == null)
                    break;
                oneBlock.add(oneTuple);
            }
            if (oneBlock.size() != 0)
                ioCount++;
        }
        return oneBlock;
    }

    private Tuple getNextTuple() {
        try {
            String nextLine = this.readLine();
            if (nextLine == null || nextLine.trim().equals("")) {
                isFinished = true;
                return null;
            }
            return createTupleFromLine(nextLine);
        } catch (IOException e) {
            e.printStackTrace();
            isFinished = true;
            return null;
        }
    }

    public File getFile() { return file; }

    public boolean isFinished() { return isFinished; }

    public int getIOReadCount() { return ioCount; }


    /**
     * Extract info from a string to create a tuple
     * @param line read from input file
     * @return a tuple
     */
    private Tuple createTupleFromLine(String line) {
        try {
            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-mm-dd");
            int claimID = Integer.valueOf(line.substring(0, 8));
            Date claimDate = dateFormatter.parse(line.substring(8, 18));
            int clientID = Integer.valueOf(line.substring(18, 27));
            String clientName = line.substring(27, 52).trim();
            String clientAddress = line.substring(52, 202).trim();
            String clientEmail = line.substring(202, 230).trim();
            short itemID = Short.valueOf(line.substring(230, 232));
            double amountDamage = Double.valueOf(line.substring(232, 241));
            double amountPaid = Double.valueOf(line.substring(241, 250));
            return new Tuple(claimID, claimDate, clientID, clientName, clientAddress,
                    clientEmail, itemID, amountDamage, amountPaid);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
