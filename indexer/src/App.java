import java.util.Scanner;
import java.util.Hashtable;
import java.io.File;
import java.io.IOException;

/**
 * Important notes:
 * Lookup query: a query that involves a predicate on a certain column
 * Two conditions to be satisifed:
 * 1. Full table scan: the database system reads the entire table one by one, if no index is available
 * 2. Index lookup: the database system checks the index, and if the value exists it specifies which data block to read. This occurs if there is an index available on the column
 * involved in the condition.
 * 
 * TASK ONE: Build hash table
 * Iterate through every record and extract its RandomV value into a hash table
 */

public class App {
     // the table to store the RandomV values for each record in the dataset directory
    // format will be F-O where F is the file # (1-99) and O is the offset # (0-3960)

    static Hashtable<Integer, String> table = new Hashtable<>();

    public static void main(String[] args) throws Exception {
        getInput();
    }

    private static void getInput () {
        System.out.println("Program is ready and waiting for user command.");
        Scanner scanner = new Scanner(System.in);
        String command = scanner.nextLine();
        if (command.equals("CREATE INDEX ON Project2Dataset (RandomV)")) {
            initTable();
        }
        getInput();
    }

    // initializes the hash table
    private static void initTable () {
        try {
            for (int F = 1; F <= 99; F++) { // iterate through each file in the dataset directory
                String fileName = System.getProperty("user.dir")+"/Project2Dataset/F"+F+".txt";
                File file = new File(fileName);
                Scanner scanner = new Scanner(file);
                String data = scanner.nextLine();
                for (int R = 0; R < 100; R++) { // iterate through each record, each record is 40 characters long
                    // the desired byte range is (40*r + 32) to (40*r + 36)
                    int beginIndex = 40*R + 33;
                    int endIndex = 40*R + 37;
                    Integer k = Integer.parseInt(data.substring(beginIndex, endIndex));
                    int O = R*40; // the offset #, record # * 40 since each record is of size 40 bytes
                    String v = Integer.toString(F)+"-"+Integer.toString(O);
                    // now add values to hashtable
                    table.put(k, v);
                    System.out.println(table);
                }
                scanner.close();
            }
        } catch (IOException e) {
            System.out.println("Invalid filename");
        }
        
    }
}
