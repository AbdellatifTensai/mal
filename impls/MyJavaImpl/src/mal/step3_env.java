package mal;
import java.util.Scanner;

import mal.env.Env;
import mal.types.IMalFunction;
import mal.types.MalInteger;
import mal.types.MalList;
import mal.types.MalSymbol;
import mal.types.MalType;

class step3_env{

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Env eval_env = new Env(null);
        IMalFunction add = a -> new MalInteger(a.getMalList().get(0).getInteger() + a.getMalList().get(1).getInteger());
        IMalFunction sub = a -> new MalInteger(a.getMalList().get(0).getInteger() - a.getMalList().get(1).getInteger());
        IMalFunction mul = a -> new MalInteger(a.getMalList().get(0).getInteger() * a.getMalList().get(1).getInteger());
        IMalFunction div = a -> new MalInteger(a.getMalList().get(0).getInteger() / a.getMalList().get(1).getInteger()); 
        eval_env.set(new MalSymbol("+"), add); 
        eval_env.set(new MalSymbol("-"), sub);
        eval_env.set(new MalSymbol("*"), mul);
        eval_env.set(new MalSymbol("/"), div);
        
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
            MalList list = new MalList();
            ast.getMalList().malTypes.forEach(x->list.add(EVAL(x, env)));
            return list;
        }
        else if(ast.symbol_Q()){
            return env.get(ast.getMalSymbol());
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
        if(ast.getMalList().isEmpty()) return ast;

        MalSymbol head = ast.getMalList().get(0).getMalSymbol(); 
        
        switch(head.val){
            case "def!": 
                MalSymbol def_first_arg = ast.getMalList().get(1).getMalSymbol();
                MalType def_second_eval_arg = EVAL(ast.getMalList().get(2),env);
                env.set(def_first_arg, def_second_eval_arg);

                return def_second_eval_arg;

            case "let*": // (let* [c 2] c) -> set(c,2), return eval(c) a.k.a: list<symbol, list<symbol, int>, symbol>
                Env let_env = new Env(env,null,null);
                MalList let_first_arg = ast.getMalList().get(1).getMalList();
                MalType let_second_arg = ast.getMalList().get(2);

                let_env.set(let_first_arg.get(0).getMalSymbol(), EVAL(let_first_arg.get(1), let_env));
                return EVAL(let_second_arg, let_env);

            default:
                MalType args = eval_ast(ast, env);
                IMalFunction f = env.get(head).getMalFunction();
                return f.apply((MalList)args);
        }
    }

    private static String PRINT(MalType input){
        return input.toString();
    }
}