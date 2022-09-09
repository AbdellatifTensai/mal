package mal;

import java.util.Map.Entry;

import mal.types.MalHashMap;
import mal.types.MalList;
import mal.types.MalType;

class printer {
    
    private printer(){}

    public static String _pr_str(MalType m, boolean print_readably){
        if(m.sequential_Q()) return printList(m.getMalList(),print_readably," ", m.getMalList().start, m.getMalList().end);
        else if(m.hashmap_Q()) return printMap(m.getMap(), print_readably);

        if (m.toString().length() > 0 && m.toString().charAt(0) == core.U029E) return ":" + m.toString().substring(1);
        else if (print_readably)  return "\""+m.toString()+"\"";
        else return escape(m.toString());
    }
    
    public static String escape(String s){
        return s.replace( "\\\\","\\")
                .replace( "\\t" ,"\t")
                .replace( "\\b" ,"\b")
                .replace( "\\n" ,"\n")
                .replace( "\\r" ,"\r")
                .replace( "\\f" ,"\f")
                .replace( "\\'" ,"\'")
                .replace( "\\\"","\"");
    }

    public static String printMap(MalHashMap map, boolean print_readably){
        StringBuilder s = new StringBuilder();
        s.append("{");
        for(Entry<MalType,MalType> e: map.map.entrySet()){
            s.append(_pr_str(e.getKey(), print_readably));
            s.append(" ");
            s.append(_pr_str(e.getValue(), print_readably));
        }
        s.append("}");
        return s.toString();
    }

    public static String printList(MalList list, boolean print_readably, String delim, String start, String end){
        StringBuilder s = new StringBuilder();
        for(int x=0;x<list.size();x++){
            s.append(_pr_str(list.get(x),print_readably));
            if(x != list.size()-1) s.append(delim);
        }
        return start + s.toString() + end;
    }
}
