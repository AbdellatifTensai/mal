package mal;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

import mal.env.Env;
import mal.types.IMalFunction;
import mal.types.MalException;
import mal.types.MalFunction;
import mal.types.MalList;
import mal.types.MalSymbol;
import mal.types.MalType;

class step9_try{

    public static void main(String[] args) throws IOException{
        Scanner scanner = new Scanner(System.in);
        Env eval_env = new Env(null, core.NS);

        eval_env.set(new MalSymbol("eval"), new IMalFunction(){ @Override public MalType apply(MalList arg){ return EVAL(arg.get(0), eval_env); } });
        Files.lines(Paths.get(System.getProperty("user.dir"),"/impls/MyJavaImpl/src/mal/core.mal")).forEach(x->repl(x, eval_env));

        while(true){
            System.out.print("user> ");
            String input = scanner.nextLine();
            if(input.equals("exit")){ scanner.close(); break; }

            try{System.out.println(
                repl(input, eval_env));}
            catch(Exception e){ System.out.println(e); }
        }
    }

    private static String repl(String input, Env env){
        return PRINT(EVAL(READ(input), env));
    }

    private static MalType eval_ast(MalType ast, Env env){
        if(ast.list_Q()){
            MalList list = new MalList();
            ast.getMalList().malTypes.forEach(x -> list.add(EVAL(x, env)));
            return list;
        }

        else if(ast.symbol_Q())
            return env.get(ast.getMalSymbol());

        return ast;
    }

    private static MalType READ(String input){
        MalType output = reader.read_str(input);
        System.out.println(printer._pr_str(output.toString(), false));
        return output;
    }

    private static MalType EVAL(MalType ast, Env env){
        while(true){
        if(!ast.list_Q()) return eval_ast(ast, env);
        if(ast.getMalList().isEmpty()) return ast;

        ast = macroExpand(ast, env);
        if(!ast.list_Q()) return eval_ast(ast, env);

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
                MalList do_args = ast.getMalList().subList(1, ast.getMalList().size()-1);
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
            
            case "quote":
                return ast.getMalList().get(1);

            case "quasiquote":
                ast = quasiquote(ast.getMalList().get(1)); break;
                
            case "quasiquoteexpand":
                return quasiquote(ast.getMalList().get(1));

            case "defmacro!":
                MalSymbol macro_frst_arg = ast.getMalList().get(1).getMalSymbol();
                IMalFunction macro_sec_eval_arg = EVAL(ast.getMalList().get(2), env).getMalFunctionImpl().setMacro();
                env.set(macro_frst_arg, macro_sec_eval_arg);
                return macro_sec_eval_arg;               

            case "macroexpand":
                return macroExpand(ast.getMalList().get(1), env);

            case "try*": // (try*)
                try{
                    return EVAL(ast.getMalList().get(1), env);
                }catch(RuntimeException e){
                    MalList cat = ast.getMalList().get(2).getMalList(); 
                    return EVAL(cat.get(2), new Env(env, new MalList(cat.get(1)), new MalList(new MalException(e))));
                }

            default:
                MalType args = eval_ast(ast, env);
                IMalFunction f = args.getMalList().get(0).getMalFunction();
                if(f instanceof MalFunction){
                    ast = f.getMalFunctionImpl().body;                    
                    env = new Env(f.getMalFunctionImpl().env, f.getMalFunctionImpl().params.getMalList(), args.getMalList().rest());
                }
                else {
                    MalType result = f.apply(((MalList)args).rest()); 
                    return result; 
                }
        }
        }
    }

    private static MalType quasiquote(MalType ast){
        if(ast.symbol_Q()) return new MalList(core.QUOTE, ast);
        if(!ast.list_Q()) return ast;

        MalList list = ast.getMalList();
        if(list.get(0).equals(core.UNQUOTE)) return list.get(1);

        MalList result = new MalList();
        for(int x=list.size()-1;x>=0;x--){
            MalType elt = list.get(x);
            if(elt.list_Q() && elt.getMalList().get(0).equals(core.SPLICE_UNQUOTE))
                result = new MalList(core.CONCAT, elt.getMalList().get(1), result);
            else
                result = new MalList(core.CONS, quasiquote(elt), result);
        }
        return result;
    }

    private static boolean isMacroCall(MalType ast, Env env){
        if(!ast.list_Q()) return false;

        MalType key = ast.getMalList().get(0);
        if(env.find(key) == null) return false;

        MalType val = env.get(key);
        if(!val.function_Q()) return false;

        return val.getMalFunctionImpl().isMacro;
    }

    private static MalType macroExpand(MalType ast, Env env){
        while(isMacroCall(ast, env))
            ast = env.get(ast.getMalList().get(0)).getMalFunctionImpl().apply(ast.getMalList().rest());
        
        return ast;
    }

    private static String PRINT(MalType input){
        return printer._pr_str(input.toString(), false);
    }
}