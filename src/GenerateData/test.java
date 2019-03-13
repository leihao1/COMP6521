package GenerateData;

public class test {
    public static void main(String[] args){
        // total lines: 100000; total people:80000
        // total lines: 1000000; total people:700000
        GenerateRandomData ger = new GenerateRandomData(100001,70000);
        ger.writeFile();
    }
}
