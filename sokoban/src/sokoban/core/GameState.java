package sokoban.core;

import java.util.Arrays;

/**
 * 불변 게임 상태: 플레이어 위치, 블록 위치 집합, 아직 메워지지 않은 구멍 집합.
 * 위치는 {@link Level#idx(int, int, int)} 인덱스. 되돌리기와 솔버가 공통으로 쓴다(NFR-3).
 */
public final class GameState {
    public final int player;
    private final int[] blocks; // 오름차순
    private final int[] holes;  // 오름차순, 남은 구멍
    private final int hash;

    private GameState(int player, int[] sortedBlocks, int[] sortedHoles) {
        this.player = player;
        this.blocks = sortedBlocks;
        this.holes = sortedHoles;
        this.hash = 31 * (31 * player + Arrays.hashCode(sortedBlocks)) + Arrays.hashCode(sortedHoles);
    }

    public static GameState of(int player, int[] blocks, int[] holes) {
        int[] b = blocks.clone();
        int[] h = holes.clone();
        Arrays.sort(b);
        Arrays.sort(h);
        return new GameState(player, b, h);
    }

    public int blockCount() {
        return blocks.length;
    }

    public int holeCount() {
        return holes.length;
    }

    public int block(int i) {
        return blocks[i];
    }

    public int hole(int i) {
        return holes[i];
    }

    public boolean hasBlock(int idx) {
        return Arrays.binarySearch(blocks, idx) >= 0;
    }

    public boolean isHole(int idx) {
        return Arrays.binarySearch(holes, idx) >= 0;
    }

    public GameState withPlayer(int newPlayer) {
        return new GameState(newPlayer, blocks, holes);
    }

    /** 플레이어를 옮기면서 블록 {@code from}을 {@code to}로 옮긴다. */
    public GameState withPush(int newPlayer, int from, int to) {
        int[] nb = blocks.clone();
        int pos = Arrays.binarySearch(nb, from);
        nb[pos] = to;
        Arrays.sort(nb);
        return new GameState(newPlayer, nb, holes);
    }

    /** 플레이어를 옮기면서 블록 {@code from}을 구멍 {@code hole}에 밀어 넣는다(블록·구멍 모두 소멸). */
    public GameState withFill(int newPlayer, int from, int hole) {
        int[] nb = new int[blocks.length - 1];
        int k = 0;
        for (int b : blocks) {
            if (b != from) nb[k++] = b;
        }
        int[] nh = new int[holes.length - 1];
        k = 0;
        for (int h : holes) {
            if (h != hole) nh[k++] = h;
        }
        return new GameState(newPlayer, nb, nh);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GameState other)) return false;
        return hash == other.hash && player == other.player
                && Arrays.equals(blocks, other.blocks) && Arrays.equals(holes, other.holes);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public String toString() {
        return "GameState{player=" + player + ", blocks=" + Arrays.toString(blocks)
                + ", holes=" + Arrays.toString(holes) + "}";
    }
}
