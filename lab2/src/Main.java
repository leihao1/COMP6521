//import com.javamex.classmexer.MemoryUtil;
//-javaagent:"E:\download\classmexer-0_03\classmexer.jar"
import java.io.*;
import java.util.*;

public class Main {
    private static  boolean inDisk = false;
    private static String fileName = "input\\LA2.txt";
    private static String filePath = System.getProperty("user.dir")+"\\lab2\\";
//    private static HashMap<Integer,HashMap<Integer,HashMap<Integer,ArrayList<String>>>> index = new HashMap<>();
    private static HashMap<Integer,HashMap<Integer,HashMap<Integer,StringBuilder>>> index = new HashMap<>();
    private static ArrayList<Float> testx = new ArrayList<>();
    private static ArrayList<Float> testy = new ArrayList<>();
    private static ArrayList<Float> testz = new ArrayList<>();

    public static void main(String[] args){
        System.out.println("Max mem "+Runtime.getRuntime().maxMemory()/1000000);
        System.out.println("Free mem "+Runtime.getRuntime().freeMemory()/1000000);

        buildIndex();
//        System.out.println("Memory used for index : "+MemoryUtil.deepMemoryUsageOf(index)/1000000.0);
        while (true){
            System.out.println(" ");
            System.out.println("Select A Query Type And Press Enter :");
            System.out.println("1: Range Query");
            System.out.println("2: Nearest Neighbor Query");
            Scanner in = new Scanner(System.in);
            int type;
            try {
                 type = in.nextInt();
            }catch (Exception e){
                System.err.println("Invalid option!");
                continue;
            }
            switch (type){
                case 1:
                    String[][] rq = new String[3][2];
                    while(true) {
                        System.out.println(" ");
                        System.out.println("Range Query...");
                        System.out.println("Please enter lower and upper bounds for x y z (separate by space) :");
                        System.out.println("[Press \"Enter\" To Return Main Menu]");
                        String[] query;
                        try {
                            Scanner in2 = new Scanner(System.in);
                            query = in2.nextLine().split(" ");
                            rq[0][0] = query[0];
                            rq[0][1] = query[1];
                            rq[1][0] = query[2];
                            rq[1][1] = query[3];
                            rq[2][0] = query[4];
                            rq[2][1] = query[5];
                        } catch (Exception e) {
                            System.err.println("Wrong range query!");
                            break;
                        }

                        ArrayList<String> result = rangeQuery(rq);

                        if(result.size() >0) {
                            System.out.println("Query Done. Save Result?(y/n)");
                            Scanner in3 = new Scanner(System.in);
                            String save = in3.nextLine();
                            if(save.equalsIgnoreCase("Y")){
                                saveQueryResult(result, query);
                            }
                        }
                    }
                    break;
                case 2:
                    String[] nnq = new String[3];
                    while (true){
                        System.out.println(" ");
                        System.out.println("K-NN Query...");
                        System.out.println("Enter Target Point x y z Coordinates (separate by space) :");
                        System.out.println("[Press \"Enter\" To Return Main Menu]");
                        String[] query;
                        try {
                            Scanner in2 = new Scanner(System.in);
                            query = in2.nextLine().split(" ");
                            nnq[0] = query[0];
                            nnq[1] = query[1];
                            nnq[2] = query[2];
                        } catch (Exception e) {
                            System.err.println("Wrong knn query!");
                            break;
                        }

                        ArrayList<String> result = nearestNeighborQuery(nnq);

                        if(result.size() >0) {
                            System.out.println("Query Done. Save Result?(y/n)");
                            Scanner in3 = new Scanner(System.in);
                            String save = in3.nextLine();
                            if(save.equalsIgnoreCase("Y")){
                                saveQueryResult(result, query);
                            }
                        }
                    }

            }

        }
    }
/*
    private static void buildIndex(){
        System.out.println("Start Building Index ...");
        long start  = System.currentTimeMillis();
        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath+fileName));
//            RandomAccessFile ra = new RandomAccessFile(filePath+fileName,"r");
            String line = null;
            int pointer = 0;
            try {
                line = br.readLine();
//                pointer = ra.getFilePointer();
//                line = ra.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            int i =0;
            int j = 0;
            while(line !=null){
//                if(i%1000000 == 0){
//                    System.out.println("Finish tuples: "+i);
//                }

                //twice faster than using split()
                String x = line.substring(1,9);
                String y = line.substring(11,19);
                String z = line.substring(21,29);
                int xKey = (int)Math.ceil(Double.valueOf(x)/CONSTANT.BUCKET_SIZE);
                int yKey = (int)Math.ceil(Double.valueOf(y)/CONSTANT.BUCKET_SIZE);
                int zKey = (int)Math.ceil(Double.valueOf(z)/CONSTANT.BUCKET_SIZE);

                if(!inDisk) {
                    if (!index.containsKey(xKey)) {
                        HashMap<Integer, HashMap<Integer, ArrayList<String>>> hy = new HashMap<>();
                        HashMap<Integer, ArrayList<String>> hz = new HashMap<>();
                        ArrayList<String> record = new ArrayList<>();
                        record.add(line);
                        j++;
                        hz.put(zKey, record);
                        hy.put(yKey, hz);
                        index.put(xKey, hy);
                    } else {
                        if (!index.get(xKey).containsKey(yKey)) {
                            HashMap<Integer, ArrayList<String>> hz = new HashMap<>();
                            ArrayList<String> record = new ArrayList<>();
                            record.add(line);
                            j++;
                            hz.put(zKey, record);
                            index.get(xKey).put(yKey, hz);
                        } else {
                            if (!index.get(xKey).get(yKey).containsKey(zKey)) {
                                ArrayList<String> record = new ArrayList<>();
                                record.add(line);
                                j++;
                                index.get(xKey).get(yKey).put(zKey, record);
                            } else {
                                index.get(xKey).get(yKey).get(zKey).add(line);
                                j++;
                            }
                        }
                    }
                }

                try {
                    line = br.readLine();
//                    pointer = ra.getFilePointer();
//                    line = ra.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                i++;
                pointer += 31;
            }//last tuple
            System.out.println("Index build finished. Time : "+(System.currentTimeMillis()-start)/1000.0+" (s)");
            System.out.println("Scan tuples i :" +i);
            System.out.println("Add records j :"+j);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static ArrayList<String> rangeQuery(String[][] query){
        assert (query.length ==3 && query[0].length == 2 ) : "Wrong query format!";
        System.out.println("Start Range Query ...");
        long start = System.currentTimeMillis();

        ArrayList<String> result = new ArrayList<>();
        double XL = Double.valueOf(query[0][0]);
        double XH = Double.valueOf(query[0][1]);
        double YL = Double.valueOf(query[1][0]);
        double YH = Double.valueOf(query[1][1]);
        double ZL = Double.valueOf(query[2][0]);
        double ZH = Double.valueOf(query[2][1]);
        int xLow, xHigh, yLow, yHigh, zLow, zHigh;
        xLow = (int) Math.ceil(XL / CONSTANT.BUCKET_SIZE);
        xHigh = (int) Math.ceil(XH / CONSTANT.BUCKET_SIZE);
        yLow = (int) Math.ceil(YL / CONSTANT.BUCKET_SIZE);
        yHigh = (int) Math.ceil(YH / CONSTANT.BUCKET_SIZE);
        zLow = (int) Math.ceil(ZL / CONSTANT.BUCKET_SIZE);
        zHigh = (int) Math.ceil(ZH / CONSTANT.BUCKET_SIZE);

        boolean wholeFile = false;
        if(XL==0 && XH==1000 && YL==0 && YH==1000 && ZL==0 && ZH==1000){
            wholeFile = true;
    //            System.out.println("whole ");
        }

        for (int i = xLow; i <= xHigh; i++) {
            if (index.containsKey(i)) {
                for (int j = yLow; j <= yHigh; j++) {
                    if (index.get(i).containsKey(j)) {
                        for (int k = zLow; k <= zHigh; k++) {
                            if (index.get(i).get(j).containsKey(k)) {
                                result.addAll(index.get(i).get(j).get(k));
                            }
                        }
                    }
                }
            }
        }

        ArrayList<String> newResult = new ArrayList<>();
        //check range
        if(!wholeFile) {
            for (String line : result) {
                double x = Double.parseDouble(line.substring(1, 9));
                double y = Double.parseDouble(line.substring(11, 19));
                double z = Double.parseDouble(line.substring(21, 29));
                if (x >= XL && x <= XH && y >= YL && y <= YH && z >= ZL && z <= ZH) {
                    newResult.add(line);
                }
            }
        }else{
            newResult.addAll(result);
        }
        System.out.println("Range Query Finished. Time: "+(System.currentTimeMillis()-start)/1000.0+" (s)");
        System.out.println("Range Query Point Number : "+newResult.size());
        return newResult;

}
*/
    private static void buildIndex(){
        System.out.println("Start Building Index ...");
        long start  = System.currentTimeMillis();
        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath+fileName));
//            RandomAccessFile ra = new RandomAccessFile(filePath+fileName,"r");
            String line = null;
            int pointer = 0;
            try {
                line = br.readLine();
//                pointer = ra.getFilePointer();
//                line = ra.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            int i =0;
            int j = 0;
            while(line !=null){
//                if(i%1000000 == 0){
//                    System.out.println("Finish tuples: "+i);
//                }

                //twice faster than using split()
                String x = line.substring(1,9);
                String y = line.substring(11,19);
                String z = line.substring(21,29);
                int xKey = (int)Math.ceil(Double.valueOf(x)/CONSTANT.BUCKET_SIZE);
                int yKey = (int)Math.ceil(Double.valueOf(y)/CONSTANT.BUCKET_SIZE);
                int zKey = (int)Math.ceil(Double.valueOf(z)/CONSTANT.BUCKET_SIZE);

                if(!inDisk) {
                    if (!index.containsKey(xKey)) {
                        HashMap<Integer, HashMap<Integer, StringBuilder>> hy = new HashMap<>();
                        HashMap<Integer, StringBuilder> hz = new HashMap<>();
                        j++;
                        hz.put(zKey, new StringBuilder("("+x+","+y+","+z+")"));
                        hy.put(yKey, hz);
                        index.put(xKey, hy);
                    } else {
                        if (!index.get(xKey).containsKey(yKey)) {
                            HashMap<Integer, StringBuilder> hz = new HashMap<>();
                            j++;
                            hz.put(zKey, new StringBuilder("("+x+","+y+","+z+")"));
                            index.get(xKey).put(yKey, hz);
                        } else {
                            if (!index.get(xKey).get(yKey).containsKey(zKey)) {
                                j++;
                                index.get(xKey).get(yKey).put(zKey, new StringBuilder("("+x+","+y+","+z+")"));
                            } else {
                                index.get(xKey).get(yKey).get(zKey).append("("+x+","+y+","+z+")");
                                j++;
                            }
                        }
                    }
                }

                try {
                    line = br.readLine();
//                    pointer = ra.getFilePointer();
//                    line = ra.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                i++;
                pointer += 31;
            }//last tuple
            System.out.println("Index build finished. Time : "+(System.currentTimeMillis()-start)/1000.0+" (s)");
            System.out.println("Scan tuples i :" +i);
            System.out.println("Add records j :"+j);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static ArrayList<String> rangeQuery(String[][] query){
        assert (query.length ==3 && query[0].length == 2 ) : "Wrong query format!";
        System.out.println("Start Range Query ...");
        long start = System.currentTimeMillis();

        ArrayList<StringBuilder> result = new ArrayList<>();
        double XL = Double.valueOf(query[0][0]);
        double XH = Double.valueOf(query[0][1]);
        double YL = Double.valueOf(query[1][0]);
        double YH = Double.valueOf(query[1][1]);
        double ZL = Double.valueOf(query[2][0]);
        double ZH = Double.valueOf(query[2][1]);
        int xLow, xHigh, yLow, yHigh, zLow, zHigh;
        xLow = (int) Math.ceil(XL / CONSTANT.BUCKET_SIZE);
        xHigh = (int) Math.ceil(XH / CONSTANT.BUCKET_SIZE);
        yLow = (int) Math.ceil(YL / CONSTANT.BUCKET_SIZE);
        yHigh = (int) Math.ceil(YH / CONSTANT.BUCKET_SIZE);
        zLow = (int) Math.ceil(ZL / CONSTANT.BUCKET_SIZE);
        zHigh = (int) Math.ceil(ZH / CONSTANT.BUCKET_SIZE);

        boolean wholeFile = false;
        if(XL==0 && XH==1000 && YL==0 && YH==1000 && ZL==0 && ZH==1000){
            wholeFile = true;
//            System.out.println("whole ");
        }

        for (int i = xLow; i <= xHigh; i++) {
            if (index.containsKey(i)) {
                for (int j = yLow; j <= yHigh; j++) {
                    if (index.get(i).containsKey(j)) {
                        for (int k = zLow; k <= zHigh; k++) {
                            if (index.get(i).get(j).containsKey(k)) {
                                result.add(index.get(i).get(j).get(k));
                            }
                        }
                    }
                }
            }
        }

        ArrayList<String> newResult = new ArrayList<>();
        //check range
        if(!wholeFile) {
            for (StringBuilder sb : result) {
                int pointer =0;
                while(pointer<= sb.length()-28){
                    double x = Double.parseDouble(sb.toString().substring(pointer+1, pointer+9));
                    double y = Double.parseDouble(sb.toString().substring(pointer+10, pointer+18));
                    double z = Double.parseDouble(sb.toString().substring(pointer+19, pointer+27));
                    pointer += 28;
                    if (x >= XL && x <= XH && y >= YL && y <= YH && z >= ZL && z <= ZH) {
                        newResult.add("("+x+","+y+","+z+")");
                    }

                }
            }
        }else{
            for(StringBuilder sb :result){
                newResult.add(sb.toString());
            }
        }
        System.out.println("Range Query Finished. Time: "+(System.currentTimeMillis()-start)/1000.0+" (s)");
        System.out.println("Range Query Point Number : "+newResult.size());
        return newResult;

    }

    private static ArrayList<String> nearestNeighborQuery(String[] nnq){
        assert (nnq.length == 3 ) : "Wrong query format in KNN query ";
        System.out.println("Start KNN Query ...");
        long start = System.currentTimeMillis();

        int x = (int)Math.ceil(Double.valueOf(nnq[0])/CONSTANT.BUCKET_SIZE);
        int y = (int)Math.ceil(Double.valueOf(nnq[1])/CONSTANT.BUCKET_SIZE);
        int z = (int)Math.ceil(Double.valueOf(nnq[2])/CONSTANT.BUCKET_SIZE);
        int[][] startBucket = {{x,x},{y,y},{z,z}};
        ArrayList<String> foundPoints = new ArrayList<>();

        int[][] nextBucket = getNextBuckets(startBucket,foundPoints);//method call

        ArrayList<String> neighbors = new ArrayList<>();

        double distance = calculateMinDistance(nnq,foundPoints,neighbors);//method call

        //smallest bucket num to target point , use to calculate possible closer points
        int minBucketNum;
        minBucketNum = Math.min(Math.min(nextBucket[0][1]-startBucket[0][1],nextBucket[1][1]-startBucket[1][1]),
                nextBucket[2][1]-startBucket[2][1]);

        double newDistance = checkPossibleCloserPoint(distance,minBucketNum,nextBucket,nnq,neighbors);//method call

        if(newDistance < distance) {
            distance = newDistance;
        }
        System.out.println("NN Query Finished. Time : "+(System.currentTimeMillis()-start)/1000.0+" (s)");
        System.out.println("Nearest Neighbors Number : "+neighbors.size());
        System.out.println("Min Distance : "+distance);
        return neighbors;
    }

    private static int[][] getNextBuckets(int[][] buckets, ArrayList<String> foundPoints) {
        System.out.println("Get next bucket...");
        int[][] nextBucket = new int[3][2];
        int xLow = buckets[0][0];
        int xHigh = buckets[0][1];
        int yLow = buckets[1][0];
        int yHigh = buckets[1][1];
        int zLow = buckets[2][0];
        int zHigh = buckets[2][1];

        //find non-empty x bucket
        while(!index.containsKey(xLow) && !index.containsKey(xHigh)){
            xLow --;
            xLow = Math.max(0,xLow);
            xHigh ++;
            xHigh = Math.min(xHigh,CONSTANT.BUCKET_NUM);
        }

        //find non-empty y bucket
        int min1 = yLow;
        int max1 = yHigh;
        while(!index.get(xLow).containsKey(min1) && !index.get(xLow).containsKey(max1)){
            if(min1 == 0 && max1 == CONSTANT.BUCKET_NUM)
                break;
            min1 --;
            min1 = Math.max(0,min1);
            max1 ++;
            max1 = Math.min(max1,CONSTANT.BUCKET_NUM);
        }
        int min2 = yLow;
        int max2 = yHigh;
        while(!index.get(xHigh).containsKey(min2) && !index.get(xHigh).containsKey(max2)){
            if(min2 ==0 && max2 == CONSTANT.BUCKET_NUM)
                break;
            min2 --;
            min2 = Math.max(0,min2);
            max2 ++;
            max2 = Math.min(max2,CONSTANT.BUCKET_NUM);
        }
        //minimize bucket num to search later
        yLow = Math.max(min1,min2);
        yHigh = Math.min(max1,max2);

        //find non-empty z bucket
        int min3 = zLow;
        int min4 = zLow;
        int min5 = zLow;
        int min6 = zLow;
        int max3 = zHigh;
        int max4 = zHigh;
        int max5 = zHigh;
        int max6 = zHigh;
        while (!index.get(xLow).get(yLow).containsKey(min3) && !index.get(xLow).get(yLow).containsKey(max3)){
            if(min3 == 0 && max3 == CONSTANT.BUCKET_NUM)
                break;
            min3 --;
            min3 = Math.max(0,min3);
            max3 ++;
            max3 = Math.min(max3,CONSTANT.BUCKET_NUM);
        }
        while (!index.get(xLow).get(yHigh).containsKey(min4) && !index.get(xLow).get(yHigh).containsKey(max4)){
            if(min4 == 0 && max4 == CONSTANT.BUCKET_NUM)
                break;
            min4 --;
            min4 = Math.max(0,min4);
            max4 ++;
            max4 = Math.min(max4,CONSTANT.BUCKET_NUM);
        }
        while (!index.get(xHigh).get(yLow).containsKey(min5) && !index.get(xHigh).get(yLow).containsKey(max5)){
            if(min5 == 0 && max5 == CONSTANT.BUCKET_NUM)
                break;
            min5 --;
            min5 = Math.max(0,min5);
            max5 ++;
            max5 = Math.min(max5,CONSTANT.BUCKET_NUM);
        }
        while (!index.get(xHigh).get(yHigh).containsKey(min6) && !index.get(xHigh).get(yHigh).containsKey(max6)){
            if(min6 == 0 && max6 == CONSTANT.BUCKET_NUM)
                break;
            min6 --;
            min6 = Math.max(0,min6);
            max6 ++;
            max6 = Math.min(max6,CONSTANT.BUCKET_NUM);
        }
        zLow = Math.max(Math.max(min5,min6),Math.max(min3,min4));
        zHigh = Math.min(Math.min(max3,max4),Math.min(max5,max6));

        double rq_xLow = (xLow-1) * CONSTANT.BUCKET_SIZE + 0.000001;
        double rq_yLow = (yLow-1) * CONSTANT.BUCKET_SIZE + 0.000001;
        double rq_zLow = (zLow-1) * CONSTANT.BUCKET_SIZE + 0.000001;
        double rq_xHigh = xHigh * CONSTANT.BUCKET_SIZE;
        double rq_yHigh = yHigh * CONSTANT.BUCKET_SIZE;
        double rq_zHigh = zHigh * CONSTANT.BUCKET_SIZE;
        String[][] newQuery = {{String.valueOf(rq_xLow),String.valueOf(rq_xHigh)},
                {String.valueOf(rq_yLow),String.valueOf(rq_yHigh)},
                {String.valueOf(rq_zLow),String.valueOf(rq_zHigh)}};


        foundPoints.addAll(rangeQuery(newQuery));//method call rangeQuery()

        //next bucket range to found points from
        nextBucket[0][0] = xLow-2;
        nextBucket[0][1] = xHigh+2;
        nextBucket[1][0] = yLow-2;
        nextBucket[1][1] = yHigh+2;
        nextBucket[2][0] = zLow-2;
        nextBucket[2][1] = zHigh+2;

        return nextBucket;
    }

    private static double calculateMinDistance(String[] nnq, ArrayList<String> foundPoints,ArrayList<String> neighbors) {
        System.out.println("Calculating min distance...");
        HashMap<Double,ArrayList<String>> candidates = new HashMap<>();
        double distance = Double.MAX_VALUE;
        candidates.put(distance,new ArrayList<>());
        double x = Double.valueOf(nnq[0]);
        double y = Double.valueOf(nnq[1]);
        double z = Double.valueOf(nnq[2]);
        for(String line : foundPoints){
            double x_diff = Double.valueOf(line.substring(1,9)) - x;
            double y_diff = Double.valueOf(line.substring(11,19)) - y;
            double z_diff = Double.valueOf(line.substring(21,29)) - z;
            double temp = Math.sqrt((x_diff*x_diff + y_diff*y_diff + z_diff*z_diff));
            if(temp == 0){
                if(candidates.containsKey(temp))
                    candidates.get(temp).add(line);
                else {
                    ArrayList<String> single = new ArrayList<>();
                    single.add(line);
                    candidates.put(temp,single);
                }
            }else{
                if(temp < distance){
                    candidates.remove(distance);
                    ArrayList<String> t = new ArrayList<>();
                    t.add(line);
                    candidates.put(temp,t);
                    distance = temp;
                }else if(temp == distance) {
                    candidates.get(temp).add(line);
                }

            }
        }
        double test = 0.0;
        if(candidates.containsKey(test) && candidates.get(test).size() >1 )
            System.err.println("duplicate coordinate in data set ! :"+candidates.get(test).get(0));
        else{
            for(Map.Entry<Double,ArrayList<String>> entry : candidates.entrySet()){
                neighbors.addAll(entry.getValue());
            }
        }
        return distance;
    }

    private static double checkPossibleCloserPoint(double distance, int nearestBucket,int[][] nextBucket,String[] nnq,ArrayList<String> neighbors) {
        System.out.println("Check possible closer points...");
        double newDistance = Double.POSITIVE_INFINITY;
        if(distance >= CONSTANT.BUCKET_SIZE * nearestBucket){
            System.out.println("Possible closer point !");
            ArrayList<String> newFoundPoints = new ArrayList<>();
            getNextBuckets(nextBucket,newFoundPoints);

            ArrayList<String> newNeighbors = new ArrayList<>();
            newDistance = calculateMinDistance(nnq,newFoundPoints,newNeighbors);
            if(newDistance < distance){
                System.out.println("Found closer points!");
                neighbors.clear();
                neighbors.addAll(newNeighbors);
            }
        }
        return newDistance;
    }

    private static void saveQueryResult(ArrayList<String> result,String[] query) {
        System.out.println("Saving result to file ...");
        StringBuilder name = new StringBuilder("");
        String suffix ;
        if(query.length ==6)
            suffix = ".rq";
        else
            suffix = ".nnq";
        for(String s :query) {
            name.append(s).append("_");
        }
        String outFileName  = name.toString().substring(0,name.length()-1);
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(filePath+"output\\"
                    +outFileName+suffix,false));
            for(String s:result){
                bw.write(s+"\n");
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Result File Saved At : "+filePath+"output\\"+outFileName+suffix);
    }

    private static ArrayList<String> retrieveDataFromDisk(ArrayList<String> pointers){
        ArrayList<String> result = new ArrayList<>();
        try {
            RandomAccessFile ra = new RandomAccessFile(filePath+fileName,"r");
            String line;
            for(String p : pointers){
                try {
                    ra.seek(Long.valueOf(p));
                    line = ra.readLine();
                    result.add(line);
                } catch (IOException e) {
                    System.out.println("Read from disk failed !");
                    e.printStackTrace();
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

//    private static void writeIndexToDisk(){
//        int j = 0;
//        for(Map.Entry<Integer,HashMap<Integer,HashMap<Integer,ArrayList<String>>>> entry:index.entrySet()){
//            for(Map.Entry<Integer,HashMap<Integer,ArrayList<String>>> entry1 : entry.getValue().entrySet()){
//                for(Map.Entry<Integer,ArrayList<String>> entry2:entry1.getValue().entrySet()){
//                    try
//                    {
//                        BufferedWriter writer;
//                        File file = new File(filePath+"output\\index.index");
//                        file.createNewFile();
//
//                        writer = new BufferedWriter(new FileWriter(file));
//                        for(String s : entry2.getValue()){
//                            writer.write(s);
//                            j++;
//                            if(j%1000000 ==0)
//                                System.out.println("j :"+j);
//                        }
//                    }catch(IOException ioe)
//                    {
//                        ioe.printStackTrace();
//                    }
//                }
//            }
//        }
//
//        System.out.println("write fiish");
//    }

}
