package App.Lambda.Simplify;

import App.Token.Token;
import App.Token.TokenType;
import javafx.util.Pair;

import java.util.ArrayList;

public class SimplifyTable {

    private ArrayList<Stack> table;
    private ArrayList<Token> tokens;

    private static int lNum = 1;

    public SimplifyTable(ArrayList<Token> tokens) {
        table = new ArrayList<>();
        this.tokens = tokens;

        // Если токены есть, то заполняем таблицу
        if (!tokens.isEmpty()) {
            fillTable();
        }
    }

    // Превращает таблицу в строку
    public ArrayList<Token> getSimpleTokens() {
        if (tokens.isEmpty()) {
            return null;
        }

        return simplificationL(0).getTokens();
    }

    // Упрощение l_i
    private Stack simplificationL(int pos) {
        Stack stack = table.get(pos);
        boolean somethingChange = true;

        while (somethingChange) {
            somethingChange = false;
            // Если есть stack, то заменяем его
            for (int j = 0; j < stack.size(); j++) {
                Token token = stack.get(j);
                if (token.isStack()) {
                    stackReplacement(stack, j);
                    somethingChange = true;
                    break;
                }
            }
        }

        return stack;
    }

    private void stackReplacement(Stack li, int pos) {
        Token token = li.get(pos);
        Stack lj = simplificationL(token.getStackNum());
        li.getTokens().remove(pos);
        li.getTokens().addAll(pos, lj.getTokens());
    }


    // Заполняем таблицу
    private void fillTable() {
        // Добавляем в таблицу стек
        Stack stack = new Stack(0);
        table.add(stack);

        for (Token token: tokens) {
            if (token.isOneSymbol()) {
                stack.add(token);
            } else {                                // Если выражение, создаем новый стек и обходим детей токена
                if (token.haveChild()) {
                    stack.add(new Token("L" + lNum, TokenType.STACK));
                    lNum++;
                    parseTokenChildren(token, 1);
                } else {
                    stack.add(token);
                }
            }
        }
    }

    // Добавляет в таблицу стек с детьми токена
    private void parseTokenChildren(Token token, int level) {
        Stack stack = new Stack(level);
        table.add(stack);

        ArrayList<Token> children = token.getChildren();
        if (token.isLambda()) {
            ArrayList<Token> name = new ArrayList<>();
            name.add(new Token("\\", TokenType.BACKSLASH));
            name.add(new Token(token.getLambdaName(), TokenType.VARIABLE));
            name.add(new Token(".", TokenType.DOT));
            children.addAll(0, name);
        }
        for (Token child: children) {
            if (child.isOneSymbol()) {
                stack.add(child);
            } else {
                stack.add(new Token("L" + lNum, TokenType.STACK));
                lNum++;
                parseTokenChildren(child, level + 1);
            }
        }
    }

    // Упрощаем выражение
    public void simplify() {
        boolean anyPQ = findPQ();
        while (anyPQ) {
            anyPQ = findPQ();
        }
    }

// Ищем и делаем PQ
private boolean findPQ() {

    for (int i = table.size() - 1; i >= 0; i--) {
        // Если в stack есть лямбда, то этот stack может быть P. Проверяем, есть ли Q
        if (isLambdaInStack(i)) {
            ArrayList<TableToken> q = findQforP(i);
            if (q != null) {
                ArrayList<TableToken> p = deployLambdaStack(i);
                PQExpression pq = new PQExpression(p, q, table.get(i).getLevel());
                pq.setTable(table);
                pq.application();
                return true;
            }
        }
    }

    return false;
}

    // Проверяет, есть ли Q у данного P
    private ArrayList<TableToken> findQforP(int stackNum) {
        ArrayList<TableToken> q = null;
        Pair<Integer, Integer> pos = findStackWithLRule(stackNum);
        int parentStackNum = pos.getKey();
        int LposInParent = pos.getValue();
        Stack parent = table.get(parentStackNum);
        ArrayList<Token> parentTokens = parent.getTokens();

        // Это значит, что P - крайний правый элемент и у него нету Q
        if (LposInParent + 1 >= parentTokens.size()) {
            return q;
        }
        // беру правый элемент, если это закрывающая скобка, то беру элемент дальше и нужно, чтобы это было ЧТО УГОДНО КРОМЕ ЗАКРЫВАЮЩЕЙ СКОБКИ
        //                    , если это открывающая скобка, тогда я Q - это вся скобка
        //                    , если это какой-то элемент, то подставляю этот элемент

        // если правый элемент скобка и это конец строки, то иду вверх

        // Позиция следующего элемента
        int i = LposInParent + 1;
        Token rightToken = parent.get(i);

        if (rightToken.isRightBracket()) {
            // Если справа от скобки есть еще элементы
            if (i + 1 < parentTokens.size()) {
                rightToken = parent.get(i);
                // Если это закрывающя скобка, тогда у нас вот такой вариант (...(... P)) и у P нету Q
                if (rightToken.isRightBracket()) {
                    return q;
                }
                // (... P) ( Q ) - ищем, где закрывается скобка. Это и есть Q (ВМЕСТЕ СО СКОБКАМИ)
                if (rightToken.isLeftBracket()) {
                    q = new ArrayList<>();
                    int countLeft = 1;
                    while (countLeft > 0) {
                        q.add(new TableToken(parentStackNum, i, rightToken));
                        i++;
                        rightToken = parent.get(i);
                        if (rightToken.isRightBracket()) {
                            countLeft --;
                        }
                    }
                    // Добавляем правую скобку
                    q.add(new TableToken(parentStackNum, i, rightToken));
                }
                // ( ... P) x - x это и есть Q
                if (rightToken.isVar() || rightToken.isStack()) {
                    q = new ArrayList<>();
                    q.add(new TableToken(parentStackNum, i, rightToken));
                }
            } else { // Если справа от скобки элементов нету
                return findQforP(parentStackNum);
            }
            // Если q найдено, то проверяем, что у нас слева от P внутри скобки ничего нету, т.е. ( P )
            if (q != null) {
                Token leftToken = parent.get(LposInParent - 1);
                if (leftToken.isLeftBracket()) {
                    return q;
                } else {
                    return null;
                }
            }
        } else if (rightToken.isLeftBracket()) { // P ( Q ) - ищем, где закрывается скобка. Это и есть Q (ВМЕСТЕ СО СКОБКАМИ)
            q = new ArrayList<>();
            int countLeft = 1;
            while (countLeft > 0) {
                q.add(new TableToken(parentStackNum, i, rightToken));
                i++;
                rightToken = parent.get(i);
                if (rightToken.isRightBracket()) {
                    countLeft --;
                }
            }
            // Добавляем правую скобку
            q.add(new TableToken(parentStackNum, i, rightToken));

            return q;
        } else if (rightToken.isVar() || rightToken.isStack()) { //  ... P x - x это и есть Q
            q = new ArrayList<>();
            q.add(new TableToken(parentStackNum, i, rightToken));

            return q;
        }

        return q;
    }

    // Возвращает пару <stackNum, pos>, где используется правило L_lnum
    private Pair<Integer, Integer> findStackWithLRule(int lNum) {
        for (int i = 0; i < table.size(); i++) {
            Stack stack = table.get(i);
            ArrayList<Token> tokens = stack.getTokens();
            for (int j = 0; j < tokens.size(); j++) {
                Token token = tokens.get(j);
                if (token.isStack() && token.getStackNum() == lNum) {
                    return new Pair<>(i, j);
                }
            }
        }
        return new Pair<>(-1, -1);
    }

    // Ищем среди PQ пар самую глубокую
    private PQExpression findPQMaxLevel(ArrayList<PQExpression> pqExpressions) {
        // Находим максимальную глубину
        int level = pqExpressions.get(0).getLevel();
        for (PQExpression pqExpression: pqExpressions) {
            if (pqExpression.getLevel() > level) {
                level = pqExpression.getLevel();
            }
        }

        // Добавляем все PQ выражения с максимальной глубиной в массив
        ArrayList<PQExpression> expressions = new ArrayList<>();
        for (PQExpression pqExpression: pqExpressions) {
            if (pqExpression.getLevel() == level) {
                expressions.add(pqExpression);
            }
        }

        // Т.к. PQ выражения добавляются по порядку, следовательно, самое последнее выражение будет находится правее всего
        return expressions.get(expressions.size() - 1);

    }

    // Проверяет, есть ли в данном стеке лямбда-выражение
    private boolean isLambdaInStack(int stackNum) {
        Stack stack = table.get(stackNum);
        ArrayList<Token> tokens = stack.getTokens();

        // Ищем внутри стека 3 последоватльных символа: BACKSLASH, VARIABLE, DOT
        for (int i = 0; i < tokens.size() - 2; i++) {
            Token backslashToken = tokens.get(i);
            Token variableToken = tokens.get(i + 1);
            Token dotToken = tokens.get(i + 2);
            if (backslashToken.isBackslash() && variableToken.isVar() && dotToken.isDot()) {
                return true;
            }
        }
        return false;
    }

    // Раскручиваем данный stack, пока новые L не перестанут добавляться
    private ArrayList<TableToken> deployLambdaStack(int stackNum) {
        ArrayList<TableToken> lambdaTokens = new ArrayList<>();
        Stack stack = table.get(stackNum);
        ArrayList<Token> tokens = stack.getTokens();

        // Находим индекс начала лямбды
        int start = 0;
        for (int i = 0; i < tokens.size() - 2; i++) {
            Token backslashToken = tokens.get(i);
            Token variableToken = tokens.get(i + 1);
            Token dotToken = tokens.get(i + 2);
            if (backslashToken.isBackslash() && variableToken.isVar() && dotToken.isDot()) {
                start = i + 3;
                lambdaTokens.add(new TableToken(stackNum, i, backslashToken));
                lambdaTokens.add(new TableToken(stackNum, i + 1, variableToken));
                lambdaTokens.add(new TableToken(stackNum, i + 2, dotToken));
            }
        }

        // Раскручиваем stack
        for (int i = start; i < stack.size(); i++) {
            if (stack.get(i).isStack()) {
                ArrayList<TableToken> lTokens = continueDeployLambdaStack(stack.get(i).getStackNum());
                if (!lTokens.isEmpty()) {
                    lambdaTokens.addAll(lTokens);
                }
            } else {
                lambdaTokens.add(new TableToken(stackNum, i, stack.get(i)));
            }
        }

        return lambdaTokens;

    }

    // Рекурсивно раскучиваем stack c заданного stack
    private ArrayList<TableToken> continueDeployLambdaStack(int stackNum) {
        ArrayList<TableToken> tokens = new ArrayList<>();
        Stack stack = table.get(stackNum);
        // Пока добавляются новые выражения раскручиваем stack
        for (int i = 0; i < stack.size(); i++) {
            if (stack.get(i).isStack()) {
                ArrayList<TableToken> lTokens = continueDeployLambdaStack(stack.get(i).getStackNum());
                if (!lTokens.isEmpty()) {
                    tokens.addAll(lTokens);
                }
            } else {
                tokens.add(new TableToken(stackNum, i, stack.get(i)));
            }
        }
        return tokens;
    }


}
