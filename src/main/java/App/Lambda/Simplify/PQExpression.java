package App.Lambda.Simplify;

import App.Token.Token;

import java.util.ArrayList;

public class PQExpression {

    private ArrayList<TableToken> p;
    private ArrayList<TableToken> q;
    private int level;
    private ArrayList<Stack> table;

    public PQExpression(ArrayList<TableToken> p, ArrayList<TableToken> q, int level) {
        this.p = p;
        this.q = q;
        this.level = level;
    }

    public void setTable(ArrayList<Stack> table) {
        this.table = table;
    }

    public ArrayList<TableToken> getP() {
        return p;
    }

    public ArrayList<TableToken> getQ() {
        return q;
    }

    public int getLevel() {
        return level;
    }

    public void application() {
        ArrayList<Token> qTokens = new ArrayList<>();
        for (TableToken qToken: q) {
            qTokens.add(qToken.getToken());
        }

        String lambdaName = p.get(1).getToken().getValue();
        // Заменя
        for (int i = p.size() - 1; i >= 2; i--) {
            TableToken tt = p.get(i);
            String tokenName = tt.getToken().getValue();
            if (tokenName.equals(lambdaName)) {
                int stackNum = tt.getStackNum();
                int pos = tt.getPos();
                table.get(stackNum).getTokens().remove(pos);
                table.get(stackNum).getTokens().addAll(pos, qTokens);
            }
        }
        // Удаляем "\x."
        for (int i = 2; i >= 0; i--) {
            TableToken tt = p.get(i);
            int stackNum = tt.getStackNum();
            int pos = tt.getPos();
            table.get(stackNum).getTokens().remove(pos);
        }
        // Удаляем q
        for (int i = q.size() - 1; i >= 0; i--) {
            TableToken tt = q.get(i);
            int stackNum = tt.getStackNum();
            int pos = tt.getPos();
            table.get(stackNum).getTokens().remove(pos);
        }
    }
}
