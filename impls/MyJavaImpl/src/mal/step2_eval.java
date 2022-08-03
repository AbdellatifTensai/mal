package mal;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import mal.types.MalFunction;
import mal.types.MalInteger;
import mal.types.MalList;
import mal.types.MalType;

class step2_eval{

    public static Map<String,MalFunction> eval_env; 

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        eval_env = new HashMap<>();
        eval_env.put("+", new MalFunction(a ->MalInteger.add((MalInteger)a.getSecond(),(MalInteger)a.getFirst())));
        eval_env.put("-", new MalFunction(a ->MalInteger.add((MalInteger)a.getSecond(),(MalInteger)a.getFirst())));
        eval_env.put("*", new MalFunction(a ->MalInteger.add((MalInteger)a.getSecond(),(MalInteger)a.getFirst())));
        eval_env.put("/", new MalFunction(a ->MalInteger.add((MalInteger)a.getSecond(),(MalInteger)a.getFirst())));
        
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

    private static MalType eval_ast(MalType ast){
        if(ast.list_Q()){
            ast.getMalTypes().forEach(step2_eval::EVAL);
            return ast;
        }
        else if(ast.symbol_Q()){
            return eval_env.get(ast.getMalSymbol().val);
        }
        return ast;
    }

    private static MalType READ(String input){
        MalType output = reader.read_str(input);
        System.out.println(output);
        return output;
    }

    private static MalType EVAL(MalType ast){
        if(ast.list_Q()){
            if(ast.getMalTypes().isEmpty()) return ast;

            MalType head = ast.getMalTypes().poll();
            if(!head.symbol_Q()) throw new RuntimeException("something is wrong!");
            MalType args = eval_ast(ast);
            MalFunction f = eval_env.get(head.getMalSymbol().val);

            return f.malFunction.apply((MalList)args);

        }else{
            return eval_ast(ast);
        }
    }
    private static String PRINT(MalType input){
        return input.toString();
    }
}