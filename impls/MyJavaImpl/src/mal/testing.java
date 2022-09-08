package mal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class testing {
    
    public static void main(String[] args) {
        test4();
    }

    static void test3(){
        Dummy dummy1 = new Dummy(){ @Override public Dummy apply(){ System.out.println("hi dummy1"); return this; }}; 
        Dummy dummy2 = new Dummy(){ @Override public Dummy apply(){ return this; }}; 

        dummy1.apply().then(()-> {System.out.println("orElse dummy1"); return dummy1;});
        dummy2.apply().then(()-> {System.out.println("orElse dummy2"); return dummy2;});
    }

    static void test4(){
        Dommy dommy1 = new Dommy(){ @Override public Dommy apply(){ System.out.println("hi dommy1"); applied=true; return this; }}; 
        Dommy dommy2 = new Dommy(){ @Override public Dommy apply(){ return this; }}; 

        dommy1.apply().orElse(new Dommy(){ @Override Dommy apply(){ System.out.println("orElse dommy1"); return this; }});
        dommy2.apply().orElse(new Dommy(){ @Override Dommy apply(){ System.out.println("orElse dommy2"); return this; }});
    }

    abstract static class Dommy{
        boolean applied;
        Dommy apply(){ applied = false; return this;}
        void orElse(Dommy func){ if(!applied) func.apply(); }
    }

    interface Dummy{
        Dummy apply();
        default void then(Dummy func){ func.apply(); }
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
