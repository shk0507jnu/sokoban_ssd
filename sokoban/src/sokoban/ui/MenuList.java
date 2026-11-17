package sokoban.ui;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;

/** 세로 메뉴 목록. 키보드(↑↓/W S, Enter/Space)와 마우스를 모두 받는다. */
final class MenuList {
    private final String[] items;
    private int selected;
    private final Rectangle[] boxes;

    MenuList(String... items) {
        this.items = items;
        this.boxes = new Rectangle[items.length];
        for (int i = 0; i < boxes.length; i++) boxes[i] = new Rectangle();
    }

    void select(int i) {
        selected = Math.floorMod(i, items.length);
    }

    /** @return 선택 확정된 항목 번호, 없으면 -1 */
    int key(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP, KeyEvent.VK_W -> select(selected - 1);
            case KeyEvent.VK_DOWN, KeyEvent.VK_S -> select(selected + 1);
            case KeyEvent.VK_ENTER, KeyEvent.VK_SPACE -> {
                return selected;
            }
            default -> {
            }
        }
        return -1;
    }

    void hover(int x, int y) {
        for (int i = 0; i < boxes.length; i++) {
            if (boxes[i].contains(x, y)) selected = i;
        }
    }

    int click(int x, int y) {
        for (int i = 0; i < boxes.length; i++) {
            if (boxes[i].contains(x, y)) {
                selected = i;
                return i;
            }
        }
        return -1;
    }

    /** cx 중심, top부터 아래로 항목을 그린다. */
    void paint(Graphics2D g, int cx, int top, int itemW, int itemH, int gap) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setFont(Theme.font(java.awt.Font.BOLD, itemH * 0.45f));
        FontMetrics fm = g.getFontMetrics();
        for (int i = 0; i < items.length; i++) {
            int y = top + i * (itemH + gap);
            boxes[i].setBounds(cx - itemW / 2, y, itemW, itemH);
            boolean sel = i == selected;
            g.setColor(sel ? Theme.PANEL_SEL : Theme.shade(Theme.PANEL, 0.8));
            g.fillRoundRect(cx - itemW / 2, y, itemW, itemH, 16, 16);
            if (sel) {
                g.setColor(Theme.ACCENT);
                g.drawRoundRect(cx - itemW / 2, y, itemW, itemH, 16, 16);
            }
            g.setColor(sel ? Color.WHITE : Theme.TEXT);
            int tw = fm.stringWidth(items[i]);
            g.drawString(items[i], cx - tw / 2, y + (itemH + fm.getAscent() - fm.getDescent()) / 2);
        }
    }
}
