package sokoban.tool;

import sokoban.config.GameConfig;
import sokoban.core.Level;
import sokoban.level.Difficulty;
import sokoban.level.LevelFormatException;
import sokoban.level.LevelParser;
import sokoban.solver.Solver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * 레벨 디자이너용 검증 도구 (UC-17, UC-18).
 *
 * <pre>
 * java -cp out sokoban.tool.LevelTool [--check] [레벨폴더] [설정파일]
 * </pre>
 * 기본은 솔버로 풀이 가능 여부를 검증하고 optimalMoves / solution 을 파일에 기록한다.
 * {@code --check}는 파일을 고치지 않고 기록된 값이 솔버 결과와 같은지만 확인한다.
 * 마지막에 난이도 곡선(파일 이름 순서 기준)을 검사한다.
 */
public final class LevelTool {
    static final int MAX_STATES = 3_000_000;
    static final long TIME_LIMIT_MS = 60_000;

    public static void main(String[] args) throws IOException {
        boolean check = false;
        List<String> rest = new ArrayList<>();
        for (String a : args) {
            if (a.equals("--check")) check = true;
            else rest.add(a);
        }
        Path dir = Paths.get(rest.size() > 0 ? rest.get(0) : "levels");
        Path cfgFile = Paths.get(rest.size() > 1 ? rest.get(1) : "config/game.properties");
        GameConfig cfg = GameConfig.load(cfgFile);
        cfg.warnings.forEach(w -> System.out.println("[설정 경고] " + w));

        List<Path> files = new ArrayList<>();
        try (Stream<Path> s = Files.list(dir)) {
            s.filter(p -> p.getFileName().toString().endsWith(".txt")).sorted().forEach(files::add);
        }

        int failures = 0;
        List<String> names = new ArrayList<>();
        List<Double> ds = new ArrayList<>();
        for (Path f : files) {
            String name = f.getFileName().toString();
            List<String> lines = Files.readAllLines(f, StandardCharsets.UTF_8);
            Level lv;
            try {
                lv = LevelParser.parse(name, lines);
            } catch (LevelFormatException e) {
                System.out.println("[형식 오류] " + e.getMessage());
                failures++;
                continue;
            }
            Solver.Result r = Solver.solve(lv, MAX_STATES, TIME_LIMIT_MS);
            switch (r.status()) {
                case UNSOLVABLE -> {
                    System.out.printf("[풀이 불가] %s (탐색 %d)%n", name, r.statesExplored());
                    failures++;
                    continue;
                }
                case LIMIT_EXCEEDED -> {
                    System.out.printf("[검증 불가] %s 탐색 한도 초과 (%d ms)%n", name, r.millis());
                    failures++;
                    continue;
                }
                default -> {
                }
            }
            Level solved = lv.withSolution(r.moves(), r.path());
            double d = Difficulty.score(solved, cfg);
            names.add(name);
            ds.add(d);
            boolean same = lv.optimalMoves == r.moves() && lv.solution.equals(r.path());
            if (check) {
                if (!same) {
                    System.out.printf("[불일치] %s 기록=%d 솔버=%d%n", name, lv.optimalMoves, r.moves());
                    failures++;
                    continue;
                }
            } else if (!same) {
                Files.write(f, rewrite(lines, r.moves(), r.path()), StandardCharsets.UTF_8);
            }
            System.out.printf("[OK] %-22s optimal=%-4d blocks=%d D=%.1f states=%d %dms%s%n",
                    name, r.moves(), lv.blockCount(), d, r.statesExplored(), r.millis(),
                    (!check && !same) ? "  (기록 갱신)" : "");
        }

        // 난이도 곡선 검증 (FR-5.6): 파일 순서 = 의도한 순서
        for (int i = 1; i < ds.size(); i++) {
            double delta = ds.get(i) - ds.get(i - 1);
            if (delta < 0) {
                System.out.printf("[곡선 경고] %s → %s : D 감소 (%.1f)%n", names.get(i - 1), names.get(i), delta);
            } else if (delta > cfg.maxDeltaD) {
                System.out.printf("[곡선 경고] %s → %s : D 급등 (+%.1f > %.1f)%n",
                        names.get(i - 1), names.get(i), delta, cfg.maxDeltaD);
            }
        }
        System.out.println(failures == 0 ? "모든 레벨 검증 완료." : "실패 " + failures + "건.");
        if (failures > 0) System.exit(1);
    }

    /** optimalMoves / solution 줄을 갱신(없으면 name 또는 id 줄 뒤에 삽입). 나머지는 그대로 둔다. */
    static List<String> rewrite(List<String> lines, int moves, String path) {
        List<String> out = new ArrayList<>();
        boolean inMeta = true;
        int insertAt = -1;
        for (String raw : lines) {
            String l = raw.endsWith("\r") ? raw.substring(0, raw.length() - 1) : raw;
            if (l.startsWith("layer")) inMeta = false;
            if (inMeta && (l.startsWith("optimalMoves:") || l.startsWith("solution:"))) continue;
            out.add(l);
            if (inMeta && (l.startsWith("name:") || (l.startsWith("id:") && insertAt < 0))) insertAt = out.size();
        }
        if (insertAt < 0) insertAt = 0;
        out.add(insertAt, "solution: " + path);
        out.add(insertAt, "optimalMoves: " + moves);
        return out;
    }
}
