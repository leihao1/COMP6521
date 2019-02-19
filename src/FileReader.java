import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileReader extends BufferedReader {

    public float preserveMemory;
    public File file;
    public short tupleNumInOneBlock = 15;
    public int ioCounter;
    public boolean finish;

    public FileReader(File file, float preserveMemPercentage) throws FileNotFoundException {
        super(new java.io.FileReader(file));
        this.preserveMemory = Runtime.getRuntime().maxMemory() * preserveMemPercentage;
        this.file = file;
    }

    public List<Tuple> getOneBlock() {
        List<Tuple> oneBlock = new ArrayList<>(tupleNumInOneBlock);
        if (Runtime.getRuntime().freeMemory() > preserveMemory) {
            for (int i = 0; i < tupleNumInOneBlock; i++) {
                Tuple oneTuple = getOneTuple();
                if (oneTuple == null)
                    break;
                oneBlock.add(oneTuple);
            }
            if (oneBlock.size() != 0)
                ioCounter++;
        }
        return oneBlock;
    }

    private Tuple getOneTuple() {
        try {
            String nextLine = this.readLine();
            if (nextLine == null || nextLine.trim().equals("")) {
                finish = true;
                return null;
            }
            return stringParser(nextLine);
        } catch (IOException e) {
            e.printStackTrace();
            finish = true;
            return null;
        }
    }

    //parse one line file string to one tuple object
    private Tuple stringParser(String line) {
        try {
            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-mm-dd");
            int claimID = Integer.valueOf(line.substring(0, 8));
            Date claimDate = dateFormatter.parse(line.substring(8, 18));
            int clientID = Integer.valueOf(line.substring(18, 27));
            String clientName = line.substring(27, 52).trim();
            String clientAddress = line.substring(52, 202).trim();
            String clientEmail = line.substring(202, 230).trim();
            byte itemID = Byte.valueOf(line.substring(230, 232));
            float amountDamage = Float.valueOf(line.substring(232, 241));
            double amountPaid = Double.valueOf(line.substring(241, 250));
            return new Tuple(claimID, claimDate, clientID, clientName, clientAddress,
                    clientEmail, itemID, amountDamage, amountPaid);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

}
