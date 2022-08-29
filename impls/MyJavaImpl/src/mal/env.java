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
            for(int x=0;x<binds.size();x++) data.put(binds.get(x), expres.get(x));
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
            else if(!outer.equals(null)) return outer.find(key);
            return null; 
        }

        MalType get(MalType key){
            Env temp = find(key);
            if(!temp.equals(null)) return temp.data.get(key);
            throw new RuntimeException("the key '"+key+"' was not found!");
        }

        @Override public String toString(){ return "Env"; }
    }
}
