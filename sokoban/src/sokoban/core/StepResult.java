package sokoban.core;

/**
 * 유효한 입력 1회의 결과.
 *
 * @param state          처리 후 상태
 * @param dir            입력 방향
 * @param playerFrom     플레이어 이전 위치
 * @param playerTo       플레이어 새 위치
 * @param blockFrom      민 블록의 이전 위치(안 밀었으면 -1)
 * @param blockTo        민 블록의 새 위치(안 밀었으면 -1)
 * @param blockVanished  블록이 구멍에 들어가 소멸했는가
 * @param cleared        이 턴을 끝낸 뒤 클리어 조건을 만족하는가
 */
public record StepResult(GameState state, Direction dir, int playerFrom, int playerTo,
                         int blockFrom, int blockTo, boolean blockVanished, boolean cleared) {
    public boolean pushed() {
        return blockFrom >= 0;
    }
}
