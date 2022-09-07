package mal;

import java.util.Scanner;

public class testing {
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while(true){
            String str = scanner.nextLine();
            if(str.equals("exit")){ scanner.close(); break; }
            str.chars().forEach(System.out::println);
            System.out.println("------------------");
            System.out.println(str);
            System.out.println(str.replace("\\n","\n"));
        }
    }
}
