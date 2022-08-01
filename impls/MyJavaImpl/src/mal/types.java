package mal;
import java.util.Arrays;
import java.util.LinkedList;

public class types {

    public static final MalConst Nil   = new MalConst("Nil");
    public static final MalConst True  = new MalConst("True");
    public static final MalConst False = new MalConst("False");

    public static abstract class MalType{}

    public static class MalList extends MalType{
        LinkedList<MalType> malTypes;

        public MalList() {
            this.malTypes = new LinkedList<>();
        }

        MalList addAll(MalType... malTypes){
            for(MalType m : malTypes) this.malTypes.add(m);
            return this;
        }

        @Override
        public String toString() {
            return Arrays.toString(malTypes.toArray());
        }

    }

    public static class MalInteger extends MalType{
        int malInt;
        public MalInteger(int malInt) { this.malInt = malInt; }
        @Override
        public String toString() { return "(" + malInt + ")"; }
    }

    public static class MalConst extends MalType{
        String malConst;
        private MalConst(String malConst){ this.malConst = malConst; }
        @Override
        public String toString() { return "("+malConst+")"; }
    }

    public static class MalString extends MalType{
        String malString;
        public MalString(String malString){ this.malString = malString; }
        @Override
        public String toString(){ return "(" + malString + ")"; }
    }

    public static class MalSymbol extends MalType{
        String malSymbol;
        public MalSymbol(String malSymbol) { this.malSymbol = malSymbol; }
        @Override
        public String toString() { return "("+malSymbol+")"; }
    }
}
