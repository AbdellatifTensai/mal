package mal;
import java.util.Scanner;

import mal.env.Env;
import mal.types.IMalFunction;
import mal.types.MalFunction;
import mal.types.MalList;
import mal.types.MalSymbol;
import mal.types.MalType;

class step5_tco{

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Env eval_env = new Env(null, core.NS);
        
        while(true){
            System.out.print("user> ");

            String input = scanner.nextLine();
            if(input.equals("exit")){ scanner.close(); break; }

            // try{
                System.out.println(
                    repl(input, eval_env)
                );
            // }
            // catch(Exception e){
            //     System.out.println(e);
            // }
        }
    }

    private static String repl(String input, Env env){
        return PRINT(EVAL(READ(input), env));
    }

    private static MalType eval_ast(MalType ast, Env env){
        if(ast.list_Q())
            return ast.getMalList().map(x->EVAL(x, env));

        else if(ast.symbol_Q())
            return env.get(ast.getMalSymbol());

        return ast;
    }

    private static MalType READ(String input){
        MalType output = reader.read_str(input);
        System.out.println(output);
        return output;
    }

    private static MalType EVAL(MalType ast, Env env){
        while(true){
        if(!ast.list_Q()) return eval_ast(ast, env);
        if(ast.getMalList().isEmpty()) return ast;

        MalType head = ast.getMalList().get(0);

        switch(head.symbol_Q()? head.getMalSymbol().val: ""){
            case "def!": // (def! a b)
                MalSymbol def_first_arg = ast.getMalList().get(1).getMalSymbol();
                MalType def_second_eval_arg = EVAL(ast.getMalList().get(2), env);
                env.set(def_first_arg, def_second_eval_arg);
                return def_second_eval_arg;

            case "let*": // (let* [c 2] c)
                Env let_env = new Env(env);
                MalList let_first_args = ast.getMalList().get(1).getMalList();
                MalType let_second_arg = ast.getMalList().get(2);
                let_env.set(let_first_args.get(0).getMalSymbol(), EVAL(let_first_args.get(1), let_env));
                ast = let_second_arg;
                env = let_env;
                break;

            case "do": //( do a b c d )
                MalList do_args = ast.getMalList().subList(1, ast.getMalList().size()-2);
                MalType do_last_arg = ast.getMalList().getLast();
                eval_ast(do_args, env);
                ast = do_last_arg;
                break;
            
            case "if": // (if a b c)
                if(EVAL(ast.getMalList().get(1), env).equals(core.True)) ast = ast.getMalList().get(2);
                else ast = ast.getMalList().get(3);
                break;
                            
            case "fn*": // ((fn* (a b) (a b)) c1 c2) -> [(parameters)(body)]{arguments} 
                return new MalFunction(ast.getMalList(), env){
                    @Override public MalType apply(MalList arguments){
                        return EVAL(this.body, new Env(this.env, this.params.getMalList(), arguments));
                    }
                };

            default:
                MalType args = eval_ast(ast, env);
                IMalFunction f = args.getMalList().get(0).getMalFunction();
                if(f instanceof MalFunction){
                    ast = f.getMalFunctionImpl().body;                    
                    env = new Env(f.getMalFunctionImpl().env, f.getMalFunctionImpl().params.getMalList(), args.getMalList().rest());
                }
                else return f.apply(((MalList)args).rest());
        }
        }
    }

    private static String PRINT(MalType input){
        return input.toString();
    }
}