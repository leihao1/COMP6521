import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class TupleWriter extends BufferedWriter {
    private int ioCount = 0;
    private boolean isNewBatch = true;

    public TupleWriter(File file) throws IOException {
        super(new FileWriter(file));
    }

    public void writeBatch(List<Tuple> oneBatch) {
        try {
            if (oneBatch != null) {
                if (oneBatch.size() % Utils.getTupleNumInOneBlock() == 0)
                    ioCount += Math.floorDiv(oneBatch.size(), Utils.getTupleNumInOneBlock());
                else
                    ioCount += Math.floorDiv(oneBatch.size(), Utils.getTupleNumInOneBlock()) + 1;
                for (Tuple tuple : oneBatch) {
                    if (isNewBatch) {
                        this.write(tuple.toString());
                        isNewBatch = false;
                    } else {
                        this.newLine();
                        this.write(tuple.toString());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public int getIOWriteCount() {
        return ioCount;
    }
}
