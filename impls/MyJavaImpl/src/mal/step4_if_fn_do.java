package mal;
import java.util.Scanner;

import mal.env.Env;
import mal.types.MalFunction;
import mal.types.MalList;
import mal.types.MalSymbol;
import mal.types.MalType;

class step4_if_fn_do{

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
        if(!ast.list_Q()) return eval_ast(ast, env);
        if(ast.getMalList().isEmpty()) return ast;

        MalType head = ast.getMalList().get(0);

        switch(head.symbol_Q()? head.getMalSymbol().val: ""){
            case "def!": // (def! a b)
                MalSymbol def_first_arg = ast.getMalList().get(1).getMalSymbol();
                MalType def_second_eval_arg = EVAL(ast.getMalList().get(2), env);
                env.set(def_first_arg, def_second_eval_arg);
                return def_second_eval_arg;

            case "let*": // (let* [c 2] c) -> set(c,2), return eval(c) a.k.a: list<symbol, list<symbol, int>, symbol>
                Env let_env = new Env(env);
                MalList let_first_args = ast.getMalList().get(1).getMalList();
                MalType let_second_arg = ast.getMalList().get(2);
                let_env.set(let_first_args.get(0).getMalSymbol(), EVAL(let_first_args.get(1), let_env));
                return EVAL(let_second_arg, let_env);

            case "do": //( do a b c d ) :: list< 1:do, 2:list<args>, 3:last_arg >
                return eval_ast(ast.getMalList().tail(), env).getMalList().getLast();
            
            case "if": // (if a b c)
                return EVAL(EVAL(ast.getMalList().get(1), env).equals(core.True) ?
                                 ast.getMalList().get(2):
                                 ast.getMalList().get(3),env);
            
            case "fn*": // ((fn* (a b) (a b)) c1 c2) -> [(parameters)(body)]{arguments} 
                return new MalFunction(){
                    @Override public MalType apply(MalList t) {
                        Env fn_env = new Env(env, ast.getMalList().get(1).getMalList(), t);
                        return EVAL(ast.getMalList().get(2), fn_env);
                    }};
                                       
            default:
                MalType args = eval_ast(ast, env);
                MalFunction f = args.getMalList().remove(0).getMalFunction();
                return f.apply((MalList)args);
        }
    }

    private static String PRINT(MalType input){
        return input.toString();
    }
}