package App.LambdaToCombinatorical;

import App.Token.Token;
import App.Token.TokenType;

import java.util.ArrayList;
import java.util.List;

public class ConvertTable {

    private ArrayList<Token> tokens;

    private ArrayList<ArrayList<Token>> table;
    private static int lNum = 1;

    public ConvertTable(ArrayList<Token> tokens) {
        this.tokens = tokens;
        table = new ArrayList<>();

        fillTable(tokens);

        convert();

    }

    // Заполняем таблицу
    private void fillTable(List<Token> tokens) {
        ArrayList<Token> stack = new ArrayList<>();
        table.add(stack);
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);

            if (token.isLeftBracket()) {
                stack.add(token);

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

                // Когда count == 0, в token находится ")"
                stack.add(new Token("L" + lNum++, TokenType.STACK));
                stack.add(token);
                fillTable(tokens.subList(start, i));

            } else if (token.isBackslash()) {
                stack.add(token);

                // Имя
                i++;
                token = tokens.get(i);
                stack.add(token);

                // Точка
                i++;
                token = tokens.get(i);
                stack.add(token);

                stack.add(new Token("L" + lNum++, TokenType.STACK));
                fillTable(tokens.subList(i + 1, tokens.size()));
                break;
            } else {
                stack.add(token);
            }


        }
    }

    public ArrayList<Token> getConvert() {
        return table.get(0);
    }

    private void convert() {
        for (int stackNum = table.size() - 1; stackNum >= 0; stackNum--) {
            ArrayList<Token> stack = table.get(stackNum);

            substitute(stack);

            if (haveLambda(stack)) {
                int lastLambdaPos = getLastLambdaPos(stack);
                while (lastLambdaPos != -1) {
                    doRules(stack.subList(lastLambdaPos, stack.size()));
                    lastLambdaPos = getLastLambdaPos(stack);
                }
            }

//            for (Token token: stack) {
//                System.out.print(token.getValue());
//            }
//            System.out.println();
        }
    }

    // Подставляем в stack все правила
    private void substitute(ArrayList<Token> stack) {
        for (int i = stack.size() - 1; i >= 0; i--) {
            Token token = stack.get(i);
            if (token.isStack()) {
                int num = token.getStackNum();
                stack.remove(i);
                stack.addAll(i, table.get(num));
            }
        }
    }

    // Проверяет, есть ли в stack лямбда
    private boolean haveLambda(ArrayList<Token> stack) {
        for (Token token : stack) {
            if (token.isBackslash()) {
                return true;
            }
        }
        return false;
    }

    // Возвращает позицию самой последней лямбды
    private int getLastLambdaPos(ArrayList<Token> stack) {
        for (int i = stack.size() - 1; i >= 0; i--) {
            Token token = stack.get(i);
            if (token.isBackslash()) {
                return i;
            }
        }
        return -1;
    }


    private void doRules(List<Token> stack) {
        String name = stack.get(1).getValue();

        // Удаляем все внешние скобки
        boolean deletedBrackets = deleteAllArgsBrackets(stack);
        while (deletedBrackets) {
            deletedBrackets = deleteAllArgsBrackets(stack);
        }

        ArrayList<Argument> args = getLambdaArgs(stack);
        checkRule(name, args, stack);
    }


    // Удаляет внешние скобки у лямбы "\x.( ... )" -> "\x. ..."
    private boolean deleteAllArgsBrackets(List<Token> stack) {
        if (stack.get(3).isLeftBracket()) {
            int count = 1;
            int i = 3;
            while (count > 0) {
                i++;
                Token token = stack.get(i);
                if (token.isLeftBracket()) {
                    count++;
                } else if (token.isRightBracket()) {
                    count--;
                }
            }
            // Скобки действительно внешние, удаляем
            if (i == stack.size() - 1) {
                stack.remove(i);
                stack.remove(3);
                return true;
            }
        }
        return false;
    }

    // Формируем массив из аргументов лямбды
    private ArrayList<Argument> getLambdaArgs(List<Token> stack) {
        ArrayList<Argument> args = new ArrayList<>();
        for (int i = 3; i < stack.size(); i++) {
            Token token = stack.get(i);
            Argument arg = new Argument(token, i);

            if (token.isLeftBracket()) {
                int count = 1;
                while (count > 0) {
                    i++;
                    token = stack.get(i);
                    arg.add(token, i);
                    if (token.isLeftBracket()) {
                        count++;
                    } else if (token.isRightBracket()) {
                        count--;
                    }
                }

                args.add(arg);
            } else {
                args.add(arg);
            }
        }
        return args;
    }

    // Проверяем, какое правило можем применить
    private void checkRule(String name, ArrayList<Argument> args, List<Token> stack) {
        if (args.size() == 1) {
            if (name.equals(args.get(0).getName())) {
                doRuleI(stack);
            } else {
                doRuleK(stack, args);
            }
        } else {
            if (isStackHaveArgWithSameName(name, args)) {
                doRuleS(name, args, stack);
            } else {
                doRuleK(stack, args);
            }
        }
    }

    // Проверяет, есть ли в аргументе лямбды, функция с таким же названием, как и сама лямбда
    private boolean isStackHaveArgWithSameName(String name, ArrayList<Argument> args) {
        for (Argument arg: args) {
            ArrayList<Token> tokens = arg.getTokens();
            for (Token token: tokens) {
                if (token.getValue().equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void doRuleI(List<Token> stack) {
        stack.clear();
        stack.add(new Token("I", TokenType.I_RULE));
    }

    private void doRuleK(List<Token> stack, ArrayList<Argument> args) {
        stack.clear();
        stack.add(new Token("K", TokenType.K_RULE));
        if (args.size() == 1) {
            Argument arg = args.get(0);
            stack.addAll(arg.getTokens());
        } else {
            stack.add(new Token("(", TokenType.LEFT_BRACKET));
            for (Argument arg: args) {
                stack.addAll(arg.getTokens());
            }
            stack.add(new Token(")", TokenType.RIGHT_BRACKET));
        }

    }

    private void doRuleS(String name, ArrayList<Argument> args, List<Token> stack) {
        // Группируем элементы
        while (args.size() > 2) {
            Argument argument1 = args.get(0);
            Argument argument2 = args.get(1);
            argument1.group(argument2);
            args.remove(1);
        }

        stack.clear();
        stack.add(new Token("S", TokenType.S_RULE));
        doBracketS(name, args.get(0), stack);
        doBracketS(name, args.get(1), stack);
    }

    private void doBracketS(String name, Argument argument, List<Token> stack) {
        ArrayList<Token> bracket = new ArrayList<>();

        bracket.add(new Token("\\", TokenType.BACKSLASH));
        bracket.add(new Token(name, TokenType.VARIABLE));
        bracket.add(new Token(".", TokenType.DOT));
        bracket.addAll(argument.getTokens());
        doRules(bracket);

        if (bracket.size() == 1) {
            stack.add(bracket.get(0));
        } else {
            stack.add(new Token("(", TokenType.LEFT_BRACKET));
            stack.addAll(bracket);
            stack.add(new Token(")", TokenType.RIGHT_BRACKET));
        }
    }


}
