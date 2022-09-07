package mal;

class printer {
    
    private printer(){}

    public static String _pr_str(String value, boolean print_readably){
        if (value.length() > 0 && value.charAt(0) == '\u029e') return ":" + value.substring(1);
        else if (print_readably)  return value;
        else return escape(value);
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
}
