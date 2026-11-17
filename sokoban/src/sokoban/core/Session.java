package sokoban.core;

import sokoban.config.GameConfig;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * 한 레벨의 플레이 세션: 현재 상태, 되돌리기/다시 하기 기록, 카운터.
 * 구현은 상태 스냅샷 스택 방식이다(FR-3.7). {@link GameState}가 불변이라 스냅샷 비용이 작다.
 * Swing에 의존하지 않는다.
 */
public final class Session {
    private final Level level;
    private final GameConfig config;
    private GameState state;
    private final Deque<GameState> undoStack = new ArrayDeque<>();
    private final Deque<GameState> redoStack = new ArrayDeque<>();
    private int undoCount;
    private boolean cleared;

    public Session(Level level, GameConfig config) {
        this.level = level;
        this.config = config;
        this.state = level.initial;
    }

    public Level level() {
        return level;
    }

    public GameState state() {
        return state;
    }

    /** 이동 횟수 = 유효 입력 누적. 되돌리면 1 감소 (FR-1.6, FR-3.3). */
    public int moves() {
        return undoStack.size();
    }

    public int undoCount() {
        return undoCount;
    }

    public boolean isCleared() {
        return cleared;
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    /** 방향 입력. 막혔거나 클리어 후면 null (상태·횟수 불변). */
    public StepResult move(Direction dir) {
        if (cleared) return null;
        StepResult r = Engine.step(level, state, dir);
        if (r == null) return null;
        undoStack.push(state);
        redoStack.clear();
        state = r.state();
        cleared = r.cleared();
        return r;
    }

    public boolean undo() {
        if (cleared || undoStack.isEmpty()) return false;
        redoStack.push(state);
        state = undoStack.pop();
        undoCount++;
        return true;
    }

    public boolean redo() {
        if (cleared || redoStack.isEmpty()) return false;
        undoStack.push(state);
        state = redoStack.pop();
        return true;
    }

    /** 처음부터 다시 (이동·되돌리기 횟수 0). */
    public void reset() {
        state = level.initial;
        undoStack.clear();
        redoStack.clear();
        undoCount = 0;
        cleared = false;
    }

    public int score() {
        return Scoring.score(level.optimalMoves, moves(), undoCount, config.kappa);
    }

    public int stars() {
        double r3 = level.star3Ratio != null ? level.star3Ratio : config.star3Ratio;
        double r2 = level.star2Ratio != null ? level.star2Ratio : config.star2Ratio;
        return Scoring.stars(level.optimalMoves, moves(), r3, r2);
    }
}
