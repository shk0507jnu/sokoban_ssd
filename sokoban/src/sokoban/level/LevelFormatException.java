package sokoban.level;

/** 레벨 파일 검증 오류. 어느 파일의 몇 번째 줄이 문제인지 담는다 (NFR-8). */
public final class LevelFormatException extends Exception {
    public final String source;
    public final int line; // 0이면 파일 전체

    public LevelFormatException(String source, int line, String message) {
        super(source + (line > 0 ? ":" + line : "") + ": " + message);
        this.source = source;
        this.line = line;
    }
}
