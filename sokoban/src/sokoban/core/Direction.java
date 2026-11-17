package sokoban.core;

/**
 * 3차원 6방향. 화면(아이소메트릭) 대응:
 * x+ = 오른쪽 아래, y+ = 왼쪽 아래, z+ = 위.
 * UP(y-1)=오른쪽 위, RIGHT(x+1)=오른쪽 아래, DOWN(y+1)=왼쪽 아래, LEFT(x-1)=왼쪽 위,
 * ABOVE(z+1)=위, BELOW(z-1)=아래.
 * 솔루션 기호: U R D L T(top) B(bottom).
 */
public enum Direction {
    UP('U', 0, -1, 0),
    RIGHT('R', 1, 0, 0),
    DOWN('D', 0, 1, 0),
    LEFT('L', -1, 0, 0),
    ABOVE('T', 0, 0, 1),
    BELOW('B', 0, 0, -1);

    public final char symbol;
    public final int dx;
    public final int dy;
    public final int dz;

    Direction(char symbol, int dx, int dy, int dz) {
        this.symbol = symbol;
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
    }

    public static Direction fromSymbol(char c) {
        for (Direction d : values()) {
            if (d.symbol == c) return d;
        }
        throw new IllegalArgumentException("알 수 없는 방향 기호: " + c);
    }

    /** 솔루션 문자열에 쓸 수 있는 기호 전체. */
    public static String symbols() {
        StringBuilder sb = new StringBuilder();
        for (Direction d : values()) sb.append(d.symbol);
        return sb.toString();
    }
}
