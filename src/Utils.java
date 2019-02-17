import java.io.File;
import java.util.List;

public class Utils {
    private static short tupleBytes = 250;
    private static short tupleNumInOneBlock = 15;

    /**
     * Get the total amount of system memory currently available
     * @return amount of available memory in bytes
     */
    public static long getFreeMemory() {
//      Ref: https://stackoverflow.com/questions/12807797/java-get-available-memory
//		long allocatedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
//		return Runtime.getRuntime().maxMemory() - allocatedMemory;

        return Runtime.getRuntime().freeMemory();
    }

    /**
     * Get the total amount of memory that JVM attempt to use
     * @return amount of memory in bytes
     */
    public static long getMaxMemory() {
        return Runtime.getRuntime().maxMemory();
    }

    public static long calcInputBufferCount() {
        int blockBytes = tupleBytes * tupleNumInOneBlock;
        long blockCount = getFreeMemory() / blockBytes;
        return (blockCount - 1);
    }

    /**
     * Prepare the tempFolder and the outputFolder
     */
    public static void prepareFolders(String outputPath) {
        File outputFolder = new File(outputPath);
        cleanFolder(outputFolder);
    }

    /**
     * Make sure a folder is created and empty
     * @param folder
     */
    public static void cleanFolder(File folder) {
        if (folder.exists()) {
            for (File file : folder.listFiles()) {
                if (file.isDirectory()) {
                    cleanFolder(file);
                } else {
                    file.delete();
                }
            }
        } else {
            folder.mkdir();
        }
    }

    public static short getTupleNumInOneBlock() {
        return tupleNumInOneBlock;
    }

    /**
     * QuickSort On List of Tuple Object , Sorting by Client ID
     * @param low  0
     * @param high pass size of list -1
     */
    public static void quickSort(List<Tuple> batch, int low, int high) {
        int i = low, j = high;
        Tuple pivot = batch.get(low + (high - low) / 2);
        while (i <= j) {
            while (batch.get(i).getClientID() < pivot.getClientID()) {
                i++;
            }
            while (batch.get(j).getClientID() > pivot.getClientID()) {
                j--;
            }

            if (i <= j) {
                exchange(batch, i, j);
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

    /**
     * Swap objects in list
     *
     * @param i
     * @param j
     */
    private static void exchange(List<Tuple> batch, int i, int j) {
        Tuple temp = (Tuple) batch.get(i);
        batch.set(i, batch.get(j));
        batch.set(j, temp);
    }
}
