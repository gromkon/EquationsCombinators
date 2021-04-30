package App;

import App.CombinatorialLogic.CombinatoricsTable;
import App.LambdaToCombinatorical.ConvertTable;
import App.NotActual.Combinators.Lexer;
import App.Lambda.LambdaLexer;
import App.Lambda.Simplify.SimplifyTable;
import App.Token.Token;
import App.Token.TokenType;


import java.util.ArrayList;

import static App.Utils.*;

public class Main {

    private static void lambdaLexer(String[] args) {
        String input;
        if (args.length > 0) {
            input = Utils.readFile(args[0]);
        } else {
//            input = Utils.readFile("input/testZapiska.txt");
            input = Utils.readFile("input/test3.txt");
        }
        System.out.println(input);
        System.out.println();

        LambdaLexer lexer = new LambdaLexer(input);
        ArrayList<Token> left = lexer.getLeft();
        ArrayList<Token> right = lexer.getRight();
        for (Token rightToken: right) {
            System.out.print(rightToken.getValue());
        }
        System.out.println();

        SimplifyTable table = new SimplifyTable(right);
        table.simplify();
        ArrayList<Token> simpleRight = table.getSimpleTokens();
        ArrayList<Token> simpleRightDeleteBrackets = deleteDoubleBrackets(simpleRight);

        for (Token token: simpleRightDeleteBrackets) {
            System.out.print(token.getValue());
        }
        System.out.println();
//        System.out.println();
//        System.out.println();


        ConvertTable convertTable = new ConvertTable(simpleRightDeleteBrackets);
        ArrayList<Token> combinatorsExpr = convertTable.getConvert();


//        combinatorsExpr.add(19, new Token("S", TokenType.S_RULE));
//        combinatorsExpr.add(20, new Token("S", TokenType.S_RULE));
//        combinatorsExpr.add(21, new Token("K", TokenType.K_RULE));
//        combinatorsExpr.add(22, new Token("S", TokenType.S_RULE));
//        combinatorsExpr.add(23, new Token("K", TokenType.K_RULE));

//        System.out.println(combinatorsExpr);
        for (Token token: combinatorsExpr) {
            System.out.print(token.getValue());
        }
        System.out.println();

        CombinatoricsTable combinatoricsTable = new CombinatoricsTable(combinatorsExpr);
        System.out.print(left.get(0));
        System.out.print(" = ");
        ArrayList<Token> resRight = combinatoricsTable.getSimplify();
        for (Token token: resRight) {
            System.out.print(token.getValue());
        }

    }

    public static void main(String[] args) {

        lambdaLexer(args);

    }
}
