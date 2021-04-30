package App.Lambda;

import App.Lambda.Brackets.Bracket;
import App.Lambda.Brackets.Part;
import App.Token.Token;
import App.Token.TokenType;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LambdaLexer {

    // Входная строка
    private String input;
    // Токены левой части
    private ArrayList<Token> left;
    // Токены правой части
    private ArrayList<Token> right;

    public LambdaLexer(String input) {
        this.input = input;
        left = new ArrayList<>();
        right = new ArrayList<>();

        parseLeft();

        parseRight();
    }

    // Разбивает левую часть на токены
    private void parseLeft() {
        int equalPos = input.indexOf("=");
        String str = input.substring(0, equalPos);
        str = str.replace(" ", "");

        // Добавляем токен с именем
        this.left.add(parseNameLeft(str));
    }

    // Выполняет поиск имени в левой части
    private Token parseNameLeft(String str) {
        Pattern pattern = Pattern.compile("^[A-Z]+[0-9]*");
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            String name = str.substring(matcher.start(), matcher.end());
            return new Token(name, TokenType.NAME);
        } else {
            throw new RuntimeException("No name token in left part");
        }
    }

    // Выполняет поиск x1...xn в левой части
    private ArrayList<Token> parseVariablesLeft(String str) {
        ArrayList<Token> tokens = new ArrayList<>();
        Pattern pattern = Pattern.compile("x[0-9]");
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
            String var = str.substring(matcher.start(), matcher.end());
            tokens.add(new Token(var, TokenType.VARIABLE));
        }
        return tokens;
    }

    // Разбивает правую часть на токены
    private void parseRight() {
        String xsLeft = getXSLeft();

        // Получаем строку с (x1...xn) из левой части склеенную со всей правой
        int equalPos = input.indexOf("=");
        String str = input.substring(equalPos + 1);
        str = xsLeft.concat(str.replace(" ", ""));

        // Ищем "part-ы"
        ArrayList<Part> parts = getPartsRight(str);

        // Разбиваем "part-ы" на токены и добавляем их в правую часть
        this.right.addAll(getTokensFromPartsRight(parts));

        // Группируем токены в лямбды
        groupTokensInLambdas(right);
        // Все лямбды "разворачиваем"
        ArrayList<Pair<String, String>> namesChange = ungroupLambdas(right);
        // Обновляем имена у токенов
        updateNames(namesChange, right);

//        for (Token token: right) {
//            System.out.println(token);
//        }

    }

    private void getNewRightPart(ArrayList<Token> tokens) {
        for (Token token: tokens) {
            if (token.isLambda()) {
                System.out.print(token.getValue());
                break;
            }
            if (token.haveChild()) {
                getNewRightPart(token.getChildren());
            } else {
                System.out.print(token.getValue());
            }
        }
    }

    // Разделяет лямбда-токены "\x1x2.expr" -> "\x1.(\x2.(expr))
    // Также добавляет при необходимости скобки в лямбда-токены "\x1.expr" ->"\x1.(expr)"
    private ArrayList<Pair<String, String>> ungroupLambdas(ArrayList<Token> tokens) {
        ArrayList<Pair<String, String>> namePairs = new ArrayList<>();
        for (Token token: tokens) {
            if (token.isLambda()) {
                String value = token.getValue();
                // Находим имена лямбд
                ArrayList<String> names = getLambdasNames(value);
                int namesSize = names.size();
                // Если лямбду можно "развернуть"
                if (namesSize > 1) {
                    Token parentToken = token;
                    for (int i = 1; i < namesSize; i++) {

                        // Имя для новой лямбды
                        StringBuilder nameSb = new StringBuilder();
                        nameSb.append("\\");
                        for (int j = i; j < namesSize; j++) {
                            nameSb.append(names.get(j));
                        }
                        nameSb.append(".");
//                        nameSb.append("(");
                        for (Token child: parentToken.getChildren()) {
                            nameSb.append(child.getValue());
                        }
//                        nameSb.append(")");
                        Token insideLambda = new Token(nameSb.toString(), TokenType.LAMBDA);

                        // Дети для новой лямбды
                        ArrayList<Token> children = new ArrayList<>();
//                        // Если дети лямбды уже обернуты в скобки, или ребенок всего 1, то новых скобок не добавляем
                        ArrayList<Token> parentsChildren = parentToken.getChildren();
//                        if (parentsChildren.get(0).isLeftBracket() && parentsChildren.get(parentsChildren.size() - 1).isRightBracket() && parentsChildren.size() > 1) {
//                            children.addAll(parentsChildren);
//                        } else {
//                            children.add(new Token("(", TokenType.LEFT_BRACKET));
//                            children.addAll(parentsChildren);
//                            children.add(new Token(")", TokenType.RIGHT_BRACKET));
//                        }
                        children.addAll(parentsChildren);

                        insideLambda.addAll(children);

                        // Меняем детей у родителя
                        parentToken.deleteChildren();
//                        parentToken.add(new Token("(", TokenType.LEFT_BRACKET));
                        parentToken.add(insideLambda);
//                        parentToken.add(new Token(")", TokenType.RIGHT_BRACKET));

                        // Меняем имя у родителя
                        String oldName = parentToken.getValue();
                        StringBuilder parentName = new StringBuilder();
                        parentName.append("\\");
                        parentName.append(names.get(i - 1));
                        parentName.append(".");
//                        parentName.append("(");
                        parentName.append(insideLambda.getValue());
//                        parentName.append(")");
                        parentToken.setValue(parentName.toString());

                        parentToken = insideLambda;

                        // Меняем имя у всех родителей
                        namePairs.add(new Pair<>(oldName, parentName.toString()));
                    }
                } else {
//                    // Добавляем скобки при необходимости
//                    ArrayList<Token> children = token.getChildren();
//                    if (!(children.get(0).isLeftBracket() && children.get(children.size() - 1).isRightBracket()) && children.size() != 1) {
//                        children.add(0, new Token("(", TokenType.LEFT_BRACKET));
//                        children.add(new Token(")", TokenType.RIGHT_BRACKET));
//
//                        // Меняем имя
//                        String oldName = token.getValue();
//                        StringBuilder name = new StringBuilder();
//                        int dotIndex = oldName.indexOf(".");
//                        name.append(oldName.substring(0, dotIndex));
//                        name.append("(");
//                        name.append(oldName.substring(dotIndex + 1));
//                        name.append(")");
//                        token.setValue(name.toString());
//                    }
                }
            }
            if (token.haveChild()) {
                ArrayList<Pair<String, String>> names = ungroupLambdas(token.getChildren());
                namePairs.addAll(names);
            }
        }
        return namePairs;
    }

    // Обновляет имя родителей лямб-токенов
    private void updateNames(ArrayList<Pair<String, String>> names, Token token) {
        for (Pair<String, String> namePair: names) {
            String oldName = namePair.getKey();
            String newName = namePair.getValue();
            String name = token.getValue();
            token.setValue(name.replace(oldName, newName));
        }
    }

    // Обновляет имя родителей лямб-токенов
    private void updateNames(ArrayList<Pair<String, String>> names, ArrayList<Token> tokens) {
        for (Token token: tokens) {
            updateNames(names, token);
            if (token.haveChild()) {
                updateNames(names, token.getChildren());
            }
        }
    }

    // Возвращает все имена лямбды
    private ArrayList<String> getLambdasNames(String value) {
        ArrayList<String> names = new ArrayList<>();

        String name = value.substring(1, value.indexOf("."));

        Pattern pattern = Pattern.compile("[a-z][0-9]*");
        Matcher matcher = pattern.matcher(name);

        while (matcher.find()) {
            names.add(name.substring(matcher.start(), matcher.end()));
        }

        return names;
    }

    // Группирует лямбда-токены в лямбды
    private boolean groupTokensInLambdas(ArrayList<Token> tokens) {
        boolean matchLambda = true;

        while (matchLambda) {
            matchLambda = false;
            for (int i = 0; i < tokens.size(); i++) {
                Token token = tokens.get(i);

                // Если встретили лямда-токен, группируем его
                if (token.isBackslash()) {
                    matchLambda = true;
                    int startIndex = i++;
                    Token lambdaNameToken = tokens.get(i);

                    // Ищем точку
                    while (!lambdaNameToken.isDot()) {
                        lambdaNameToken = tokens.get(++i);
                    }

                    // Группируем имена лямбда-токенов
                    ArrayList<Token> lambdaNameTokens = new ArrayList<>(tokens.subList(startIndex + 1, i));

                    // Формируем детей лямбды
                    ArrayList<Token> children = new ArrayList<>();
                    // Если лямбда находится внутри скобок, добавляем до предпоследнего токена
                    // Если лямбда находится не в скобках, добавляем до последнего токена
                    int lastPos = lambdaEndPos(tokens);
                    for (i = i + 1; i <= lastPos; i++) {
                        children.add(tokens.get(i));
                    }

                    // Формируем имя лямбда-токена
                    // Добавляем "\имя."
                    StringBuilder sb = new StringBuilder();
                    sb.append("\\");
                    for (Token lambdaName: lambdaNameTokens) {
                        sb.append(lambdaName.getValue());
                    }
                    sb.append(".");
                    // Добавляем имена детей
                    for (Token child: children) {
                        sb.append(child.getValue());
                    }

                    Token lambdaToken = new Token(sb.toString(), TokenType.LAMBDA);
                    // Добавляем в токен детей
                    lambdaToken.addAll(children);

                    // Удаляем все токены, которые добавили в лямбду
                    tokens.removeAll(tokens.subList(startIndex, lastPos + 1));
                    // Добавляем лямбду в токены
                    tokens.add(startIndex, lambdaToken);

                    // Уменьшаем индекс обхода массива, т.к. удалили элементы из массива
                    i -= lastPos + 1 - startIndex;

                } else if (token.haveChild()) {         // Если встретили токен, у которого есть дети, ищем там лямба токены
                    boolean match = groupTokensInLambdas(token.getChildren());
                    if (match) {
                        matchLambda = match;
                    }
                }
            }
        }
        return matchLambda;

    }

    // Проверяет, находится ли токен из массива токенов внутри какой-либо скобки
    // Если да, то возвращает предпоследнюю позицию (последняя позиция это скобка) скобки,
    // Если нет - возвращает последнюю позицию
    private int lambdaEndPos(ArrayList<Token> tokens) {
        if (tokens.get(0).isLeftBracket()) {
            return tokens.size() - 2;
        }
        return tokens.size() - 1;
    }

    // Разбивает "part-ы" на токены
    private ArrayList<Token> getTokensFromPartsRight(ArrayList<Part> parts) {
        ArrayList<Token> tokens = new ArrayList<>();
        for (Part part: parts) {
            if (part.isBracket()) {
                Token bracketToken = getBracketPartTokens(part.getPart());
                tokens.add(bracketToken);
            } else {
                ArrayList<Token> partTokens = getNoBracketPartTokens(part.getPart());
                tokens.addAll(partTokens);
            }

        }
        return tokens;
    }

    // Представляет выржаение (...), где внутри возможно скобки, в виде токена-дерева
    private Token getBracketPartTokens(String str) {
        Token token = new Token(str, TokenType.EXPRESSION);

        // Получаем дерево со скобками
        ArrayList<Bracket> brackets = getBracketsTreeRight(str);
        if (brackets.size() != 1) {
            throw new RuntimeException("Not than one pair of brackets is str");
        }
        Bracket bracket = brackets.get(0);

        // Получаем всех его детей
        ArrayList<Token> childTokens = getInBracketChildTokens(bracket, str, 0);
        token.addAll(childTokens);

        return token;
    }

    // Разбивает выражение (...), где внутри возможны скобки, на токены
    private ArrayList<Token> getInBracketChildTokens(Bracket bracket, String str, int lastIndex) {
        ArrayList<Token> tokens = new ArrayList<>();
        // Если есть дети
        if (bracket.haveChild()) {
            ArrayList<Bracket> children = bracket.getChildren();
            for (Bracket child: children) {
                // Если между последним обработаным индексом и следующей открывающей скобкой есть какие-то символы, то обрабатываем их
                if (child.getLeft() > lastIndex) {
                    String left = str.substring(lastIndex, child.getLeft()); // Выделяем левую часть
                    // Разбиваем левую часть на токены
                    if (left.charAt(0) == '(') {
                        tokens.addAll(getFromLeftBracketWithNoInsideBracketTokens(left));
                    } else {
                        tokens.addAll(getNoBracketPartTokens(left));
                    }
                }
                lastIndex = child.getLeft();

                // Добавляем ребенка как токен
                String middle = str.substring(child.getLeft(), child.getRight() + 1);
                Token childToken = new Token(middle, TokenType.EXPRESSION);
                ArrayList<Token> childTokens = getInBracketChildTokens(child, str, lastIndex);
                childToken.addAll(childTokens);
                tokens.add(childToken);

                lastIndex = child.getRight() + 1;
            }
            // После обработки всех детей, разбиваем выражение, справа от последней скобки (как минимум будет закрывающая скобка)
            String right = str.substring(lastIndex);
            tokens.addAll(getToRightBracketWithNoInsideBracketTokens(right));
        } else {
            tokens.addAll(getBracketWithNoInsideBracketTokens(str.substring(bracket.getLeft(), bracket.getRight() + 1)));
        }
        return tokens;
    }

    // Разбивает выражение (...), где внутри нету других скобок, на токены
    private ArrayList<Token> getBracketWithNoInsideBracketTokens(String str) {
        ArrayList<Token> tokens = new ArrayList<>();
        tokens.add(new Token("(", TokenType.LEFT_BRACKET));
        ArrayList<Token> tokensInBracket = getNoBracketPartTokens(str.substring(1, str.lastIndexOf(")")));
        tokens.addAll(tokensInBracket);
        tokens.add(new Token(")", TokenType.RIGHT_BRACKET));
        return tokens;
    }

    // Разбивает выражение (.., где внутри нету других скобок, на токены
    private ArrayList<Token> getFromLeftBracketWithNoInsideBracketTokens(String str) {
        ArrayList<Token> tokens = new ArrayList<>();
        tokens.add(new Token("(", TokenType.LEFT_BRACKET));
        ArrayList<Token> tokensInBracket = getNoBracketPartTokens(str.substring(1));
        tokens.addAll(tokensInBracket);
        return tokens;
    }

    // Разбивает выражение ..), где внутри нету других скобок, на токены
    private ArrayList<Token> getToRightBracketWithNoInsideBracketTokens(String str) {
        ArrayList<Token> tokens = new ArrayList<>();
        ArrayList<Token> tokensInBracket = getNoBracketPartTokens(str.substring(0, str.lastIndexOf(")")));
        tokens.addAll(tokensInBracket);
        tokens.add(new Token(")", TokenType.RIGHT_BRACKET));
        return tokens;
    }

    // Разбивает выражение без скобок на токены
    private ArrayList<Token> getNoBracketPartTokens(String str) {
        ArrayList<Token> tokens = new ArrayList<>();
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];

            if (isBackslash(c)) {                                                       // Обратная черта
                tokens.add(new Token("\\", TokenType.BACKSLASH));
            } else if (isDot(c)) {                                                      // Точка
                tokens.add(new Token(".", TokenType.DOT));
            } else if (isSymbol(c)) {                                                   // Переменная
                int start = i;
                if (i + 1 >= chars.length) {
                    tokens.add(new Token(c, TokenType.VARIABLE));
                    break;
                }
                char nextChar = chars[++i];
                while (isDigit(nextChar)) {
                    i++;
                    if (i == chars.length) {
                        break;
                    }
                    nextChar = chars[i];
                }
                tokens.add(new Token(str.substring(start, i), TokenType.VARIABLE));

                i--; // Т.к. мы уже находимся на следующем символе, то i--
            } else if (isDigit(c)) {                                                    // Число
                throw new RuntimeException("Error in writing the right part. Expected \"\\\" or \".\" or character in string \"" + str + "\"");
            }
        }
        return tokens;
    }

    // Разбивает правую часть на "part-ы"
    private ArrayList<Part> getPartsRight(String str) {
        ArrayList<Part> parts = new ArrayList<>();

        // Получаем дерево со скобками
        ArrayList<Bracket> brackets = getBracketsTreeRight(str);
        int prevIndexes = 0;

        // Разбиваем строку на "part-ы"
        for (Bracket bracket: brackets) {
            if (bracket.getLeft() - 1 > prevIndexes) {
                parts.add(new Part(
                        str.substring(prevIndexes, bracket.getLeft()),
                        prevIndexes,
                        bracket.getLeft() - 1
                ));
            }
            if (bracket.getRight() > bracket.getLeft()) {
                parts.add(new Part(
                        str.substring(bracket.getLeft(), bracket.getRight() + 1),
                        bracket.getLeft(),
                        bracket.getRight()
                ));
            }
            prevIndexes = bracket.getRight() + 1;
        }
        // Добавляем "part" справа от последней скобки
        if (str.length() > prevIndexes) {
            parts.add(new Part(
                    str.substring(prevIndexes),
                    prevIndexes,
                    str.length() - 1
            ));
        }

        return parts;
    }

    // Возвращает "дерево со скобками" в правой части
    private ArrayList<Bracket> getBracketsTreeRight(String str) {
        // Ищем индексы скобок
        ArrayList<Bracket> brackets = getBracketsIndexesRight(str);

        // Составляем "дерево" со скобками
        return findBracketsChildren(brackets);
    }

    // Ищет индексы скобок
    private ArrayList<Bracket> getBracketsIndexesRight(String str) {
        ArrayList<Bracket> brackets = new ArrayList<>();
        ArrayList<Integer> leftBrackets = new ArrayList<>();

        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '(') {
                leftBrackets.add(i);
            } else if (str.charAt(i) == ')') {

                // Если нашли правую скобку, но не нашли левую
                if (leftBrackets.isEmpty()) {
                    throw new RuntimeException("The right bracket has no pair");
                }

                // Создаем Bracket из последней найденной левой скобки и найденной правой; удаляем последнюю левую скобку из массива
                brackets.add(new Bracket(leftBrackets.get(leftBrackets.size() - 1), i));
                leftBrackets.remove(leftBrackets.size() - 1);
            }
        }

        if (!leftBrackets.isEmpty()) {
            throw new RuntimeException("Different count of left and right brackets");
        }

        return brackets;
    }

    // Возвращает "дерево" со скобками (вспомогательная функция)
    private ArrayList<Bracket> findBracketsChildren(ArrayList<Bracket> brackets) {
        ArrayList<Bracket> deleteBrackets = new ArrayList<>();
        for (Bracket bracket: brackets) {
            // Если эта скобка удалена, то пропускаем ее
            if (isDeleteBracket(bracket, deleteBrackets)) {
                continue;
            }

            for (Bracket bracketInside: brackets) {
                // Если это та же самая скобка, до переходим к следующей
                if (bracket.isEqualIndexes(bracketInside)) {
                    continue;
                }

                // Если эта скобка удалена, то пропускаем ее
                if (isDeleteBracket(bracketInside, deleteBrackets)) {
                    continue;
                }

                // Если bracketInside внутри bracket, то добавляем bracketInside к детям bracket; добавляем bracketInside в список удаленных скобок
                if (bracketInside.isInside(bracket)) {
                    bracket.add(bracketInside);
                    deleteBrackets.add(bracketInside);
                }

            }
        }

        // Удаляем лишнии скобки
        for (Bracket deleteBracket: deleteBrackets) {
            brackets.remove(deleteBracket);
        }

        return brackets;
    }

    // Проверяет, удалена ли данная скобка
    private boolean isDeleteBracket(Bracket bracket, ArrayList<Bracket> brackets) {
        for (Bracket deleteBracket: brackets) {
            if (bracket.isEqualIndexes(deleteBracket)) {
                return true;
            }
        }
        return false;
    }

    // Проверяет, является данный char backslash-ом "\"
    private boolean isBackslash(char c) {
        return c == '\\';
    }

    // Проверяет, является данный char точкой "."
    private boolean isDot(char c) {
        return c == '.';
    }

    // Проверяет, является ли данная строка переменной x1, x2 и т. п.
    private boolean isVariable(String var) {
        Pattern pattern = Pattern.compile("[a-z][0-9]*");
        Matcher matcher = pattern.matcher(var);
        return matcher.find();
    }

    // Проверяет, является данный char символом [a-z]
    private boolean isSymbol(char c) {
        String s = String.valueOf(c);
        Pattern pattern = Pattern.compile("[a-z]");
        Matcher matcher = pattern.matcher(s);
        return matcher.find();
    }

    // Проверяет, является данный char цифрой [0-9]
    private boolean isDigit(char c) {
        String s = String.valueOf(c);
        Pattern pattern = Pattern.compile("[0-9]");
        Matcher matcher = pattern.matcher(s);
        return matcher.find();
    }

    // Получаем строку вида "\x1...xn." где (x1...xn) - токены из левой части
    private String getXSLeft() {
        // Получаем токены x1...xn из левой части
        int equalPos = input.indexOf("=");
        String str = input.substring(0, equalPos);
        str = str.replace(" ", "");
        // Преобразовываем их в строку
        ArrayList<Token> xs = parseVariablesLeft(str);
        StringBuilder sb = new StringBuilder();
        if (!xs.isEmpty()) {
            sb.append("\\");
            for (Token token: xs) {
                sb.append(token.getValue());
            }
            sb.append(".");
        }
        return sb.toString();
    }

    public ArrayList<Token> getLeft() {
        return left;
    }

    public Token getCombinatorName() {
        return left.get(0);
    }

    public ArrayList<Token> getRight() {
        return right;
    }
}
