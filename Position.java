public class Position {
    private final int x;
    private final int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean isValid(int maxRows, int maxCols) {
        return this.x >= 0 && this.x < maxRows && this.y >= 0 && this.y < maxCols;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    // Métodos utilitários para navegação
    public Position moveUp() {
        return new Position(x - 1, y);
    }

    public Position moveDown() {
        return new Position(x + 1, y);
    }

    public Position moveLeft() {
        return new Position(x, y - 1);
    }

    public Position moveRight() {
        return new Position(x, y + 1);
    }

    public Position[] getAdjacentPositions() {
        return new Position[]{
            moveUp(),    // 0
            moveDown(),  // 1
            moveLeft(),  // 2
            moveRight()  // 3
        };
    }

    public int manhattanDistance(Position other) {
        return Math.abs(this.x - other.x) + Math.abs(this.y - other.y);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Position)) return false;
        Position other = (Position) obj;
        return this.x == other.x && this.y == other.y;
    }

    @Override
    public int hashCode() {
        return 31 * x + y;
    }
}