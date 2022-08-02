package mal;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import mal.types.MalFunction;
import mal.types.MalInteger;
import mal.types.MalList;
import mal.types.MalSymbol;
import mal.types.MalType;

class step2_eval{

    public static Map<String,MalFunction> eval_env; 

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        eval_env = new HashMap<>();
        eval_env.put("+", new MalFunction(a -> ((MalInteger)a.malTypes.getFirst()).add((MalInteger)a.malTypes.get(1))));
        eval_env.put("-", new MalFunction(a -> ((MalInteger)a.malTypes.getFirst()).sub((MalInteger)a.malTypes.get(1))));
        eval_env.put("*", new MalFunction(a -> ((MalInteger)a.malTypes.getFirst()).mul((MalInteger)a.malTypes.get(1))));
        eval_env.put("/", new MalFunction(a -> ((MalInteger)a.malTypes.getFirst()).div((MalInteger)a.malTypes.get(1))));
        
        while(true){
            System.out.print("user> ");

            String input = scanner.nextLine();
            if(input.equals("exit")){ scanner.close(); break; }

            System.out.println(
                repl(input,eval_env)
            );
        }
    }

    private static String repl(String input, Map env){
        return PRINT(EVAL(READ(input)));
    }

    private static MalType eval_ast(MalType ast){
        if(ast instanceof MalList){
            MalList old_ast = (MalList) ast;
            return old_ast.mutate(step2_eval::EVAL);
        }
        else if(ast instanceof MalSymbol){
            MalSymbol symbol = (MalSymbol) ast;
            return eval_env.get(symbol.val);
        }
        return ast;
    }

    private static MalType READ(String input){
        return reader.read_str(input);
    }
    private static MalType EVAL(MalType ast){
        if(ast instanceof MalList){
            MalList old_ast = (MalList) ast;
            if(old_ast.isEmpty()) return ast;

            MalType head = old_ast.malTypes.poll();
            if(!(head instanceof MalSymbol)) throw new RuntimeException("something is wrong!");

            MalSymbol fSymbol = (MalSymbol) head;
            MalType args = eval_ast(old_ast);
            MalFunction f = eval_env.get(fSymbol.val);

            return f.malFunction.apply((MalList)args);

        }else{
            return eval_ast(ast);
        }
    }
    private static String PRINT(MalType input){
        return input.toString();
    }
}