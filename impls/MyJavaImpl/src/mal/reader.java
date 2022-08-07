package mal;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mal.types.MalInteger;
import mal.types.MalList;
import mal.types.MalString;
import mal.types.MalSymbol;
import mal.types.MalType;

public class reader{

    private reader(){ throw new RuntimeException("not supposed to be instansiated"); }

    public static MalType read_str(String input){
        String[] tokens = tokenize(input);
        Reader reader = new Reader(tokens, 0); 
        return read_form(reader);
    }

    public static String[] tokenize(String input){
        List<String> tokens = new LinkedList<>();
        Pattern pattern = Pattern.compile("[\\s,]*(~@|[\\[\\]{}()'`~@]|\"(?:[\\\\].|[^\\\\\"])*\"?|;.*|[^\\s \\[\\]{}()'\"`~@,;]*)");
        Matcher matcher = pattern.matcher(input);

        while(matcher.find()){
            String token = matcher.group(1);
            if(token != null && !token.equals("") && token.charAt(0) != ';')
                tokens.add(token);
        }
        return tokens.toArray(new String[0]);
    }

    public static MalType read_form(Reader rdr){
        char firstChar = rdr.peek().charAt(0);
        MalType form;
        if(firstChar == '(')
            form = read_list(rdr);
        else
            form = read_atom(rdr);
        return form;
    }

    public static MalType read_list(Reader rdr){
        MalList list = new MalList();

        while(rdr.hasNext()){
            String token = rdr.next();

            if(token == null)
                throw new RuntimeException("expected ')' at"+rdr.pos+" instead of EOF");

            else if(token.charAt(0) == ')')
                break;

            else
                list.add(read_form(rdr));
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
        else if((newToken = matcher.group(6)) != null) return new MalString(newToken);
        else if(matcher.group(7) != null) throw new RuntimeException("expected '\"', got EOF");
        else if((newToken=matcher.group(8)) != null) return new MalString("\u029e"+newToken);
        else if((newToken=matcher.group(9)) != null) return new MalSymbol(newToken);
        else throw new RuntimeException("unrecognized '"+matcher.group(0)+"'");
    }

    private static class Reader{
        String[] tokens;
        int pos;
        int index;

        Reader(String[] tokens, int pos){
            this.tokens = tokens;
            this.pos = pos;
            this.index = 0;
        }

        boolean hasNext(){ return index < tokens.length; }

        String next(){ index++; return tokens[index]; }

        String peek(){ return hasNext()? tokens[index]: null; }
    }
}