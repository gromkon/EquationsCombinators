package App.Lambda.Simplify;

import App.Token.Token;

import java.util.ArrayList;

public class Stack {

    private int level;
    private ArrayList<Token> tokens;

    public Stack() {
        level = 0;
        tokens = new ArrayList<>();
    }

    public Stack(int level) {
        this.level = level;
        tokens = new ArrayList<>();
    }

    public Stack(int level, ArrayList<Token> tokens) {
        this.level = level;
        this.tokens = tokens;
    }

    public int size() {
        return tokens.size();
    }

    public Token get(int i) {
        return tokens.get(i);
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public ArrayList<Token> getTokens() {
        return tokens;
    }

    public void add(Token token) {
        tokens.add(token);
    }

    public void add(ArrayList<Token> tokens) {
        tokens.addAll(tokens);
    }

    @Override
    public String toString() {
        return "(" + level + ") " + tokens;
    }
}
