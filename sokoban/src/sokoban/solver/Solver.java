package sokoban.solver;

import sokoban.core.Direction;
import sokoban.core.Engine;
import sokoban.core.GameState;
import sokoban.core.Level;
import sokoban.core.StepResult;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

/**
 * BFS 솔버 (FR-5.2, FR-5.9). 게임 규칙 엔진 {@link Engine}을 그대로 재사용하고
 * {@link GameState}의 equals/hashCode로 중복 상태를 건너뛴다.
 * BFS이므로 처음 찾은 해가 최소 유효 입력 횟수다.
 */
public final class Solver {
    public enum Status { SOLVED, UNSOLVABLE, LIMIT_EXCEEDED }

    public record Result(Status status, int moves, String path, int statesExplored, long millis) {
    }

    private static final class Node {
        final GameState state;
        final Node parent;
        final Direction dir;
        final int depth;

        Node(GameState state, Node parent, Direction dir, int depth) {
            this.state = state;
            this.parent = parent;
            this.dir = dir;
            this.depth = depth;
        }
    }

    private Solver() {
    }

    public static Result solve(Level lv, int maxStates, long timeLimitMillis) {
        long t0 = System.currentTimeMillis();
        Set<GameState> seen = new HashSet<>();
        ArrayDeque<Node> queue = new ArrayDeque<>();
        Node root = new Node(lv.initial, null, null, 0);
        seen.add(lv.initial);
        queue.add(root);
        int explored = 0;

        while (!queue.isEmpty()) {
            Node n = queue.poll();
            explored++;
            if ((explored & 1023) == 0 && System.currentTimeMillis() - t0 > timeLimitMillis) {
                return new Result(Status.LIMIT_EXCEEDED, -1, "", explored, System.currentTimeMillis() - t0);
            }
            for (Direction d : Direction.values()) {
                StepResult r = Engine.step(lv, n.state, d);
                if (r == null) continue;
                Node child = new Node(r.state(), n, d, n.depth + 1);
                if (r.cleared()) {
                    return new Result(Status.SOLVED, child.depth, pathOf(child), explored,
                            System.currentTimeMillis() - t0);
                }
                if (seen.add(r.state())) {
                    if (seen.size() > maxStates) {
                        return new Result(Status.LIMIT_EXCEEDED, -1, "", explored, System.currentTimeMillis() - t0);
                    }
                    queue.add(child);
                }
            }
        }
        return new Result(Status.UNSOLVABLE, -1, "", explored, System.currentTimeMillis() - t0);
    }

    private static String pathOf(Node n) {
        StringBuilder sb = new StringBuilder();
        for (Node c = n; c.parent != null; c = c.parent) sb.append(c.dir.symbol);
        return sb.reverse().toString();
    }
}
