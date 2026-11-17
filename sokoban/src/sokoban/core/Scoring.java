package sokoban.core;

/** 점수·별 계산 (FR-4.3, FR-4.4). */
public final class Scoring {
    private Scoring() {
    }

    /** score = max(0, round(1000 × min(1, optimal/moves)) − κ × undoCount) */
    public static int score(int optimal, int moves, int undoCount, int kappa) {
        if (moves <= 0 || optimal <= 0) return 0;
        double efficiency = Math.min(1.0, (double) optimal / moves);
        long s = Math.round(1000 * efficiency) - (long) kappa * undoCount;
        return (int) Math.max(0, s);
    }

    /** ★★★: moves ≤ ⌈optimal×r3⌉, ★★: moves ≤ ⌈optimal×r2⌉, 그 외 클리어는 ★. */
    public static int stars(int optimal, int moves, double star3Ratio, double star2Ratio) {
        if (moves <= limit(optimal, star3Ratio)) return 3;
        if (moves <= limit(optimal, star2Ratio)) return 2;
        return 1;
    }

    /** ⌈optimal × ratio⌉ (부동소수점 오차 보정) */
    public static int limit(int optimal, double ratio) {
        return (int) Math.ceil(optimal * ratio - 1e-9);
    }
}
