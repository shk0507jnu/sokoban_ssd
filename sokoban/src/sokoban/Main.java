package sokoban;

import sokoban.ui.App;

import javax.swing.SwingUtilities;
import java.nio.file.Path;

/** 실행 진입점. {@code -Dsokoban.home=폴더}로 levels/ 와 config/ 가 있는 위치를 바꿀 수 있다. */
public final class Main {
    public static void main(String[] args) {
        Path home = Path.of(System.getProperty("sokoban.home", "."));
        SwingUtilities.invokeLater(() -> new App(home).start());
    }
}
