package GenerateData;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/** @author Lekang Du
 * Information needs to generate: CNumber(8);
 * AmountPaid (Dec(9,2)); Cname(25) and  CID(9)
 */
public class GenerateRandomData {
    private String CDate = "2015-10-12";
    private String CAddress = "Stewartsville NJ 8886 Northeast                                                                                                                       "; // 150 chars
    private String CEmail = "sommerville5y@pinterest.com ";  // 28 chars
    private String InsuredItem = "06"; // 2
    private String AmountDamage = "270299.42";  // Dec(9,2)
//    private float AmountPaid;  // Dec(9,2)
    private int totalLines;  // how many lines of data you want to generate
    private int totalPeople; // how many people you want to have in this file

    private SourceEn En = new SourceEn();
    File outPutFile;
    FileWriter fw;

    /** @author Lekang Du
     * input totalLines and totalPeople
     */
    public GenerateRandomData(int totalLines,int totalPeople){
        this.totalLines = totalLines;
        this.totalPeople = totalPeople;
    }

    /** @author Lekang Du
     * generate CNumber, 8 chars, start from 11111111
     */
    private String gernerateCNumber(int numOfcurrentLines){
        return Integer.toString(numOfcurrentLines + 11111111);
    }
    /** @author Lekang Du
     * generate CID, 9 chars, from
     * generate CName, 25 chars, start with firstname,
     * there's a space between first name and last name
     * format: "435830994Ninette Sickert          ",
     *
     */
    public HashSet<String> genrateCNameAndCID(){
        HashSet<String> nameSet = generateName();
        HashSet<Integer> CIDSet = new HashSet<>();
        HashSet<String> CNameAndCID = new HashSet<>();
        Iterator<String> it = nameSet.iterator();

        while(CNameAndCID.size()<totalPeople){
            StringBuffer br = new StringBuffer();
            Random random = new Random();
            int number=(int)(random.nextInt(899999999)+100000000);
            if(!CIDSet.contains(number) && it.hasNext()){
                br.append(number);
                br.append(it.next());
                CIDSet.add(number);
                while(br.length() < 34){
                    br.append(" ");
                }
                String t = br.toString();
                if(t.length() > 34){
//                    System.out.println("length bigger than 34!!!");
                    break;
                }
//                System.out.println(t);
                CNameAndCID.add(t);
            }
        }
//        System.out.println(CNameAndCID.size());
//        System.out.println(AmountDamage);

        return CNameAndCID;
    }

    public String genrateAmountPaid(){
        float f = nextFloat(100000.01f,999999.99f);
        String AmountPaid = formateRate(Float.toString(f));
        return AmountPaid;
    }

    /**
     * (min,max)
     */
    private static float nextFloat(final float min, final float max) {
        return min + ((max - min) * new Random().nextFloat());
    }

    /**
     * 保留小数点后两位小数
     * @param rateStr xx.xxxxx
     * @return result   xx.xx
     * */
    private static String formateRate(String rateStr) {
        if (rateStr.contains(".")) {
            // 获取小数点的位置
            int num = 0;
            num = rateStr.indexOf(".");

            String dianAfter = rateStr.substring(0, num + 1);
            String afterData = rateStr.replace(dianAfter, "");
            if(afterData.length() == 1){
                afterData = afterData + "0";
            }

            return rateStr.substring(0, num) + "." + afterData.substring(0, 2);
        } else {
            if (rateStr.equals("1")) {
                return "100";
            } else {
                return rateStr;
            }
        }
    }
    /**
     * generate name set
     */
    private HashSet<String> generateName(){
        HashSet<String> nameSet = new HashSet<>();
        int j = 0;
            for(String temp_First : En.namesEn){
                for(int i = En.namesEn.length; i > 1;i--){
                    if(nameSet.size() == totalPeople){
                        break;
                    }
                    String name = null;
                    String temp_Last = null;
                    StringBuffer br = new StringBuffer();
                    temp_Last = En.namesEn[i - 1];
                    br.append(temp_First).append(" ").append(temp_Last);
                    name = br.toString();
                    nameSet.add(name);
                }
        }
        return nameSet;
    }

    /**
     * write into txt file
     * gernerateCNumber(int numOfcurrentLines)
     * String CDate = "2015-10-12"
     * HashSet<String> genrateCNameAndCID()
     * String CAddress = "Stewartsville NJ 8886 Northeast                                                                                                                       "
     * String CEmail = "sommerville5y@pinterest.com ";  // 28 chars
     * String InsuredItem = "06"; // 2
     * String AmountDamage = "270299.42";  // Dec(9,2)
     * String genrateAmountPaid
     */
    public void writeFile(){
        try{
            int currentLines = 0;
            HashSet<String> CIDandNameSet = genrateCNameAndCID();
            Iterator<String> it = CIDandNameSet.iterator();
            Iterator<String> it2 = CIDandNameSet.iterator();

            outPutFile = new File(System.getProperty("user.dir")+"\\src\\input\\" + totalLines+"_"+totalPeople + ".txt");

            if (!outPutFile.exists()) {
                outPutFile.createNewFile();
            }
            fw = new FileWriter(outPutFile.getAbsoluteFile(),false);
            while(currentLines < totalLines){
                StringBuffer br = new StringBuffer();
                br.append(gernerateCNumber(currentLines));
                br.append(CDate);
                if(it.hasNext()){
                    br.append(it.next());
                }
                else {
                    br.append(it2.next());
                }
                br.append(CAddress);
                br.append(CEmail);
                br.append(InsuredItem);
                br.append(AmountDamage);
                br.append(genrateAmountPaid());
                br.append("\r");
                String outPutLine = br.toString();
//                System.out.print(outPutLine);
                fw.write(outPutLine);

                currentLines++;
            }
            System.out.println("done");
            fw.close();



        }catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if (fw != null)fw.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

    }


}
}
