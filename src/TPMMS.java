import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class TPMMS {
    private static int ioRead = 0;
    private static int ioWrite = 0;
    private static String inputPath = "F:\\books\\COMP6521\\lab\\lab1\\src\\input\\";
    private static String outputPath = "F:\\books\\COMP6521\\lab\\lab1\\src\\output\\";
    private static String inputFileName = "105000.txt";
    private static float preserveMemPercentageP1 = 0.15f;
    private static float preserveMemPercentageP2 = 0.0f;
    private static float preserveMemPercentageP3 = 0.1f;
    private static short maxFileToMerge = 80;

    public static void main(String[] args) {
        Utils.prepareFolders(outputPath);

        ioRead = 0;
        ioWrite = 0;
        long startTime1 = System.nanoTime();
        phaseOne();
        System.out.printf("Phase 1 time: %.2f(s) %n", ((System.nanoTime() - startTime1) / 1000000000.0));
        System.out.printf("IO_Read = %d, IO_Write = %d %n%n", ioRead, ioWrite);

        ioRead = 0;
        ioWrite = 0;
        long startTime2 = System.nanoTime();
        phaseTwo();
        System.out.printf("Phase 2 time: %.2f(s) %n", ((System.nanoTime() - startTime2) / 1000000000.0));
        System.out.printf("IO_Read = %d, IO_Write = %d %n%n", ioRead, ioWrite);

        ioRead = 0;
        ioWrite = 0;
        long startTime3 = System.nanoTime();
        getTop10CostlyClients();
        System.out.printf("Phase 3 time: %.2f(s) %n", ((System.nanoTime() - startTime3) / 1000000000.0));
        System.out.printf("IO_Read = %d, IO_Write = %d %n%n", ioRead, ioWrite);
        System.out.printf("Total time: %.2f(s) %n", ((System.nanoTime() - startTime1) / 1000000000.0));
    }

    private static void phaseOne() {
        System.out.println("Phase One Start");
        TupleReader inputReader = null;
        TupleWriter outputWriter = null;
        try {
            // Initializing reader
            inputReader = new TupleReader(new File(inputPath + inputFileName), preserveMemPercentageP1); // TODO: tweak this number

            // Keep requesting new blocks until running out of memory
            // The reader is responsible for monitoring the memory
            short batchCounter = 0;
            long totalReadTime = 0;
            long totalSortTime = 0;
            long totalWriteTime = 0;
            while (!inputReader.isFinished()) {
                System.gc();
                ArrayList<Tuple> oneBatch = new ArrayList<>();

                long startTime = System.nanoTime();
                while (true) {
                    List<Tuple> oneBlock = inputReader.getNextBlock();
                    if (oneBlock.isEmpty()) { // Not enough memory or done reading input
                        break;
                    }
                    oneBatch.addAll(oneBlock);
                }
                totalReadTime += System.nanoTime() - startTime;
                if (!oneBatch.isEmpty()) {
                    // Sort the batch
                    startTime = System.nanoTime();
                    Utils.quickSort(oneBatch, 0, oneBatch.size() - 1);
                    totalSortTime += System.nanoTime() - startTime;

                    // Dump the batch to a file
                    startTime = System.nanoTime();
                    batchCounter++;
                    outputWriter = new TupleWriter(new File(String.format(outputPath + "%d.txt", batchCounter)));
                    outputWriter.writeBatch(oneBatch);
                    ioWrite += outputWriter.getIOWriteCount();
                    outputWriter.close();
                    totalWriteTime += System.nanoTime() - startTime;
                    System.out.printf("Sort batch %d finish, %d tuples tn this batch %n", batchCounter, oneBatch.size());
                }
            }
            System.out.printf("Phase 1 Finish: #Batch = %d, totalReadTime = %ds, totalSortTime = %ds, " +
                    "totalWriteTime = %ds %n", batchCounter, totalReadTime / 1000000000,
                    totalSortTime / 1000000000, totalWriteTime / 1000000000);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputReader != null) {
                ioRead += inputReader.getIOReadCount();
                try { inputReader.close(); }
                catch (IOException e) { e.printStackTrace(); }
            }
            if (outputWriter != null) {
                try { outputWriter.close(); }
                catch (IOException e) { e.printStackTrace(); }
            }
        }
    }

    private static void phaseTwo() {
        System.out.println("Phase Two Start");
        File outputFolder = new File(outputPath);
        short passesCount = 0;

        while (outputFolder.listFiles().length > 1) {
            mergeSortedFiles(outputFolder, ++passesCount);
        }
    }

    private static void mergeSortedFiles(File outputFolder, short passesCount) {
        List<TupleReader> inputReaders = null;
        TupleWriter outputWriter = null;
        try {
            int numOfFileToMerge = Math.min(outputFolder.listFiles().length, maxFileToMerge);//TODO:
            List<List<Tuple>> inputBuffers = new ArrayList<>(numOfFileToMerge);
            inputReaders = new ArrayList<>(numOfFileToMerge);
            short fileCount = 0;
            for (File tempFile : outputFolder.listFiles()) {
                if (++fileCount > numOfFileToMerge)
                    break;

                inputBuffers.add(new ArrayList<>());
                inputReaders.add(new TupleReader(new File(tempFile.getAbsolutePath()), preserveMemPercentageP2));
            }
            outputWriter = new TupleWriter(new File(String.format(outputPath + "%s_merged.txt", passesCount)));
            List<Tuple> outputBuffer = new ArrayList<>(Utils.getTupleNumInOneBlock());

            while (true) {
                // Make sure no batch is empty
                boolean allEmpty = true;
                for (int i = 0; i < numOfFileToMerge; i++) { //fills in all input buffers with one block
                    List<Tuple> oneBuffer = inputBuffers.get(i);
//                    System.out.println("one batch size"+oneBatch.size());
                    if (oneBuffer == null) // The batch is done, hence ignored
                        continue;
                    if (oneBuffer.isEmpty()) {
                        TupleReader reader = inputReaders.get(i);
                        List<Tuple> block = reader.getNextBlock();

                        if (oneBuffer.isEmpty() && block.isEmpty() && reader.isFinished()) {
                            inputBuffers.set(i, null);
                        } else {
                            allEmpty = false;
                            oneBuffer.addAll(block);
//                            System.out.printf("batch %d %d %n",i,batches.get(i).size());
                        }
                    } else {
                        allEmpty = false;
                    }
                }

                if (allEmpty) { // Done merging
                    // Delete all temp files
                    for (int i = 0; i < numOfFileToMerge; i++) {
                        ioRead += inputReaders.get(i).getIOReadCount();
                        inputReaders.get(i).close();
                        inputReaders.get(i).getFile().delete();
                    }
                    break;
                }

                while (true) { // Keep merging until 1 batch is empty or all batches are empty
                    boolean emptyBuffer = false;
                    Tuple smallestID = null;
                    short smallestIndex = -1;
                    for (short i = 0; i < numOfFileToMerge; i++) { //get local minimum among all input buffers
                        List<Tuple> oneBuffer = inputBuffers.get(i);
                        if (oneBuffer == null)
                            continue;
                        if (oneBuffer.isEmpty()) {
                            emptyBuffer = true;
                            break;
                        }

                        Tuple firstTuple = oneBuffer.get(0); // Get the first tuple
                        if (smallestID == null || smallestID.compareTo(firstTuple) > 0) {
                            smallestID = firstTuple;
                            smallestIndex = i;
                        }
                    }

                    if (emptyBuffer) // If one batch is empty, read a new block to fill it
                        break;

                    if (smallestID == null) { // When all done
                        if (!outputBuffer.isEmpty()) {
                            outputWriter.writeBatch(outputBuffer);
                            outputBuffer.clear();
                        }
                        break;
                    }

                    outputBuffer.add(smallestID);
                    inputBuffers.get(smallestIndex).remove(0);
                    if (outputBuffer.size() == Utils.getTupleNumInOneBlock()) {
                        outputWriter.writeBatch(outputBuffer);
                        outputBuffer.clear();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        } finally {
            if (inputReaders != null) {
                inputReaders.forEach(reader -> {
                    try { reader.close(); }
                    catch (IOException e) { e.printStackTrace(); }
                });
            }
            if (outputWriter != null) {
                ioWrite += outputWriter.getIOWriteCount();
                try { outputWriter.close(); }
                catch (IOException e) { e.printStackTrace(); }
            }
        }
    }

    private static void getTop10CostlyClients() {
        System.out.println("Phase Three Start");
        File outputFolder = new File(outputPath);
        if (outputFolder.listFiles().length != 1) {
            System.err.println("There are more than one merged file");
            return;
        }
        File file = outputFolder.listFiles()[0];
        TupleReader reader = null;
        try {
            List<Tuple> buffer = new ArrayList<>();
            reader = new TupleReader(file, preserveMemPercentageP3);
            Map<Integer, Double> top10 = new HashMap<>();
            Integer currentClientId = null;
            double currentClientPaid = 0.0;
            while (!reader.isFinished()) {
                buffer.clear();
                while (true) {
                    List<Tuple> block = reader.getNextBlock();
                    if (block.isEmpty()) // Not enough memory or done reading input
                        break;
                    buffer.addAll(block);
                }
                for (Tuple tuple : buffer) {
                    Integer newClientId = tuple.getClientID();
                    if (currentClientId == null) { // First iteration
                        currentClientId = newClientId;
                        currentClientPaid = 0.0;
                    } else if (currentClientId.intValue() != newClientId.intValue()) { // Sum up old client, start calculating new client
                        if (top10.size() < 10) {
                            top10.put(currentClientId, currentClientPaid);
                        } else {
                            Integer smallestClientId = null;
                            double smallestAmount = Double.MAX_VALUE;
                            for (Entry<Integer, Double> entry : top10.entrySet()) {
                                if (smallestAmount > entry.getValue().doubleValue()) {
                                    smallestClientId = entry.getKey();
                                    smallestAmount = entry.getValue().doubleValue();
                                }
                            }
                            if (smallestAmount < currentClientPaid) {
                                top10.remove(smallestClientId);
                                top10.put(currentClientId, currentClientPaid);
                            }
                        }
                        currentClientId = newClientId;
                        currentClientPaid = 0.0;
                    }
                    // Adding the paid amount
                    currentClientPaid += tuple.getAmountPaid();
                }
            }

            System.out.println("Top 10 costly clients: ");
            for (Entry<Integer, Double> entry : top10.entrySet()) {
                System.out.println("ClientID: " + entry.getKey() + ", Total Compensation: "+ entry.getValue());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                ioRead += reader.getIOReadCount();
                try { reader.close(); }
                catch (IOException e) { e.printStackTrace(); }
            }
        }
    }
}
