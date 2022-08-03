package mal;
import java.util.LinkedList;
import java.util.Scanner;

import mal.env.Env;
import mal.types.MalFunction;
import mal.types.MalInteger;
import mal.types.MalList;
import mal.types.MalType;

class step3_env{

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Env eval_env = new Env(null);
        eval_env.set("+", new MalFunction(a -> MalInteger.add((MalInteger)a.getSecond(),(MalInteger)a.getFirst())));
        eval_env.set("-", new MalFunction(a -> MalInteger.sub((MalInteger)a.getSecond(),(MalInteger)a.getFirst())));
        eval_env.set("*", new MalFunction(a -> MalInteger.mul((MalInteger)a.getSecond(),(MalInteger)a.getFirst())));
        eval_env.set("/", new MalFunction(a -> MalInteger.div((MalInteger)a.getSecond(),(MalInteger)a.getFirst())));
        
        while(true){
            System.out.print("user> ");

            String input = scanner.nextLine();
            if(input.equals("exit")){ scanner.close(); break; }

            System.out.println(
                repl(input, eval_env)
            );
        }
    }

    private static String repl(String input, Env env){
        return PRINT(EVAL(READ(input), env));
    }

    private static MalType eval_ast(MalType ast, Env env){
        if(ast.list_Q()){
            ast.getMalTypes().replaceAll(x->EVAL(x, env));
            return ast;
        }
        else if(ast.symbol_Q()){
            return env.get(ast.getMalSymbol().val);
        }
        return ast;
    }

    private static MalType READ(String input){
        MalType output = reader.read_str(input);
        System.out.println(output);
        return output;
    }

    private static MalType EVAL(MalType ast, Env env){
        if(!ast.list_Q()) return eval_ast(ast, env);
        if(ast.getMalTypes().isEmpty()) return ast;

        String head = ast.getMalTypes().poll().getMalSymbol().val; 
        
        switch(head){
            case "def!": 
                String def_first_arg = ast.getMalTypes().getFirst().getMalSymbol().val;
                MalType def_second_eval_arg = EVAL(ast.getMalTypes().getLast(),env);
                env.set(def_first_arg, def_second_eval_arg);

                return def_second_eval_arg;

            case "let*": // (let* [c 2] c) -> set(c,2), return eval(c) a.k.a: list<symbol, list<symbol, int>, symbol>
                Env let_env = new Env(env);
                LinkedList<MalType> let_first_arg = ast.getMalTypes().getFirst().getMalTypes();
                MalType let_second_arg = ast.getMalTypes().getLast();

                let_env.set(let_first_arg.getFirst().getMalSymbol().val, EVAL(let_first_arg.getLast(), let_env));
                return EVAL(let_second_arg, let_env);

            default:
                MalType args = eval_ast(ast, env);
                MalFunction f = env.get(head).getMalFunction();
                return f.apply((MalList)args);
        }
    }

    private static String PRINT(MalType input){
        return input.toString();
    }
}