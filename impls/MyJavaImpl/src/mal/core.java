package mal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import mal.types.IMalFunction;
import mal.types.MalAtom;
import mal.types.MalConst;
import mal.types.MalInteger;
import mal.types.MalList;
import mal.types.MalString;
import mal.types.MalSymbol;
import mal.types.MalType;

public class core {

    public static final MalConst Nil             = new MalConst("Nil");
    public static final MalConst True            = new MalConst("True");
    public static final MalConst False           = new MalConst("False");
    public static final MalSymbol QUOTE          = new MalSymbol("quote");
    public static final MalSymbol UNQUOTE        = new MalSymbol("unquote");
    public static final MalSymbol SPLICE_UNQUOTE = new MalSymbol("splice-unquote");
    public static final MalSymbol QUASIQUOTE     = new MalSymbol("quasiquote");
    public static final MalSymbol CONS           = new MalSymbol("cons");
    public static final MalSymbol CONCAT         = new MalSymbol("concat");

    public static final Map<MalType,IMalFunction> NS = new HashMap<>();
    static{
        NS.put(new MalSymbol("+"),           args -> new MalInteger(args.getMalList().get(0).getInteger() + args.getMalList().get(1).getInteger())); 
        NS.put(new MalSymbol("-"),           args -> new MalInteger(args.getMalList().get(0).getInteger() - args.getMalList().get(1).getInteger()));
        NS.put(new MalSymbol("*"),           args -> new MalInteger(args.getMalList().get(0).getInteger() * args.getMalList().get(1).getInteger()));
        NS.put(new MalSymbol("/"),           args -> new MalInteger(args.getMalList().get(0).getInteger() / args.getMalList().get(1).getInteger()));
        NS.put(new MalSymbol("prn"),         args -> {System.out.println(args); return Nil;}                                                      );
        NS.put(new MalSymbol("list"),        args -> args                                                                                         );
        NS.put(new MalSymbol("list?"),       args -> args.get(0).list_Q()? True: False                                                            );
        NS.put(new MalSymbol("empty?"),      args -> args.getMalList().isEmpty()? True: False                                                     );
        NS.put(new MalSymbol("count"),       args -> new MalInteger(args.get(0).getMalList().size())                                              );
        NS.put(new MalSymbol("="),           args -> args.get(0).equals(args.get(1))? True: False                                                 );
        NS.put(new MalSymbol("<"),           args -> args.get(0).getInteger() < args.get(1).getInteger()? True: False                             );
        NS.put(new MalSymbol(">"),           args -> args.get(0).getInteger() > args.get(1).getInteger()? True: False                             );
        NS.put(new MalSymbol("<="),          args -> args.get(0).getInteger() <= args.get(1).getInteger()? True: False                            );
        NS.put(new MalSymbol(">="),          args -> args.get(0).getInteger() >= args.get(1).getInteger()? True: False                            );
        NS.put(new MalSymbol("read-string"), args -> reader.read_str(args.get(0).getString())                                                     );
        NS.put(new MalSymbol("atom"),        args -> new MalAtom(args.get(0))                                                                     );
        NS.put(new MalSymbol("atom?"),       args -> args.get(0).atom_Q()? True: False                                                            );
        NS.put(new MalSymbol("deref"),       args -> args.get(0).getMalAtom().val                                                                 );
        NS.put(new MalSymbol("reset!"),      args ->{args.get(0).getMalAtom().val = args.get(1); return args.get(1);}                             );   
        NS.put(new MalSymbol("cons"),        args -> new MalList(args.get(1).getMalList()).add(0, args.get(0))                                    );
        NS.put(new MalSymbol("nth"),         args -> args.get(0).getMalList().malTypes.get(args.get(1).getInteger())  /*don't bypass exception*/  );
        NS.put(new MalSymbol("first"),       args -> args.get(0).getMalList().get(0)                                                              ); 
        NS.put(new MalSymbol("rest"),        args -> args.get(0).getMalList().rest()                                                              );
        NS.put(new MalSymbol("throw"),       args ->{throw new RuntimeException(args.get(0).getString());}                                        ); 
        NS.put(new MalSymbol("nil?"),        args -> args.get(0).equals(Nil)? True: False                                                         );
        NS.put(new MalSymbol("true?"),       args -> args.get(0).equals(True)? True: False                                                        );
        NS.put(new MalSymbol("false?"),      args -> args.get(0).equals(False)? True: False                                                       );
        NS.put(new MalSymbol("symbol?"),     args -> args.get(0).symbol_Q()? True: False                                                          );
        NS.put(new MalSymbol("symbol"),      args -> new MalSymbol(args.get(0).getString())                                                       );
        NS.put(new MalSymbol("keyword"),     args -> new MalSymbol(args.get(0).toString())                                                        );
        NS.put(new MalSymbol("keyword?"),    args -> args.get(0).symbol_Q()? True: False                                                          );
        NS.put(new MalSymbol("sequential?"), args -> args.get(0).list_Q()? True: False                                                      );

        NS.put(new MalSymbol("str"),         args -> new MalString(printer._pr_str(args.malTypes.stream().map(x->x.toString()).collect(Collectors.joining("")),false)));
        NS.put(new MalSymbol("pr-str"),      args -> new MalString(printer._pr_str(args.malTypes.stream().map(x->x.toString()).collect(Collectors.joining(" ")),true)));
        NS.put(new MalSymbol("map"),         args -> new MalList(args.get(1).getMalList().malTypes.stream().map(m->args.get(0).getMalFunction().apply(new MalList(m))).collect(Collectors.toList())));

        NS.put(new MalSymbol("apply"), args -> {
            MalList list = new MalList();
            for(int x=1;x<args.size()-1;x++) list.add(args.get(x));
            list.addAll(args.getLast().getMalList());
            return args.get(0).getMalFunction().apply(list);
        });
        NS.put(new MalSymbol("concat"), args -> {
                MalList list = new MalList();
                for(int x=0;x<args.size();x++) list.addAll(args.get(x).getMalList());
                return list;
        }); 
        NS.put(new MalSymbol("slurp"), args -> {
                MalString str;
                try{ str = new MalString(Files.lines(Paths.get(args.get(0).getString())).collect(Collectors.joining("\n"))); }
                catch(IOException e){ System.out.println(e.getMessage()); str = new MalString(""); }
                return str;
            }
        );
        NS.put(new MalSymbol("swap!"), args ->{
            MalAtom atom = args.get(0).getMalAtom();
            IMalFunction f = args.get(1).getMalFunction();

            if(!(args.size()>2)){ atom.val = f.apply(new MalList().add(atom.value())); return atom; }

            MalList new_args = new MalList();
            new_args.add(atom.value());
            for(int x=2;x<args.size();x++) new_args.add(args.get(x));
            atom.val = f.apply(new_args);
            
            return atom;
        });
    }

        
}