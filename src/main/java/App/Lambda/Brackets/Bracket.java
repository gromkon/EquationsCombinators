package App.Lambda.Brackets;

import java.util.ArrayList;

public class Bracket {

    // Левый индекс скобки в строке
    private int left;
    // Правый индекс скобки в строке
    private int right;
    // Скобки, находящиеся внутри данной скобки
    private ArrayList<Bracket> children;
    // Является ли данная скобка ребенком другой скобки
    private boolean isChild;

    public Bracket(int left, int right) {
        this.left = left;
        this.right = right;
        children = new ArrayList<>();
        isChild = false;
    }

    public void add(Bracket bracket) {
        children.add(bracket);
        bracket.setChild(true);
    }

    // Проверяет, находится ли this внутри bracket
    public boolean isInside(Bracket bracket) {
        return this.left > bracket.left && this.right < bracket.right;
    }

    public boolean isEqualIndexes(Bracket bracket) {
        return this.left == bracket.left && this.right == bracket.right;
    }

    public int getLeft() {
        return left;
    }

    public int getRight() {
        return right;
    }

    public ArrayList<Bracket> getChildren() {
        return children;
    }

    public boolean haveChild() {
        return children.size() > 0;
    }

    public boolean isChild() {
        return isChild;
    }

    public void setChild(boolean child) {
        isChild = child;
    }

    @Override
    public String toString() {
        return "Bracket{" +
                "left=" + left +
                ", right=" + right +
                ", children=" + children +
                '}';
    }
}
