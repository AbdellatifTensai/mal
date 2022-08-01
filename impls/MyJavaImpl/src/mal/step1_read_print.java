package mal;
import java.util.Scanner;

import mal.types.MalType;

class step1_read_print{

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        while(true){
            System.out.print("user> ");

            String input = scanner.nextLine();
            if(input.equals("exit")){ scanner.close(); break; }

            System.out.println(
                repl(input)
            );
        }
    }

    private static String repl(String input){
        return PRINT(EVAL(READ(input)));
    }

    private static MalType READ(String input){
        return reader.read_str(input);
    }
    private static MalType EVAL(MalType input){
        return input;
    }
    private static String PRINT(MalType input){
        return input.toString();
    }
}