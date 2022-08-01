package mal;
import java.util.Scanner;

class step0_repl{

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        while(true){
            System.out.print("user> ");

            String input = scanner.nextLine();
            if(input.equals("exit")){ scanner.close(); break; }

            System.out.println(repl(input));
        }
       
    }

    private static String repl(String input){
        return READ(EVAL(PRINT(input)));
    }

    private static String READ(String input)  { return input; }
    private static String EVAL(String input)  { return input; }
    private static String PRINT(String input) { return input; }
}