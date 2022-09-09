package mal;

import java.util.HashMap;
import java.util.Map;

import mal.types.IMalFunction;
import mal.types.MalList;
import mal.types.MalType;

public class env {

    public static class Env{
        Env outer; 
        Map<MalType,MalType> data;

        public Env(Env outer, MalList binds, MalList expres) {
            this.outer = outer;
            this.data = new HashMap<>();
            for(int x=0;x<binds.size();x++){
                if(binds.get(x).toString().charAt(0)=='&'){
                    data.put(binds.get(x+1), expres.subList(x, expres.size())); break;
                }
                else data.put(binds.get(x), expres.get(x));
            }
        }

        public Env(Env outer) {
            this.outer = outer;
            this.data = new HashMap<>();
        }

        public Env(Env outer, Map<MalType,IMalFunction> data){
            this.outer = outer;
            this.data = new HashMap<>(data);
        }

        void set(MalType key, MalType value){
            data.put(key, value);
        }

        Env find(MalType key){
            if(data.containsKey(key)) return this;
            else if(outer != null) return outer.find(key);
            else return null; 
        }

        MalType get(MalType key){
            Env temp = find(key);
            if(temp != null) return temp.data.get(key);
            throw new RuntimeException("the key '"+key+"' was not found!");
        }

        @Override public String toString(){ return "Env"; }
    }
}
