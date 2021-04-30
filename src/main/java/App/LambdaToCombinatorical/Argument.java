package App.LambdaToCombinatorical;

import App.Token.Token;

import java.util.ArrayList;

public class Argument {

    private ArrayList<Token> arg;
    private int start;
    private int end;

    public Argument(Token arg, int start) {
        this.arg = new ArrayList<>();
        this.arg.add(arg);
        this.start = start;
        this.end = start;
    }

    public void group(Argument arg) {
        this.arg.addAll(arg.getArg());
        this.end = arg.end;
    }

    public void add(Token arg, int end) {
        this.arg.add(arg);
        this.end = end;
    }

    public String getName() {
        if (arg.size() == 1) {
            return arg.get(0).getValue();
        }
        return null;
    }

    public int size() {
        return arg.size();
    }

    public Token get(int i) {
        return arg.get(i);
    }


    public ArrayList<Token> getArg() {
        return arg;
    }

    public ArrayList<Token> getTokens() {
        return arg;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return "(" + start + "; " + end + ") " + arg;
    }
}
