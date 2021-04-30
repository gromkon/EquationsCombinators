package App.Lambda.Brackets;


public class Part {

    // Выражение (...), где внутри могут быть другие скобки
    private String part;
    // Индекс начала "part-а" в строке
    private int left;
    // Индекс окончания "part-а" в строке
    private int right;

    public Part(String part, int left, int right) {
        this.part = part;
        this.left = left;
        this.right = right;
    }

    // Проверяет, находится ли скобка внутри данной части выражения
    public boolean isBracketInside(Bracket bracket) {
        return left <= bracket.getLeft() && right >= bracket.getRight();
    }

    public char charAt(int index) {
        return part.charAt(index);
    }

    public boolean isBracket() {
        return part.charAt(0) == '(';
    }

    public String getPart() {
        return part;
    }

    @Override
    public String toString() {
        return "Part{" +
                "part='" + part + '\'' +
                ", left=" + left +
                ", right=" + right +
                '}';
    }
}
