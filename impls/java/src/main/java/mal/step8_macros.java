package mal;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import mal.env.Env;
import mal.types.MalFunction;
import mal.types.MalHashMap;
import mal.types.MalList;
import mal.types.MalSymbol;
import mal.types.MalThrowable;
import mal.types.MalVal;
import mal.types.MalVector;

public class step8_macros {
    // read
    public static MalVal READ(String str) throws MalThrowable {
        return reader.read_str(str);
    }

    // eval
    public static Boolean starts_with(MalVal ast, String sym) {
        //  Liskov, forgive me
        if (ast instanceof MalList && !(ast instanceof MalVector) && ((MalList)ast).size() == 2) {
            MalVal a0 = ((MalList)ast).nth(0);
            return a0 instanceof MalSymbol && ((MalSymbol)a0).getName().equals(sym);
        }
        return false;
    }

    public static MalVal quasiquote(MalVal ast) {
        if ((ast instanceof MalSymbol || ast instanceof MalHashMap))
            return new MalList(new MalSymbol("quote"), ast);

        if (!(ast instanceof MalList))
            return ast;

        if (starts_with(ast, "unquote"))
            return ((MalList)ast).nth(1);

        MalVal res = new MalList();
        for (Integer i=((MalList)ast).size()-1; 0<=i; i--) {
            MalVal elt = ((MalList)ast).nth(i);
            if (starts_with(elt, "splice-unquote"))
                res = new MalList(new MalSymbol("concat"), ((MalList)elt).nth(1), res);
            else
                res = new MalList(new MalSymbol("cons"), quasiquote(elt), res);
        }
        if (ast instanceof MalVector)
            res = new MalList(new MalSymbol("vec"), res);
        return res;
    }

    public static Boolean is_macro_call(MalVal ast, Env env)
            throws MalThrowable {
        if (ast instanceof MalList) {
            MalVal a0 = ((MalList)ast).nth(0);
            if (a0 instanceof MalSymbol &&
                env.find(((MalSymbol)a0)) != null) {
                MalVal mac = env.get(((MalSymbol)a0));
                if (mac instanceof MalFunction &&
                    ((MalFunction)mac).isMacro()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static MalVal macroexpand(MalVal ast, Env env)
            throws MalThrowable {
        while (is_macro_call(ast, env)) {
            MalSymbol a0 = (MalSymbol)((MalList)ast).nth(0);
            MalFunction mac = (MalFunction) env.get(a0);
            ast = mac.apply(((MalList)ast).rest());
        }
        return ast;
    }

    public static MalVal eval_ast(MalVal ast, Env env) throws MalThrowable {
        if (ast instanceof MalSymbol) {
            return env.get((MalSymbol)ast);
        } else if (ast instanceof MalList) {
            MalList old_lst = (MalList)ast;
            MalList new_lst = ast.list_Q() ? new MalList()
                                           : (MalList)new MalVector();
            for (MalVal mv : (List<MalVal>)old_lst.value) {
                new_lst.conj_BANG(EVAL(mv, env));
            }
            return new_lst;
        } else if (ast instanceof MalHashMap) {
            MalHashMap new_hm = new MalHashMap();
            Iterator it = ((MalHashMap)ast).value.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry)it.next();
                new_hm.value.put(entry.getKey(), EVAL((MalVal)entry.getValue(), env));
            }
            return new_hm;
        } else {
            return ast;
        }
    }

    public static MalVal EVAL(MalVal orig_ast, Env env) throws MalThrowable {
        MalVal a0, a1,a2, a3, res;
        MalList el;

        while (true) {

        //System.out.println("EVAL: " + printer._pr_str(orig_ast, true));
        if (!orig_ast.list_Q()) {
            return eval_ast(orig_ast, env);
        }
        if (((MalList)orig_ast).size() == 0) { return orig_ast; }

        // apply list
        MalVal expanded = macroexpand(orig_ast, env);
        if (!expanded.list_Q()) {
            return eval_ast(expanded, env);
        }
        MalList ast = (MalList) expanded;
        if (ast.size() == 0) { return ast; }
        a0 = ast.nth(0);
        String a0sym = a0 instanceof MalSymbol ? ((MalSymbol)a0).getName()
                                               : "__<*fn*>__";
        switch (a0sym) {
        case "def!":
            a1 = ast.nth(1);
            a2 = ast.nth(2);
            res = EVAL(a2, env);
            env.set(((MalSymbol)a1), res);
            return res;
        case "let*":
            a1 = ast.nth(1);
            a2 = ast.nth(2);
            MalSymbol key;
            MalVal val;
            Env let_env = new Env(env);
            for(int i=0; i<((MalList)a1).size(); i+=2) {
                key = (MalSymbol)((MalList)a1).nth(i);
                val = ((MalList)a1).nth(i+1);
                let_env.set(key, EVAL(val, let_env));
            }
            orig_ast = a2;
            env = let_env;
            break;
        case "quote":
            return ast.nth(1);
        case "quasiquoteexpand":
            return quasiquote(ast.nth(1));
        case "quasiquote":
            orig_ast = quasiquote(ast.nth(1));
            break;
        case "defmacro!":
            a1 = ast.nth(1);
            a2 = ast.nth(2);
            res = EVAL(a2, env);
            res = res.copy();
            ((MalFunction)res).setMacro();
            env.set((MalSymbol)a1, res);
            return res;
        case "macroexpand":
            a1 = ast.nth(1);
            return macroexpand(a1, env);
        case "do":
            eval_ast(ast.slice(1, ast.size()-1), env);
            orig_ast = ast.nth(ast.size()-1);
            break;
        case "if":
            a1 = ast.nth(1);
            MalVal cond = EVAL(a1, env);
            if (cond == types.Nil || cond == types.False) {
                // eval false slot form
                if (ast.size() > 3) {
                    orig_ast = ast.nth(3);
                } else {
                    return types.Nil;
                }
            } else {
                // eval true slot form
                orig_ast = ast.nth(2);
            }
            break;
        case "fn*":
            final MalList a1f = (MalList)ast.nth(1);
            final MalVal a2f = ast.nth(2);
            final Env cur_env = env;
            return new MalFunction (a2f, (mal.env.Env)env, a1f) {
                public MalVal apply(MalList args) throws MalThrowable {
                    return EVAL(a2f, new Env(cur_env, a1f, args));
                }
            };
        default:
            el = (MalList)eval_ast(ast, env);
            MalFunction f = (MalFunction)el.nth(0);
            MalVal fnast = f.getAst();
            if (fnast != null) {
                orig_ast = fnast;
                env = f.genEnv(el.slice(1));
            } else {
                return f.apply(el.rest());
            }
        }

        }
    }

    // print
    public static String PRINT(MalVal exp) {
        return printer._pr_str(exp, true);
    }

    // repl
    public static MalVal RE(Env env, String str) throws MalThrowable {
        return EVAL(READ(str), env);
    }

    public static void main(String[] args) throws MalThrowable {
        String prompt = "user> ";

        final Env repl_env = new Env(null);

        // core.java: defined using Java
        for (String key : core.ns.keySet()) {
            repl_env.set(new MalSymbol(key), core.ns.get(key));
        }
        repl_env.set(new MalSymbol("eval"), new MalFunction() {
            public MalVal apply(MalList args) throws MalThrowable {
                return EVAL(args.nth(0), repl_env);
            }
        });
        // MalList _argv = new MalList();
        // for (Integer i=1; i < args.length; i++) {
        //     _argv.conj_BANG(new MalString(args[i]));
        // }
        // repl_env.set(new MalSymbol("*ARGV*"), _argv);


        // core.mal: defined using the language itself
        RE(repl_env, "(def! not (fn* (a) (if a false true)))");
        RE(repl_env, "(def! load-file (fn* (f) (eval (read-string (str \"(do \" (slurp f) \"\nnil)\")))))");
        RE(repl_env, "(defmacro! cond (fn* (& xs) (if (> (count xs) 0) (list 'if (first xs) (if (> (count xs) 1) (nth xs 1) (throw \"odd number of forms to cond\")) (cons 'cond (rest (rest xs)))))))");
        
        // Integer fileIdx = 0;
        // if (args.length > 0 && args[0].equals("--raw")) {
        //     readline.mode = readline.Mode.JAVA;
        //     fileIdx = 1;
        // }
        // if (args.length > fileIdx) {
        //     RE(repl_env, "(load-file \"" + args[fileIdx] + "\")");
        //     return;
        // }
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();
            if(input.equals("exit")){ scanner.close(); break; }

            try{System.out.println(
                PRINT(RE(repl_env, input)));}
            catch(Exception e){ System.out.println(e); }
            // String line;
            // try {
            //     line = readline.readline(prompt);
            //     if (line == null) { continue; }
            // } catch (readline.EOFException e) {
            //     break;
            // } catch (IOException e) {
            //     System.out.println("IOException: " + e.getMessage());
            //     break;
            // }
            // try {
            //     System.out.println(PRINT(RE(repl_env, line)));
            // } catch (MalContinue e) {
            //     continue;
            // } catch (MalThrowable t) {
            //     System.out.println("Error: " + t.getMessage());
            //     continue;
            // } catch (Throwable t) {
            //     System.out.println("Uncaught " + t + ": " + t.getMessage());
            //     continue;
            // }
        }
    }
}
