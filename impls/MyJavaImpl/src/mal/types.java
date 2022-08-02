package mal;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class types {

    public static final MalConst Nil   = new MalConst("Nil");
    public static final MalConst True  = new MalConst("True");
    public static final MalConst False = new MalConst("False");

    public static interface MalType{}

    public static class MalList implements MalType, Collector<MalType,LinkedList<MalType>,MalList>{
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

        MalType mutate(Function<MalType,MalType> f){
            return malTypes.stream().map(f).collect(this);
        }

        boolean isEmpty(){
            return malTypes.isEmpty();
        }

        @Override
        public String toString() {
            return Arrays.toString(malTypes.toArray());
        }

        @Override
        public Supplier<LinkedList<MalType>> supplier(){ return LinkedList::new; }
        @Override
        public BiConsumer<LinkedList<MalType>, MalType> accumulator(){ return LinkedList::add; }
        @Override
        public BinaryOperator<LinkedList<MalType>> combiner(){ return (l1,l2) -> {l1.addAll(l2); return l1;}; }
        @Override
        public Function<LinkedList<MalType>, MalList> finisher(){ return (i)->new MalList(i); }
        @Override
        public Set<Characteristics> characteristics(){ return Collections.emptySet(); }


    }

    public static class MalInteger implements MalType{
        int val;
        public MalInteger(int malInt) { this.val = malInt; }

        public MalInteger add(MalInteger malInt){ this.val += malInt.val; return this; }
        public MalInteger sub(MalInteger malInt){ this.val -= malInt.val; return this; }
        public MalInteger mul(MalInteger malInt){ this.val *= malInt.val; return this; }
        public MalInteger div(MalInteger malInt){ this.val /= malInt.val; return this; }

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
    }

    public static class MalSymbol implements MalType{
        String val;
        public MalSymbol(String malSymbol) { this.val = malSymbol; }
        @Override
        public String toString() { return "("+val+")"; }
    }

    public static class MalFunction implements MalType{
        Function<MalList,MalType> malFunction;

        public MalFunction(Function<MalList, MalType> malFunction) {
            this.malFunction = malFunction;
        }

    }
}
