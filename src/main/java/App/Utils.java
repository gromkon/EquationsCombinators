package App;

import App.Lambda.Brackets.Bracket;
import App.Token.Token;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Utils {

    public static String readFile(String fileName) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            return reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Can't find file \"" + fileName + "\"");
        }
    }

    public static ArrayList<Token> deleteDoubleBrackets(ArrayList<Token> tokens) {
        boolean isSmthDelete = true;
        while (isSmthDelete) {
            ArrayList<Bracket> brackets = getBracketsIndexes(tokens);
            isSmthDelete = false;
            for (int i = 0; i < brackets.size() - 1; i++) {
                Bracket bracket1 = brackets.get(i);
                Bracket bracket2 = brackets.get(i + 1);
                if (bracket1.getLeft() - 1 == bracket2.getLeft() && bracket1.getRight() + 1 == bracket2.getRight()) {
                    tokens.remove(bracket1.getRight());
                    tokens.remove(bracket1.getLeft());
                    isSmthDelete = true;
                    break;
                }
            }
        }
        if (tokens.get(0).isLeftBracket() && tokens.get(tokens.size() - 1).isRightBracket()) {
            tokens.remove(tokens.size() - 1);
            tokens.remove(0);
        }

        return tokens;
    }


    // Ищет индексы скобок
    private static ArrayList<Bracket> getBracketsIndexes(ArrayList<Token> tokens) {
        ArrayList<Bracket> brackets = new ArrayList<>();
        ArrayList<Integer> leftBrackets = new ArrayList<>();

        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).getValue().equals("(")) {
                leftBrackets.add(i);
            } else if (tokens.get(i).getValue().equals(")")) {

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

    public static String calcNewInput(ArrayList<Token> left, ArrayList<Token> right) {
        StringBuilder sb = new StringBuilder();
        sb.append(concatTokens(left));
        sb.append("=");
        sb.append(concatTokens(right));
        return sb.toString();
    }

    public static String concatTokens(ArrayList<Token> tokens) {
        StringBuilder sb = new StringBuilder();
        for (Token token: tokens) {
            sb.append(token.getValue());
        }
        return sb.toString();
    }

}
