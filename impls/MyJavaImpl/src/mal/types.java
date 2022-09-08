package mal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mal.env.Env;

public class types {

    public static interface MalType{
        default boolean list_Q()             { return false;}
        default boolean symbol_Q()           { return false;}
        default boolean atom_Q()             { return false;}
        default boolean function_Q()         { return false;}
        default MalList getMalList()         { throw new RuntimeException("only MalList can implement this");    }
        default MalSymbol getMalSymbol()     { throw new RuntimeException("only MalSymbol can implement this");  }
        default IMalFunction getMalFunction(){ throw new RuntimeException("only MalFunction can implement this");}
        default String getString()           { throw new RuntimeException("only MalString can implement this");  }
        default int getInteger()             { throw new RuntimeException("only MalInteger can implement this"); }
        default MalAtom getMalAtom()         { throw new RuntimeException("only MalAtom can implement this");    }
        default MalFunction getMalFunctionImpl(){ throw new RuntimeException("only THE MalFunction implementation of IMalFunction can call this");}
    }

    public static class MalList implements MalType{
        List<MalType> malTypes;

        public MalList()                  { this.malTypes = new ArrayList<>(); }
        public MalList(List<MalType> list){ this.malTypes = new ArrayList<>(list); }
        public MalList(MalList list)      { this.malTypes = new ArrayList<>(list.malTypes);}
        public MalList(MalType... ms)     { this.malTypes = new ArrayList<>(); for(MalType m:ms) malTypes.add(m); }

        MalType get(int i){
            try{ return malTypes.get(i); }
            catch(IndexOutOfBoundsException e){ return core.Nil;}
        }
        MalType remove(int i)                   { return malTypes.remove(i); }
        boolean isEmpty()                       { return malTypes.isEmpty(); }
        MalList add(MalType m)                  { malTypes.add(m); return this;}
        MalList add(int i, MalType m)           { malTypes.add(i, m); return this;}
        int     size()                          { return malTypes.size();}
        MalList subList(int a, int z)           { return new MalList(malTypes.subList(a, z));}
        MalType getLast()                       { return malTypes.get(malTypes.size() - 1); }
        MalList rest()                          { return new MalList(malTypes.subList(1, malTypes.size()));}
        MalList addAll(MalList m)               { malTypes.addAll(m.malTypes); return this;}

        @Override
        public MalList getMalList(){ return this; }
        @Override
        public boolean list_Q(){ return true; }
        @Override
        public String toString(){ return Arrays.toString(malTypes.toArray()); }
        @Override
        public boolean equals(Object obj) {
            if(!(obj instanceof MalList)) return false;
            MalList l = (MalList) obj;
            if(this.size() != l.size()) return false;
            int size = malTypes.size();
            int eq = 0;
            for(int x=0;x<size;x++) eq += malTypes.get(x).equals(l.malTypes.get(x)) ? 1: -1; 
            return eq == size; 
        }
        
    }

    public static class MalInteger implements MalType{
        int val;
        public MalInteger(int malInt) { this.val = malInt; }

        @Override
        public String toString() { return ""+val; }
        @Override
        public int getInteger(){ return val; }

        @Override
        public boolean equals(Object obj) {
            if(!(obj instanceof MalInteger)) return false;
            MalInteger i = (MalInteger) obj;
            return this.val == i.val;
        }
        
    }

    public static class MalConst implements MalType{
        String val;
        public MalConst(String malConst){ this.val = malConst; }
        @Override
        public String toString() { return val; }
        @Override
        public boolean equals(Object obj) {
            if(!(obj instanceof MalConst)) return false;
            MalConst c = (MalConst) obj;
            return this.val.equals(c.val);
        }
        
    }

    public static class MalString implements MalType{
        String val;
        public MalString(String malString){ this.val = malString; }
        @Override
        public String toString(){ return val; }
        @Override
        public String getString(){ return val; }
        @Override
        public boolean equals(Object obj) {
            if(!(obj instanceof MalString)) return false;
            MalString s = (MalString) obj;
            return this.val.equals(s.val);
        }
        
    }

    public static class MalSymbol implements MalType{
        String val;
        public MalSymbol(String malSymbol) { this.val = malSymbol; }

        @Override
        public String toString() { return val; }
        @Override
        public boolean symbol_Q() { return true; }
        @Override
        public MalSymbol getMalSymbol() { return this; }
        @Override
        public int hashCode(){ return val.hashCode(); }

        @Override
        public boolean equals(Object obj) {
            if(!(obj instanceof MalSymbol)) return false;
            MalSymbol sym = (MalSymbol) obj;
            return this.val.equals(sym.val);
        }
    }

    public static interface IMalFunction extends MalType{
        MalType apply(MalList arguments);
        @Override default IMalFunction getMalFunction(){ return this; }
    }

    abstract public static class MalFunction implements IMalFunction{
        MalType params;
        MalType body;
        Env env;
        boolean isMacro;

        public MalFunction(MalList ast, Env env){
            this.params = ast.get(1);
            this.body = ast.get(2);
            this.env = env;
            this.isMacro = false;
        }
        @Override
        public MalFunction getMalFunctionImpl(){ return this; }
        @Override
        public String toString(){ return "MalFunction"; }
        @Override
        public boolean function_Q(){ return true; }
        public MalFunction setMacro(){ this.isMacro = true; return this;}
    }

    public static class MalAtom implements MalType{
        MalType val;
        public MalAtom(MalType val){ this.val = val; }
        @Override public String toString(){ return "*"+this.val.toString(); }
        @Override public boolean atom_Q(){ return true; }
        @Override public MalAtom getMalAtom(){ return this; }
        public MalType value(){ return this.val; }
    }

    public static class MalException implements MalType{
        RuntimeException e;
        public MalException(RuntimeException e){ this.e = e; }
        @Override public String toString(){ return this.e.toString(); }
        public String msg(){ return this.e.getMessage(); }
    }

}
