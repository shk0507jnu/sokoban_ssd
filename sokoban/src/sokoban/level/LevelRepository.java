package sokoban.level;

import sokoban.config.GameConfig;
import sokoban.core.Level;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/** 레벨 디렉터리를 읽어 난이도(D) 오름차순 목록을 만든다 (FR-5.5). 오류 레벨은 목록에서 제외하고 오류를 모은다. */
public final class LevelRepository {
    public final List<Level> levels;
    public final List<Double> difficulty;
    public final List<String> errors;

    private LevelRepository(List<Level> levels, List<Double> difficulty, List<String> errors) {
        this.levels = levels;
        this.difficulty = difficulty;
        this.errors = errors;
    }

    public static LevelRepository load(Path dir, GameConfig cfg) {
        List<String> errors = new ArrayList<>();
        List<Level> loaded = new ArrayList<>();
        List<Path> files = new ArrayList<>();
        if (Files.isDirectory(dir)) {
            try (Stream<Path> s = Files.list(dir)) {
                s.filter(p -> p.getFileName().toString().endsWith(".txt")).sorted().forEach(files::add);
            } catch (IOException e) {
                errors.add(dir + ": 레벨 폴더를 읽지 못했다 (" + e.getMessage() + ")");
            }
        } else {
            errors.add(dir + ": 레벨 폴더가 없다.");
        }
        Set<String> ids = new HashSet<>();
        for (Path f : files) {
            String name = f.getFileName().toString();
            try {
                List<String> lines = Files.readAllLines(f, StandardCharsets.UTF_8);
                Level lv = LevelParser.parse(name, lines);
                if (lv.optimalMoves < 0) {
                    throw new LevelFormatException(name, 0, "optimalMoves가 없다. LevelTool로 검증하기 전에는 출고할 수 없다.");
                }
                if (!ids.add(lv.id)) {
                    throw new LevelFormatException(name, 0, "레벨 id '" + lv.id + "'가 중복되었다.");
                }
                loaded.add(lv);
            } catch (LevelFormatException e) {
                errors.add(e.getMessage());
            } catch (IOException e) {
                errors.add(name + ": 파일을 읽지 못했다 (" + e.getMessage() + ")");
            }
        }
        // D 오름차순, 같으면 파일 이름 순서(안정 정렬)
        List<Level> sorted = new ArrayList<>(loaded);
        sorted.sort(Comparator.comparingDouble(l -> Difficulty.score(l, cfg)));
        List<Double> ds = new ArrayList<>();
        for (Level l : sorted) ds.add(Difficulty.score(l, cfg));
        return new LevelRepository(List.copyOf(sorted), List.copyOf(ds), List.copyOf(errors));
    }
}
