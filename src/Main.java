import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class Main {
    public static String inputFileName = "";
    public static String inputPath = "";
    public static String outputPath = "";

    public static float preserveMemory1 = 0.15f;
    public static float preserveMemory2 = 0.0f;
    public static float preserveMemory3 = 0.12f;

    public static byte tupleNumInOneBlock = 15;

    public static void main(String[] args) {
        File outputFolder = new File(outputPath);
        cleanFolder(outputFolder);

        long startTime = System.nanoTime();
        phaseOne();
        phaseTwo();
        phaseThree();
        System.out.printf("Total Time: %.2f(s) %n", ((System.nanoTime() - startTime) / 1000000000.0));

    }

    //clean output folder
    public static void cleanFolder(File outputFolder) {
        if (outputFolder.exists()) {
            for (File file : outputFolder.listFiles()) {
                if (file.isDirectory()) {
                    cleanFolder(file);
                } else {
                    file.delete();
                }
            }
        } else {
            outputFolder.mkdir();
        }
    }

    private static void phaseOne() {
        System.out.println("Phase One Start");

        long startTime1 = System.nanoTime();
        int diskReadCounter = 0;
        int diskWriteCounter =0;

        FileReader inputReader = null;
        FileWriter outputWriter = null;

        try {
            inputReader = new FileReader(new File(inputPath + inputFileName), preserveMemory1);

            short batchCounter = 0;
            long diskReadTimer = 0;
            long diskWriteTimer = 0;

            // Repeatedly fill the M buffers with new tuples form whole file
            while (!inputReader.finish) {
                System.gc();
                ArrayList<Tuple> oneBatch = new ArrayList<>();

                long startTime = System.nanoTime();

                //fill blocks in one batch until run out of memory
                while (true) {
                    List<Tuple> oneBlock = inputReader.getOneBlock();
                    //finish read or no left memory
                    if (oneBlock.isEmpty()) {
                        break;
                    }
                    oneBatch.addAll(oneBlock);
                }
                diskReadTimer += System.nanoTime() - startTime;
                // Sort the batch
                if (!oneBatch.isEmpty()) {
                    quickSort(oneBatch, 0, oneBatch.size() - 1);
                    // Dump the batch to a file
                    startTime = System.nanoTime();
                    batchCounter++;
                    outputWriter = new FileWriter(new File(String.format(outputPath + "%d.txt", batchCounter)));
                    outputWriter.writeOneBatch(oneBatch, tupleNumInOneBlock);
                    diskWriteCounter += outputWriter.ioCounter;
                    outputWriter.close();
                    diskWriteTimer += System.nanoTime() - startTime;
//                    System.out.printf("Sort batch %d finish, %d tuples tn this batch %n", batchCounter, oneBatch.size());
                }
            }
            System.out.printf("Phase One Finish: Batch# = %d, IO Read Time = %.2f(s), " +
                            "IO Write Time = %.2f(s) %n", batchCounter, diskReadTimer / 1000000000.0,
                    diskWriteTimer / 1000000000.0);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputReader != null) {
                diskReadCounter += inputReader.ioCounter;
                try {
                    inputReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputWriter != null) {
                try {
                    outputWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.printf("Total Time: %.2f(s) %n", ((System.nanoTime() - startTime1) / 1000000000.0));
        System.out.printf("Number Of I/O Read = %d, Number Of I/O Write = %d %n%n", diskReadCounter, diskWriteCounter);
    }

    private static void phaseTwo() {
        System.out.println("Phase Two Start");

        long startTime2 = System.nanoTime();
        int diskReadCounter = 0;
        int diskWriteCounter = 0;

        File outputFolder = new File(outputPath);
        short passesCount = 0;

        while (outputFolder.listFiles().length > 1) {
            ++passesCount;
//            mergeSortedFiles(outputFolder, ++passesCount);
            List<FileReader> inputReaders = null;
            FileWriter outputWriter = null;

            try {
                int numOfFileToMerge = Math.min(outputFolder.listFiles().length,80);//TODO:decide max file num
                inputReaders = new ArrayList<>(numOfFileToMerge);
                outputWriter = new FileWriter(new File(String.format(outputPath + "merged_%s.txt", passesCount)));

                List<List<Tuple>> inputBuffers = new ArrayList<>(numOfFileToMerge);
                List<Tuple> outputBuffer = new ArrayList<>(tupleNumInOneBlock);

                short fileCount = 0;

                //create K(< M-1) input buffers and one output buffer
                for (File tempFile : outputFolder.listFiles()) {
                    if (++fileCount > numOfFileToMerge)
                        break;
                    inputReaders.add(new FileReader(new File(tempFile.getAbsolutePath()), preserveMemory2));
                    inputBuffers.add(new ArrayList<>());
                }

                while (true) {
                    // set all buffer empty, wait to change
                    boolean allBufferEmpty = true;

                    // 1.fills in all input buffers with one block
                    for (int i = 0; i < numOfFileToMerge; i++) {
                        List<Tuple> oneBuffer = inputBuffers.get(i);
//                    System.out.println("one batch size"+oneBatch.size());
                        // finish merge this sublist, move to next
                        if (oneBuffer == null)
                            continue;
                        // one block of input buffer is empty, read next block
                        if (oneBuffer.isEmpty()) {
                            FileReader reader = inputReaders.get(i);
                            List<Tuple> oneBlock = reader.getOneBlock();
                            // all records in a sublist is finish merge, set to null to ignore
                            if (oneBlock.isEmpty() && reader.finish) {
                                inputBuffers.set(i, null);
                            } else {
                                allBufferEmpty = false;
                                oneBuffer.addAll(oneBlock);
//                            System.out.printf("batch %d %d %n",i,batches.get(i).size());
                            }
                        } else {
                            allBufferEmpty = false;
                        }
                    }

                    // all input buffer are empty, merge done
                    if (allBufferEmpty) {
                        // delete all temp files
                        for (int i = 0; i < numOfFileToMerge; i++) {
                            diskReadCounter += inputReaders.get(i).ioCounter;
                            inputReaders.get(i).close();
                            inputReaders.get(i).file.delete();
                        }
                        break;
                    }

                    // 2.keep merging until one buffer is empty
                    while (true) {
                        boolean emptyBuffer = false;
                        Tuple minClient = null;
                        short minClientIndex = -1;

                        // get local minimum among all input buffers
                        for (short i = 0; i < numOfFileToMerge; i++) {
                            List<Tuple> oneBuffer = inputBuffers.get(i);
                            // this sublist done, ignore and merge rest
                            if (oneBuffer == null)
                                continue;
                            // one buffer is empty, break to above code to fill
                            if (oneBuffer.isEmpty()) {
                                emptyBuffer = true;
                                break;
                            }
                            // get the first tuple in that buffer
                            Tuple firstTuple = oneBuffer.get(0);
                            if (minClient == null || minClient.clientID > firstTuple.clientID ) {
                                minClient = firstTuple;
                                minClientIndex = i;
                            }
                        }

                        // one buffer is empty, can not merge, go back to above code to fill
                        if (emptyBuffer)
                            break;

                        // no tuples in ant input buffers, write whatever left in output buffer
                        if (minClient == null) {
                            if (!outputBuffer.isEmpty()) {
                                outputWriter.writeOneBatch(outputBuffer, tupleNumInOneBlock);
                                outputBuffer.clear();
                            }
                            break;
                        }
                        // found one local minimum among first elements of each sublist, write to file
                        outputBuffer.add(minClient);
                        inputBuffers.get(minClientIndex).remove(0);
                        if (outputBuffer.size() == tupleNumInOneBlock) {
                            outputWriter.writeOneBatch(outputBuffer, tupleNumInOneBlock);
                            outputBuffer.clear();
                        }
                    }//end find minimum and merge
                }//end phase two

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
                    diskWriteCounter += outputWriter.ioCounter;
                    try { outputWriter.close(); }
                    catch (IOException e) { e.printStackTrace(); }
                }
            }
        }

        System.out.printf("Phase Two Finish: Total Time = %.2f(s) %n", ((System.nanoTime() - startTime2) / 1000000000.0));
        System.out.printf("Number Of IO Read = %d, Number Of IO Write = %d %n%n", diskReadCounter, diskWriteCounter);
    }

    private static void phaseThree() {
        System.out.println("Phase Three Start");

        long startTime3 = System.nanoTime();
        int diskReadCounter = 0;
        int diskWriteCounter = 0;

        File outputFolder = new File(outputPath);
        if (outputFolder.listFiles().length != 1) {
            System.err.println("More Than One Merged Files");
            return;
        }
        File file = outputFolder.listFiles()[0];
        FileReader inputReader = null;
        try {
            List<Tuple> readBuffer = new ArrayList<>();
            inputReader = new FileReader(file, preserveMemory3);
            HashMap<Integer, Double> top10 = new HashMap<>();
            Integer currentClientId = null;
            double currentClientPaid = 0.0;
            long diskReadTimer = 0;
            Integer tempMinClient = null;
            double tempMinPaid = Double.MAX_VALUE;
            int flag = 0;
            while (!inputReader.finish) {
                readBuffer.clear();
                long startTime = System.nanoTime();
                // read blocks util run out of memory
                while (true) {
                    List<Tuple> block = inputReader.getOneBlock();
                    if (block.isEmpty())
                        break;
                    readBuffer.addAll(block);
                }
                diskReadTimer += System.nanoTime() - startTime;
                // go through whole merged file and keep top10 clients
                for (Tuple tuple : readBuffer) {
                    Integer newClientId = tuple.clientID;
                    if (currentClientId == null) {
                        currentClientId = newClientId;
                        currentClientPaid = 0.0;
                    // different client, compare paid
                    } else if (currentClientId.intValue() != newClientId.intValue()) {
                        if (top10.size() < 10) {
                            top10.put(currentClientId, currentClientPaid);
                        } else {
                            if(flag == 0) {
                                Integer minClientID = null;
                                double minPaid = Double.MAX_VALUE;
                                // get local minimum clientID in top10
                                for (HashMap.Entry<Integer, Double> entry : top10.entrySet()) {
                                    if (minPaid > entry.getValue().doubleValue()) {
                                        minClientID = entry.getKey();
                                        minPaid = entry.getValue().doubleValue();
                                    }
                                }
                                tempMinClient = minClientID;
                                tempMinPaid = minPaid;
                                flag = 1;
                            }
                            // add new top10 member
                            if (tempMinPaid < currentClientPaid) {
                                top10.remove(tempMinClient);
                                top10.put(currentClientId, currentClientPaid);
                                Integer minClientID = null;
                                double minPaid = Double.MAX_VALUE;
                                // get local minimum clientID in top10
                                for (HashMap.Entry<Integer, Double> entry : top10.entrySet()) {
                                    if (minPaid > entry.getValue().doubleValue()) {
                                        minClientID = entry.getKey();
                                        minPaid = entry.getValue().doubleValue();
                                    }
                                }
                                tempMinClient = minClientID;
                                tempMinPaid = minPaid;
                            }
                        }
                        currentClientId = newClientId;
                        currentClientPaid = 0.0;
                    }
                    // sum all paid of same client
                    currentClientPaid += tuple.amountPaid;
                }
            }

            System.out.println("Top 10 Costly Clients: ");

            LinkedHashMap<Integer, Double> orderedTop10 = new LinkedHashMap<>();
            while(orderedTop10.size() < 10) {
                int maxClientID = 0;
                double maxPaid = Double.MIN_VALUE;
                for (HashMap.Entry<Integer, Double> entry : top10.entrySet()) {
                    if (maxPaid < entry.getValue()) {
                        maxClientID = entry.getKey();
                        maxPaid = entry.getValue();
                    }
                }
                top10.remove(maxClientID);
                orderedTop10.put(maxClientID, maxPaid);
            }

            for(Map.Entry<Integer, Double> entry : orderedTop10.entrySet()) {
                System.out.println("ClientID: " + entry.getKey() + ", Total Compensation: " + entry.getValue());
            }
            System.out.printf("Phase Three Finish: IO Read Time = %.2f(s), Total Time = %.2f(s) %n",
                    diskReadTimer / 1000000000.0, ((System.nanoTime() - startTime3) / 1000000000.0));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (inputReader != null) {
                diskReadCounter += inputReader.ioCounter;
                try { inputReader.close(); }
                catch (IOException e) { e.printStackTrace(); }
            }
        }
        System.out.printf("Number Of IO Read = %d, Number Of IO Write = %d %n%n", diskReadCounter, diskWriteCounter);
    }

    // quick sort base on clientID
    public static void quickSort(List<Tuple> batch, int low, int high) {
        int i = low, j = high;
        Tuple pivot = batch.get(low + (high - low) / 2);
        while (i <= j) {

            while (batch.get(i).clientID < pivot.clientID) {
                i++;
            }
            while (batch.get(j).clientID > pivot.clientID) {
                j--;
            }
            if (i <= j) {
                Tuple temp = batch.get(i);
                batch.set(i, batch.get(j));
                batch.set(j, temp);
                i++;
                j--;
            }

        }
        if (low < j) {
            quickSort(batch, low, j);
        }
        if (i < high) {
            quickSort(batch, i, high);
        }
    }

}