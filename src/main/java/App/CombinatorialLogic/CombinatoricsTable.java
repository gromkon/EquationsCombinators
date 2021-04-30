package App.CombinatorialLogic;

import App.Token.Token;
import App.Token.TokenType;

import java.util.ArrayList;
import java.util.List;

public class CombinatoricsTable {

    private ArrayList<Token> tokens;
    private ArrayList<ArrayList<Token>> table;

    private static int lNum = 1;

    public CombinatoricsTable(ArrayList<Token> tokens) {
        this.tokens = tokens;
        table = new ArrayList<>();

        fillTable(tokens);

        simplify();
    }

    public ArrayList<Token> getSimplify() {
        return table.get(0);
    }


    // Заполняет таблицу
    private void fillTable(List<Token> tokens) {
        ArrayList<Token> stack = new ArrayList<>();
        table.add(stack);
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);

            if (token.isLeftBracket()) {
                int start = i + 1;
                int count = 1;
                while (count > 0) {
                    i++;
                    token = tokens.get(i);
                    if (token.isRightBracket()) {
                        count--;
                    } else if (token.isLeftBracket()) {
                        count++;
                    }
                }

                stack.add(new Token("L" + lNum++, TokenType.STACK));

                fillTable(tokens.subList(start, i));
            } else {
                stack.add(token);
            }

        }
    }

    // Упрощает строку num
    private void simplify() {
        for (int stackNum = table.size() - 1; stackNum >= 0; stackNum--) {
            ArrayList<Token> stack = table.get(stackNum);

            boolean simplified = true;
            while (simplified) {
                simplified = false;
                Token token = stack.get(0);
                if (token.isRuleI()) {
                    simplified = doRuleI(stack.subList(0, stack.size()));
                } else if (token.isRuleK()) {
                    simplified = doRuleK(stack.subList(0, stack.size()));
                } else if (token.isRuleS()) {
                    simplified = doRuleS(stack.subList(0, stack.size()));
                }
            }

//            for (int i = 0; i < stack.size(); i++) {
//                Token token = stack.get(i);
//                if (token.isRuleI()) {
//                    if (doRuleI(stack.subList(i, stack.size()))) {
//                        i--;
//                    }
//                } else if (token.isRuleK()) {
//                   if (doRuleK(stack.subList(i, stack.size()))) {
//                       i--;
//                   }
//                } else if (token.isRuleS()) {
//                    if (doRuleS(stack.subList(i, stack.size()))) {
//                        i--;
//                    }
//                }
//            }

            substitute(stackNum);

        }
    }

    // Подставляем в stack с номером stackNum все правила
    private void substitute(int stackNum) {
        ArrayList<Token> stack = table.get(stackNum);
        for (int i = stack.size() - 1; i >= 0; i--) {
            Token token = stack.get(i);
            if (token.isStack()) {
                int num = token.getStackNum();
                stack.remove(i);
                stack.add(i, new Token(")", TokenType.RIGHT_BRACKET));
                stack.addAll(i, table.get(num));
                stack.add(i, new Token("(", TokenType.LEFT_BRACKET));
            }
        }
    }

    private boolean doRuleI(List<Token> stack) {
        if (stack.size() >= 2) {
            stack.remove(0);
            return true;
        }
        return false;
    }

    private boolean doRuleK(List<Token> stack) {
        if (stack.size() >= 3) {
            stack.remove(2);
            stack.remove(0);
            return true;
        }
        return false;
    }


    // TODO работает, только если (yz) не являются какими-либо L_num
    private boolean doRuleS(List<Token> stack) {
        if (stack.size() >= 4) {
            Token x = stack.get(1);
            Token y = stack.get(2);
            Token z = stack.get(3);

            stack.subList(0, 4).clear();

            stack.add(0, x);
            stack.add(1, z);
            stack.add(2, new Token("L" + lNum++, TokenType.STACK));

            ArrayList<Token> partInBrackets = new ArrayList<>();
            table.add(partInBrackets);
            partInBrackets.add(y);
            partInBrackets.add(z);

//            stack.add(2, new Token("(", TokenType.LEFT_BRACKET));
//            stack.add(3, y);
//            stack.add(4, z);
//            stack.add(5, new Token(")", TokenType.RIGHT_BRACKET));
            return true;
        }
        return false;
    }

}
