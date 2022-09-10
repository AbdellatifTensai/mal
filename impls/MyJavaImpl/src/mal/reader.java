package mal;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mal.types.MalHashMap;
import mal.types.MalInteger;
import mal.types.MalList;
import mal.types.MalString;
import mal.types.MalSymbol;
import mal.types.MalType;
import mal.types.MalVector;

public class reader{

    public static MalType read_str(String input){
        String[] tokens = tokenize(input);
        Reader reader = new Reader(tokens, 0); 
        return read_form(reader);
    }

    public static String[] tokenize(String input){
        List<String> tokens = new LinkedList<>();
        Pattern pattern = Pattern.compile("[\\s ,]*(~@|[\\[\\]{}()'`~@]|\"(?:[\\\\].|[^\\\\\"])*\"?|;.*|[^\\s \\[\\]{}()'\"`~@,;]*)");
        Matcher matcher = pattern.matcher(input);

        while(matcher.find()){
            String token = matcher.group(1);
            if(token != null && !token.equals("") && token.charAt(0) != ';')
                tokens.add(token);
        }
        return tokens.toArray(new String[0]);
    }

    public static MalType read_form(Reader rdr){
        MalType form;
        String token = rdr.peek();
        if(token.equals(null)) throw new RuntimeException("no input available");

        switch(token.charAt(0)){
            case'\'': rdr.next(); return new MalList(core.QUOTE, read_form(rdr));
            case '`': rdr.next(); return new MalList(core.QUASIQUOTE, read_form(rdr));
            case '~': if (token.equals("~")){ rdr.next(); return new MalList(core.UNQUOTE, read_form(rdr)); }
                      else{ rdr.next(); return new MalList(core.SPLICE_UNQUOTE, read_form(rdr)); }
            case '@': rdr.next(); return new MalList(new MalSymbol("deref"), read_form(rdr));
            case '(': form = read_list(rdr, new MalList(), '(', ')'); break;
            case ')': throw new RuntimeException("unexpected ')'");
            case '[': form = read_list(rdr, new MalVector(), '[' , ']'); break;
            case ']': throw new RuntimeException("unexpected ']'");
            case '{': form = new MalHashMap(read_list(rdr, new MalList(), '{', '}')); break;
            case '}': throw new RuntimeException("unexpected '}'");
            default : form = read_atom(rdr); break;
        }
        return form;
    }

    public static MalList read_list(Reader rdr, MalList list, char start, char end){
        while(rdr.hasNext()){
            String token = rdr.next();
            if(token == null) throw new RuntimeException("expected '"+end+"' instead of EOF");
            else if(token.charAt(0) == end) break;
            else list.add(read_form(rdr));
        }
        return list;
    }

    public static MalType read_atom(Reader rdr){
        String token = rdr.peek();
        Pattern pattern = Pattern.compile("(^-?[0-9]+$)|(^-?[0-9][0-9.]*$)|(^nil$)|(^true$)|(^false$)|^\"((?:[\\\\].|[^\\\\\"])*)\"$|^\"(.*)$|:(.*)|(^[^\"]*$)");
        Matcher matcher = pattern.matcher(token);
        String newToken;

        if(!matcher.find()) throw new RuntimeException("unrecognized token '"+token+"'");
        if((newToken=matcher.group(1)) != null) return new MalInteger(Integer.parseInt(newToken));
        else if(matcher.group(3) != null) return core.Nil;
        else if(matcher.group(4) != null) return core.True;
        else if(matcher.group(5) != null) return core.False;
        else if((newToken = matcher.group(6)) != null) return new MalString(printer.escape(newToken));
        else if(matcher.group(7) != null) throw new RuntimeException("expected '\"', got EOF");
        else if((newToken=matcher.group(8)) != null) return new MalString("\u029e"+newToken);
        else if((newToken=matcher.group(9)) != null) return new MalSymbol(newToken);
        else throw new RuntimeException("unrecognized '"+matcher.group(0)+"'");
    }

    private static class Reader{
        String[] tokens;
        int index;

        Reader(String[] tokens, int pos){
            this.tokens = tokens;
            this.index = 0;
        }

        boolean hasNext(){ return index < tokens.length; }

        String next(){ index++; return tokens[index]; }

        String peek(){ return hasNext()? tokens[index]: null; }
    }
}