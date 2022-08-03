package mal;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.function.Function;

public class types {

    public static final MalConst Nil   = new MalConst("Nil");
    public static final MalConst True  = new MalConst("True");
    public static final MalConst False = new MalConst("False");

    public static interface MalType{
        default boolean list_Q()  { return false;}
        default boolean symbol_Q(){ return false;}
        default LinkedList<MalType> getMalTypes(){ throw new RuntimeException("only MalList can implement this");    }
        default MalSymbol getMalSymbol()         { throw new RuntimeException("only MalSymbol can implement this");  }
        default MalFunction getMalFunction()     { throw new RuntimeException("only MalFunction can implement this");}
        default String getString()               { throw new RuntimeException("only MalString can implement this");  }
    }

    public static class MalList implements MalType{
        LinkedList<MalType> malTypes;

        public MalList() {
            this.malTypes = new LinkedList<>();
        }
        public MalList(LinkedList<MalType> list){
            this.malTypes = list;
        }

        MalList addAll(MalType... malTypes){
            for(MalType m : malTypes) this.malTypes.add(m);
            return this;
        }

        MalType getFirst(){ return malTypes.getFirst(); }
        MalType getSecond(){ return malTypes.get(1); }
        boolean isEmpty(){ return malTypes.isEmpty(); }

        @Override
        public LinkedList<MalType> getMalTypes(){ return malTypes; }
        @Override
        public boolean list_Q(){ return true; }
        @Override
        public String toString(){ return Arrays.toString(malTypes.toArray()); }

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
    }

    public static class MalConst implements MalType{
        String val;
        private MalConst(String malConst){ this.val = malConst; }
        @Override
        public String toString() { return "("+val+")"; }
    }

    public static class MalString implements MalType{
        String val;
        public MalString(String malString){ this.val = malString; }
        @Override
        public String toString(){ return "(" + val + ")"; }
        @Override
        public String getString(){ return val; }
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
    }

    public static class MalFunction implements MalType{
        Function<MalList,MalType> malFunction;

        public MalFunction(Function<MalList, MalType> malFunction) {
            this.malFunction = malFunction;
        }
        public MalType apply(MalList t){
            return malFunction.apply(t);
        }
        @Override
        public MalFunction getMalFunction(){ return this; }
    }
}
