package mal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class testing {
    
    public static void main(String[] args) {
        test2();
    }

    static void test2(){
        List<Integer> list = new ArrayList<>();
        List<Integer> list2 = Arrays.asList(1);
        list.add(2);
        list.addAll(list2);
        System.out.println(list);
        System.out.println(list2);
    }

    static void test1(){
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
