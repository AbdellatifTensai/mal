package mal;

import java.util.HashMap;
import java.util.Map;

import mal.types.IMalFunction;
import mal.types.MalConst;
import mal.types.MalInteger;
import mal.types.MalSymbol;
import mal.types.MalType;

public class core {
    public static final MalConst Nil   = new MalConst("Nil");
    public static final MalConst True  = new MalConst("True");
    public static final MalConst False = new MalConst("False");

    public static final Map<MalType,IMalFunction> NS = new HashMap<>();
    static{
        NS.put(new MalSymbol("+"),      args -> new MalInteger(args.getMalList().get(0).getInteger() + args.getMalList().get(1).getInteger())); 
        NS.put(new MalSymbol("-"),      args -> new MalInteger(args.getMalList().get(0).getInteger() - args.getMalList().get(1).getInteger()));
        NS.put(new MalSymbol("*"),      args -> new MalInteger(args.getMalList().get(0).getInteger() * args.getMalList().get(1).getInteger()));
        NS.put(new MalSymbol("/"),      args -> new MalInteger(args.getMalList().get(0).getInteger() / args.getMalList().get(1).getInteger()));
        NS.put(new MalSymbol("prn"),    args -> {System.out.println(args); return Nil;}                                                      );
        NS.put(new MalSymbol("list"),   args -> args                                                                                         );
        NS.put(new MalSymbol("list?"),  args -> args.get(0).list_Q()? True: False                                                            );
        NS.put(new MalSymbol("empty?"), args -> args.getMalList().isEmpty()? True: False                                                     );
        NS.put(new MalSymbol("count"),  args -> new MalInteger(args.get(0).getMalList().size())                                              );
        NS.put(new MalSymbol("="),      args -> args.get(0).equals(args.get(1))? True: False                                                 );
        NS.put(new MalSymbol("<"),      args -> args.get(0).getInteger() < args.get(1).getInteger()? True: False                             );
        NS.put(new MalSymbol(">"),      args -> args.get(0).getInteger() > args.get(1).getInteger()? True: False                             );
        NS.put(new MalSymbol("<="),     args -> args.get(0).getInteger() <= args.get(1).getInteger()? True: False                            );
        NS.put(new MalSymbol(">="),     args -> args.get(0).getInteger() >= args.get(1).getInteger()? True: False                            );
    }

        
}