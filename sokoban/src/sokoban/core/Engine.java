package sokoban.core;

/**
 * 규칙 엔진. Swing에 의존하지 않으며 상태를 바꾸지 않는 순수 함수다(NFR-1, NFR-2).
 *
 * <p>3D 격자 규칙: 중력·바닥이 없어 모든 빈 칸은 공중이며, 플레이어와 블록 모두 x, y, z 6방향으로 움직인다.
 * 턴 해석 순서(FR-2.6, 기본 게임 부분): 플레이어 이동 → 밀기 → (구멍 채움) → 클리어 판정.
 * 추가 기믹(포탈 등)은 밀기와 클리어 판정 사이에 단계로 끼워 넣는다.
 */
public final class Engine {
    private Engine() {
    }

    /**
     * 입력 1회를 처리한다. 플레이어는 6방향(x, y, z)으로 움직이고 블록도 같은 6방향으로 밀린다.
     *
     * @return 상태가 변했으면 결과, 막혀서 변화가 없으면 null (이동 횟수에 포함하지 않는다. FR-1.4)
     */
    public static StepResult step(Level lv, GameState s, Direction dir) {
        int from = s.player;
        int to = lv.neighbor(from, dir);
        if (to < 0 || !walkable(lv, s, to)) return null;

        if (!s.hasBlock(to)) {
            GameState next = s.withPlayer(to);
            return new StepResult(next, dir, from, to, -1, -1, false, isCleared(lv, next));
        }

        // 블록 밀기: 한 번에 1개만, 뒤가 막히면 불가 (FR-1.3)
        int behind = lv.neighbor(to, dir);
        if (behind < 0 || s.hasBlock(behind)) return null;
        TileType t = lv.tile(behind);
        if (t == TileType.WALL) return null;

        if (t == TileType.HOLE && s.isHole(behind)) {
            GameState next = s.withFill(to, to, behind);
            return new StepResult(next, dir, from, to, to, behind, true, isCleared(lv, next));
        }
        GameState next = s.withPush(to, to, behind);
        return new StepResult(next, dir, from, to, to, behind, false, isCleared(lv, next));
    }

    /** 플레이어가 들어갈 수 있는 칸인가(블록 점유는 여기서 따지지 않는다). */
    private static boolean walkable(Level lv, GameState s, int idx) {
        return switch (lv.tile(idx)) {
            // ICE·PORTAL: 플레이어는 평범하게 걷는다. 빙판 미끄러짐과 포탈 전송은 후속 엔진 PR에서 구현한다(지금은 빈 칸과 같다).
            case EMPTY, GOAL, ICE, PORTAL -> true;
            case HOLE -> !s.isHole(idx); // 메워진 구멍은 평범한 빈 칸
            case WALL -> false;
        };
    }

    /** 모든 목표 지점에 블록이 놓였는가 (FR-1.5). */
    public static boolean isCleared(Level lv, GameState s) {
        if (lv.goals.length == 0) return false;
        for (int g : lv.goals) {
            if (!s.hasBlock(g)) return false;
        }
        return true;
    }
}
