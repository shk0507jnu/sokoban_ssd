package sokoban.ui;

import sokoban.core.Level;
import sokoban.save.SaveStore;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.List;

/** 레벨 선택 (FR-5.7). 난이도 순 표시, 해금·별·기록을 보여 준다. */
final class LevelSelectScreen implements Screen {
    private static final int CARD_W = 170, CARD_H = 120, GAP = 16, TOP = 110;

    private final App app;
    private final List<Level> levels;
    private int selected;
    private int cols = 4;
    private int scrollRows;
    private Rectangle[] boxes = new Rectangle[0];
    private String status = "";

    LevelSelectScreen(App app, int focus) {
        this.app = app;
        this.levels = app.repo.levels;
        this.selected = Math.max(0, Math.min(focus, levels.size() - 1));
    }

    @Override
    public void paint(Graphics2D g, int w, int h) {
        cols = Math.max(1, (w - 60 + GAP) / (CARD_W + GAP));
        int gridW = cols * CARD_W + (cols - 1) * GAP;
        int left = (w - gridW) / 2;
        int visibleRows = Math.max(1, (h - TOP - 50 + GAP) / (CARD_H + GAP));
        int selRow = selected / cols;
        if (selRow < scrollRows) scrollRows = selRow;
        if (selRow >= scrollRows + visibleRows) scrollRows = selRow - visibleRows + 1;

        g.setColor(Theme.TEXT);
        g.setFont(Theme.font(Font.BOLD, 32f));
        Gfx.centered(g, "레벨 선택", w / 2, 60);
        g.setFont(Theme.font(Font.PLAIN, 14f));
        g.setColor(Theme.TEXT_DIM);
        Gfx.centered(g, "난이도 순 · 이전 레벨을 별 1개 이상으로 클리어하면 해금", w / 2, 86);

        boxes = new Rectangle[levels.size()];
        for (int i = 0; i < levels.size(); i++) {
            int row = i / cols, col = i % cols;
            int x = left + col * (CARD_W + GAP);
            int y = TOP + (row - scrollRows) * (CARD_H + GAP);
            boxes[i] = new Rectangle(x, y, CARD_W, CARD_H);
            if (row < scrollRows || row >= scrollRows + visibleRows) {
                boxes[i] = new Rectangle(-1000, -1000, 0, 0);
                continue;
            }
            card(g, i, x, y);
        }

        g.setFont(Theme.font(Font.PLAIN, 16f));
        g.setColor(Theme.ACCENT);
        Gfx.centered(g, status, w / 2, h - 52);
        g.setColor(Theme.TEXT_DIM);
        g.setFont(Theme.font(Font.PLAIN, 14f));
        Gfx.centered(g, "방향키 이동   Enter 선택   Esc 메인 메뉴", w / 2, h - 24);
    }

    private void card(Graphics2D g, int i, int x, int y) {
        Level lv = levels.get(i);
        boolean unlocked = app.isUnlocked(i);
        SaveStore.Record rec = app.save.get(lv.id);
        boolean sel = i == selected;
        g.setColor(!unlocked ? Theme.PANEL_LOCK : sel ? Theme.PANEL_SEL : Theme.PANEL);
        g.fillRoundRect(x, y, CARD_W, CARD_H, 16, 16);
        if (sel) {
            g.setColor(Theme.ACCENT);
            g.drawRoundRect(x, y, CARD_W, CARD_H, 16, 16);
        }
        g.setColor(unlocked ? Theme.TEXT : Theme.TEXT_DIM);
        g.setFont(Theme.font(Font.BOLD, 28f));
        g.drawString(String.valueOf(i + 1), x + 14, y + 38);
        g.setFont(Theme.font(Font.PLAIN, 15f));
        g.drawString(lv.name, x + 14, y + 62);
        if (!unlocked) {
            g.setColor(Theme.TEXT_DIM);
            g.setFont(Theme.font(Font.BOLD, 15f));
            Gfx.right(g, "잠김", x + CARD_W - 14, y + 36);
            return;
        }
        Gfx.stars(g, x + CARD_W / 2.0, y + 84, 9, rec.bestStars());
        g.setFont(Theme.font(Font.PLAIN, 12f));
        g.setColor(Theme.TEXT_DIM);
        String info = rec.bestStars() > 0
                ? "최소 " + rec.bestMoves() + "수 · " + rec.bestScore() + "점"
                : "최적 " + lv.optimalMoves + "수";
        Gfx.centered(g, info, x + CARD_W / 2, y + 108);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int n = levels.size();
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT, KeyEvent.VK_A -> selected = Math.max(0, selected - 1);
            case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> selected = Math.min(n - 1, selected + 1);
            case KeyEvent.VK_UP, KeyEvent.VK_W -> selected = Math.max(0, selected - cols);
            case KeyEvent.VK_DOWN, KeyEvent.VK_S -> selected = Math.min(n - 1, selected + cols);
            case KeyEvent.VK_ENTER, KeyEvent.VK_SPACE -> open(selected);
            case KeyEvent.VK_ESCAPE -> app.showMainMenu();
            default -> {
            }
        }
        if (e.getKeyCode() != KeyEvent.VK_ENTER && e.getKeyCode() != KeyEvent.VK_SPACE) status = "";
    }

    @Override
    public void mouseMoved(int x, int y) {
        for (int i = 0; i < boxes.length; i++) {
            if (boxes[i].contains(x, y)) selected = i;
        }
    }

    @Override
    public void mouseClicked(int x, int y) {
        for (int i = 0; i < boxes.length; i++) {
            if (boxes[i].contains(x, y)) {
                selected = i;
                open(i);
                return;
            }
        }
    }

    private void open(int i) {
        if (!app.isUnlocked(i)) {
            status = "잠긴 레벨이다. 이전 레벨을 별 1개 이상으로 클리어하세요.";
            return;
        }
        app.startLevel(i);
    }
}
