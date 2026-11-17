package sokoban.level;

import sokoban.config.GameConfig;
import sokoban.core.Level;

import java.util.Map;

/** 난이도 점수 D (FR-5.3). 기본 게임에는 기믹이 없어 기믹 항은 0이다. */
public final class Difficulty {
    private Difficulty() {
    }

    /** D = Σ(w_g×n_g) + α·optimalMoves + β·blockCount + γ·(기믹 종류 수−1) + δ·(사용 층 수−1) */
    public static double score(Level lv, GameConfig cfg) {
        double sum = 0;
        int kinds = 0;
        for (Map.Entry<String, Integer> e : lv.gimmicks.entrySet()) {
            if (e.getValue() > 0) {
                kinds++;
                sum += cfg.weightOf(e.getKey()) * e.getValue();
            }
        }
        sum += cfg.alpha * Math.max(0, lv.optimalMoves);
        sum += cfg.beta * lv.blockCount();
        sum += cfg.gamma * Math.max(0, kinds - 1);
        sum += cfg.delta * Math.max(0, lv.usedLayers() - 1);
        return sum;
    }
}
