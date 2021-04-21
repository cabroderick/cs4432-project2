import java.util.Scanner;

public class App {
    public static void main(String[] args) throws Exception {
        getInput();
    }

    private static void getInput () {
        System.out.println("Program is ready and waiting for user command.");
        Scanner scanner = new Scanner(System.in);
        String command = scanner.nextLine();
        scanner.close();
        getInput();
    }
}
