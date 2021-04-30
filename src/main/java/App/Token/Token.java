package App.Token;

import javafx.util.Pair;

import java.util.ArrayList;

public class Token {

    // Значение токена
    private String value;
    // Тип токена
    private TokenType type;
    // Дети токена
    private ArrayList<Token> children;

    public Token(char value, TokenType type) {
        this.value = String.valueOf(value);
        this.type = type;
        children = new ArrayList<>();
    }

    public void setType(TokenType type) {
        this.type = type;
    }

    public Token(String value, TokenType type) {
        this.value = value;
        this.type = type;
        children = new ArrayList<>();
    }

    public void application(Token token, Token parentThis, Token parentOther) {
        String name = getLambdaName();

        // Получаем список токенов, которые нужно заменить
        // Массив, в котором надо заменить; позиция элемента
        ArrayList<Pair<ArrayList<Token>, Integer>> replacementTokens = new ArrayList<>();
        findReplacementTokens(name, children, replacementTokens);

        // Заменяем своих детей, с нужным именем, на новый token
        for (Pair<ArrayList<Token>, Integer> replacementToken: replacementTokens) {
            ArrayList<Token> tokens = replacementToken.getKey();
            int pos = replacementToken.getValue();
            tokens.remove(pos);
            tokens.add(pos, token);
        }

        // Заменяем этот элемент на список своих детей
        int index = parentThis.children.indexOf(this);
        parentThis.children.remove(this);
        parentThis.children.addAll(index, children);

        // Удаляем другой токен
        parentOther.children.remove(token);
    }

    // Находит токены, которые надо заменить
    private void findReplacementTokens(String name, ArrayList<Token> tokens, ArrayList<Pair<ArrayList<Token>, Integer>> replacementTokens) {
        for (int i = 0; i < tokens.size(); i++) {
            Token child = tokens.get(i);
            if (child.getValue().equals(name)) {
                replacementTokens.add(new Pair<>(tokens, i));
            }
            if (child.haveChild()) {
                findReplacementTokens(name, child.getChildren(), replacementTokens);
            }

        }
    }

    public String getLambdaName() {
        if (type != TokenType.LAMBDA) {
            return null;
        }
        String name = value.substring(value.indexOf("\\") + 1, value.indexOf("."));
        return name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getStackNum() {
        if (type != TokenType.STACK) {
            return -1;
        }
        return Integer.parseInt(value.substring(1));
    }

    public void add(Token token) {
        children.add(token);
    }

    public void addAll(ArrayList<Token> tokens) {
        children.addAll(tokens);
    }

    public boolean isOneSymbol() {
        return isBracket() || isBackslash() || isDot() || isVar() || isRuleI() || isRuleK() || isRuleS();
    }

    public boolean isName() {
        return type == TokenType.NAME;
    }

    public boolean isVar() {
        return type == TokenType.VARIABLE;
    }

    public boolean isExpr() {
        return type == TokenType.EXPRESSION;
    }

    public boolean isLambda() {
        return type == TokenType.LAMBDA;
    }

    public boolean isBackslash() {
        return type == TokenType.BACKSLASH;
    }

    public boolean isDot() {
        return type == TokenType.DOT;
    }

    public boolean isBracket() {
        return type == TokenType.LEFT_BRACKET || type == TokenType.RIGHT_BRACKET;
    }

    public boolean isLeftBracket() {
        return type == TokenType.LEFT_BRACKET;
    }

    public boolean isRightBracket() {
        return type == TokenType.RIGHT_BRACKET;
    }

    public boolean isStack() {
        return type == TokenType.STACK;
    }

    public boolean isRule() {
        return isRuleK() || isRuleS() || isRuleI();
    }

    public boolean isRuleK() {
        return type == TokenType.K_RULE;
    }

    public boolean isRuleS() {
        return type == TokenType.S_RULE;
    }

    public boolean isRuleI() {
        return type == TokenType.I_RULE;
    }

    public boolean haveChild() {
        return children.size() > 0;
    }

    public ArrayList<Token> getChildren() {
        return children;
    }

    public String getValue() {
        return value;
    }

    public TokenType getType() {
        return type;
    }

    public void deleteChildren() {
        children = new ArrayList<>();
    }

    @Override
    public String toString() {
        if (!children.isEmpty()) {
            return value + ": \t {" + children + "}";
        } else {
            return value;
        }
    }
}
