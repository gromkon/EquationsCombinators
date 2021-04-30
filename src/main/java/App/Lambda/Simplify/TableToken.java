package App.Lambda.Simplify;

import App.Token.Token;

public class TableToken {

    private int stackNum;
    private int pos;
    private Token token;

    public TableToken(int stackNum, int pos, Token token) {
        this.stackNum = stackNum;
        this.pos = pos;
        this.token = token;
    }

    public int getStackNum() {
        return stackNum;
    }

    public void setStackNum(int stackNum) {
        this.stackNum = stackNum;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

}
