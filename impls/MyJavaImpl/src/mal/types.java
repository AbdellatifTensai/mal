package mal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import mal.env.Env;

public class types {

    public static interface MalType{
        default boolean list_Q()             { return false; }
        default boolean symbol_Q()           { return false; }
        default boolean atom_Q()             { return false; }
        default boolean function_Q()         { return false; }
        default boolean sequential_Q()       { return false; }
        default boolean hashmap_Q()          { return false; }
        default boolean vector_Q()           { return false; }
        default boolean number_Q()           { return false; }
        default boolean string_Q()           { return false; }
        default MalList getMalList()         { throw new RuntimeException("only MalList can implement this");    }
        default MalSymbol getMalSymbol()     { throw new RuntimeException("only MalSymbol can implement this");  }
        default IMalFunction getMalFunction(){ throw new RuntimeException("only MalFunction can implement this");}
        default String getString()           { throw new RuntimeException("only MalString can implement this");  }
        default int getInteger()             { throw new RuntimeException("only MalInteger can implement this"); }
        default MalAtom getMalAtom()         { throw new RuntimeException("only MalAtom can implement this");    }
        default MalHashMap getMap()          { throw new RuntimeException("only MalHashMap can implement this"); }
        default MalFunction getMalFunctionImpl(){ throw new RuntimeException("only THE MalFunction implementation of IMalFunction can call this");}
    }

    public static class MalList implements MalType{
        String start = "(", end = ")";
        List<MalType> malTypes;

        public MalList()                        { this.malTypes = new ArrayList<>(); }
        public MalList(Collection<MalType> list){ this.malTypes = new ArrayList<>(list); }
        public MalList(MalList list)            { this.malTypes = new ArrayList<>(list.malTypes);}
        public MalList(MalType... ms)           { this.malTypes = new ArrayList<>(); for(MalType m:ms) malTypes.add(m); }

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
        public boolean sequential_Q(){ return true; }
        @Override
        public String toString(){ return printer.printList(this, true, " , ", start, end); }
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

    public static class MalVector extends MalList{
        MalVector()                 { this.start = "["; this.end = "]"; this.malTypes = new Vector<>();           }
        MalVector(MalList ms)       { this.start = "["; this.end = "]"; this.malTypes = new Vector<>(ms.malTypes);}
        MalVector(List<MalType> ms) { this.start = "["; this.end = "]"; this.malTypes = new Vector<>(ms);         }
        MalVector(MalType... ms)    { super(ms); this.start = "["; this.end = "]";                                }

        MalVector subVector(int a, int b){ return new MalVector(malTypes.subList(a, b)); }

        @Override public boolean list_Q() { return false; }
        @Override public boolean vector_Q(){ return true; }
        @Override public MalList getMalList(){ return this; }
    }

    public static class MalHashMap implements MalType{
        Map<MalType,MalType> map;

        MalHashMap()                        { this.map = new HashMap<>();              }
        MalHashMap(MalHashMap map)          { this.map = new HashMap<>(map.map);       }
        MalHashMap(MalList list)            { this.map = new HashMap<>(); assoc(list); }
        MalHashMap(MalType... ms)           { this.map = new HashMap<>(); assoc(ms);   }

        MalHashMap put(MalType k, MalType v) { this.map.put(k, v); return this;}
        MalHashMap assoc(MalType... ms) { for(int x=0;x<ms.length;x+=2) map.put(ms[x], ms[x+1]);         return this; }
        MalHashMap assoc(MalList ms)    { for(int x=0;x<ms.size();x+=2) map.put(ms.get(x), ms.get(x+1)); return this; }
        MalHashMap dissoc(MalList ms)   { ms.malTypes.forEach(m -> this.map.remove(m)); return this;                  }
        MalType get(MalType key)        { MalType k = map.get(key); return k != null? k: core.Nil; }
        MalList keys(){ return new MalList(map.keySet());}
        MalList vals(){ return new MalList(map.values());}
        int size(){ return this.map.size(); }

        @Override public String toString(){ return printer.printMap(this, false); }
        @Override public boolean hashmap_Q(){ return true; }
        @Override public MalHashMap getMap(){ return this; }
    }

    public static class MalInteger implements MalType{
        int val;
        public MalInteger(int malInt) { this.val = malInt; }

        @Override public String toString()  { return ""+val;}
        @Override public int getInteger()   { return val;   }
        @Override public boolean number_Q() { return true;  }
        @Override public boolean equals(Object obj) {
            if(!(obj instanceof MalInteger)) return false;
            MalInteger i = (MalInteger) obj;
            return this.val == i.val;
        }
    }

    public static class MalConst implements MalType{
        String val;
        public MalConst(String malConst){ this.val = malConst; }
        @Override public String toString() { return val; }
        @Override public boolean equals(Object obj) {
            if(!(obj instanceof MalConst)) return false;
            MalConst c = (MalConst) obj;
            return this.val.equals(c.val);
        }
        
    }

    public static class MalString implements MalType{
        String val;
        public MalString(String malString){ this.val = malString; }

        @Override public String toString()  { return val; }
        @Override public String getString() { return val; }
        @Override public boolean string_Q() { return true;}
        @Override public int hashCode(){ return val.hashCode(); }
        @Override public boolean equals(Object obj){
            if(!(obj instanceof MalString)) return false;
            MalString s = (MalString) obj;
            return this.val.equals(s.val);
        }
    }

    public static class MalSymbol implements MalType{
        String val;
        public MalSymbol(String malSymbol) { this.val = malSymbol; }

        @Override public String toString() { return val; }
        @Override public boolean symbol_Q() { return true; }
        @Override public MalSymbol getMalSymbol() { return this; }
        @Override public int hashCode(){ return val.hashCode(); }
        @Override public boolean equals(Object obj) {
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
