package mal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;

public class types {

    public static interface MalType{
        default boolean list_Q()             { return false;}
        default boolean symbol_Q()           { return false;}
        default MalList getMalList()         { throw new RuntimeException("only MalList can implement this");    }
        default MalSymbol getMalSymbol()     { throw new RuntimeException("only MalSymbol can implement this");  }
        default MalFunction getMalFunction() { throw new RuntimeException("only MalFunction can implement this");}
        default String getString()           { throw new RuntimeException("only MalString can implement this");  }
        default int getInteger()             { throw new RuntimeException("only MalInteger can implement this"); }
    }

    public static class MalList implements MalType{
        List<MalType> malTypes;

        public MalList(){ this.malTypes = new ArrayList<>(); }
        public MalList(List<MalType> list){ this.malTypes = list; }

        MalType get(int i){
            MalType m;
            try{ m = malTypes.get(i); }
            catch(IndexOutOfBoundsException e){ m = null;}
            return m;
        }
        MalType remove(int i){ return malTypes.remove(i); }
        boolean isEmpty(){ return malTypes.isEmpty(); }
        MalList add(MalType m){ malTypes.add(m); return this;}
        MalList map(UnaryOperator<MalType> u){ malTypes.replaceAll(u); return this;}
        int size(){ return malTypes.size();}
        MalList tail(){ malTypes.remove(0); return this;}
        MalType getLast(){ return malTypes.get(malTypes.size() - 1); }

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

        public static MalInteger add(MalInteger... malInt){ return new MalInteger(malInt[0].val + malInt[1].val); }
        public static MalInteger sub(MalInteger... malInt){ return new MalInteger(malInt[0].val - malInt[1].val); }
        public static MalInteger div(MalInteger... malInt){ return new MalInteger(malInt[0].val / malInt[1].val); }
        public static MalInteger mul(MalInteger... malInt){ return new MalInteger(malInt[0].val * malInt[1].val); }

        @Override
        public String toString() { return "(" + val + ")"; }
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
        public String toString() { return "("+val+")"; }
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
        public String toString(){ return "(" + val + ")"; }
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
        public String toString() { return "("+val+")"; }
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

    public static interface MalFunction extends MalType{

        MalType apply(MalList t);

        default public MalFunction getMalFunction(){ return this; }
    }
}
