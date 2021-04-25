import java.util.Scanner;

import javax.lang.model.util.ElementScanner6;

import java.util.Hashtable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

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

    static Hashtable<Integer, String> tableIndex = new Hashtable<>();
    static ArrayList<String>[] arrayIndex = new ArrayList[5001];
    static boolean indexed = false; // indicates whether or not the index tables have been created

    public static void main(String[] args) throws Exception {
        getInput();
    }

    private static void getInput () {
        System.out.println("Program is ready and waiting for user command.");
        Scanner scanner = new Scanner(System.in);
        String command = scanner.nextLine();
        if (command.equals("CREATE INDEX ON Project2Dataset (RandomV)")) {
            initIndexes();
            System.out.println("The hash-based and array-based indexes are built successfully.");
        } else
        if (command.substring(0, 46).equals("SELECT * FROM Project2Dataset WHERE RandomV = ")) {
            int v = Integer.parseInt(command.substring(46, command.length()));
            lookup(v);
        } else
        if (command.substring(0, 46).equals("SELECT * FROM Project2Dataset WHERE RandomV > ")) {
            String end = command.substring(46, command.length());
            int v1 = Integer.parseInt(end.split(" ")[0]);
            int v2 = Integer.parseInt(end.split(" ")[4]);
            lookupRange(v1, v2);
        } else
        if (command.substring(0, 46).equals("SELECT * FROM Project2Dataset WHERE RandomV !=")) {
            int v = Integer.parseInt(command.substring(46, command.length()));
            lookupUnequal(v);
        }
        // scanner.close();
        getInput();
    }

    // initializes the hash table
    private static void initIndexes() {
        try {
            for (int F = 1; F <= 99; F++) { // iterate through each file in the dataset directory
                String fileName = getFileName(F);
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
                    tableIndex.put(k, v);
                    if (arrayIndex[k] == null) { // if the entry in the array is null, then initialize the array list. otherwise, just add to the list.
                        arrayIndex[k] = new ArrayList<String>();
                    }
                    arrayIndex[k].add(v);
                }
                scanner.close();
                indexed = true;
            }
        } catch (IOException e) {
            System.out.println("Invalid filename");
        }
        
    }

    // gets a value from the table
    private static void lookup (int v) {
        if (indexed) { // lookup from the index table
            long start = System.currentTimeMillis();

            String val = tableIndex.get(v);

            long end = System.currentTimeMillis();

            long timeElapsed = end - start;

            String[] vals = val.split("-");
            int F = Integer.parseInt(vals[0]);
            int O = Integer.parseInt(vals[1]);

            String queryVal = getRecord(F, O);

            System.out.println("Record matching query: "+queryVal);
            System.out.println("Index type used: hashtable");
            System.out.println("Time taken to answer query: "+timeElapsed+" ms");
            System.out.println("Data files read: 1");
        } else { // look through every entry in the table
            String queryVal = "";
            int filesRead = 0; // the number of files accessed
            long start = System.currentTimeMillis();
            long timeElapsed = 0;
            for (int F = 1; F <= 99; F++) {
                filesRead++;
                for (int O = 0; O < 4000; O+=40) {
                    int randomV = getRandomV(F, O);
                    if (randomV == v) {
                        queryVal = getRecord(F, O);
                        long end = System.currentTimeMillis();
                        timeElapsed = end - start;
                        F = 100;
                        break;
                    }
                }
            }

            System.out.println("Record matching query: "+queryVal);
            System.out.println("Table scan used");
            System.out.println("Time taken to answer query: "+timeElapsed+" ms");
            System.out.println("Data files read: "+filesRead);
        }
        
    }

    private static String getFileName (int F) {
        return System.getProperty("user.dir")+"/Project2Dataset/F"+F+".txt";
    }

    // returns a record where F is the file number and O is the offset in the file (in bytesP)
    private static String getRecord (int F, int O) {
        try {
            String fileName = getFileName(F);
            File file = new File(fileName);
            Scanner scanner = new Scanner(file);
            String data = scanner.nextLine();
            int startIndex = O;
            int endIndex = O + 37;
            scanner.close();
            return data.substring(startIndex, endIndex);

        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        }

        return "";
        
    }

    private static int getRandomV (int F, int O) {
        try {
            String fileName = getFileName(F);
            File file = new File(fileName);
            Scanner scanner = new Scanner(file);
            String data = scanner.nextLine();
            int startIndex = O + 33;
            int endIndex = O + 37;
            scanner.close();
            return Integer.parseInt(data.substring(startIndex, endIndex));

        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        }

        return -1;
    }

    // returns all queries within a given range
    private static void lookupRange (int v1, int v2) {
        if (indexed) { // range search in the array based index
            ArrayList<Integer> visitedFiles = new ArrayList<>();
            int filesRead = 0;
            long start = System.currentTimeMillis();
            for (int i = v1 + 1; i < v2; i++) { // search for all queries
                if (arrayIndex[i] != null) {
                    for (String val: arrayIndex[i]) {
                        String[] vals = val.split("-");
                        int F = Integer.parseInt(vals[0]);
                        int O = Integer.parseInt(vals[1]);
                        String queryVal = getRecord(F, O);
                        System.out.println("Record matching query: "+queryVal);
                        if (!visitedFiles.contains(F)) {
                            visitedFiles.add(F);
                            filesRead++;
                        }
                    }
                }
            }
            long end = System.currentTimeMillis();

            long timeElapsed = end - start;

            System.out.println("Index type used: array-based");
            System.out.println("Time taken to answer query: "+timeElapsed+" ms");
            System.out.println("Data files read: "+filesRead);
        } else { // full table 
            long start = System.currentTimeMillis();
            int filesRead = 0;
            for (int F = 1; F <= 99; F++) {
                filesRead++;
                for (int O = 0; O < 4000; O+=40) {
                    int randomV = getRandomV(F, O);
                    if (randomV > v1 && randomV < v2) {
                        String queryVal = getRecord(F, O);
                        System.out.println("Record matching query: "+queryVal);
                    }
                }
            }
            long end = System.currentTimeMillis();
            long timeElapsed = end - start;
            System.out.println("Table scan used");
            System.out.println("Time taken to answer query: "+timeElapsed+" ms");
            System.out.println("Data files read: "+filesRead);
        }
        
    }

    // looks up for inequality
    // no index table case here
    private static void lookupUnequal (int v) {
        long start = System.currentTimeMillis();
        int filesRead = 0;
        for (int F = 1; F <= 99; F++) {
            filesRead++;
            for (int O = 0; O < 4000; O+=40) {
                int randomV = getRandomV(F, O);
                if (randomV != v) {
                    String queryVal = getRecord(F, O);
                    System.out.println("Record matching query: "+queryVal);
                }
            }
        }
        long end = System.currentTimeMillis();
        long timeElapsed = end - start;
        System.out.println("Table scan used");
        System.out.println("Time taken to answer query: "+timeElapsed+" ms");
        System.out.println("Data files read: "+filesRead);
    }
}
