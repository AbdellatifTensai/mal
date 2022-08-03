package mal;

import java.util.HashMap;
import java.util.Map;

import mal.types.MalSymbol;
import mal.types.MalType;

public class env {

    public static class Env{
        Env outer; 
        Map<String,MalType> data;

        public Env(Env outer) {
            this.outer = outer;
            this.data = new HashMap<>();
        }

        void set(String key, MalType value){
            data.put(key, value);
        }

        Env find(String key){
            if(data.containsKey(key)) return this;
            else if(!outer.equals(null)) outer.find(key);
            return null; 
        }

        MalType get(String key){
            Env temp = find(key);
            if(!temp.equals(null)) return temp.data.get(key);
            throw new RuntimeException("the key '"+key+"' was not found!");
        }
    }
}
