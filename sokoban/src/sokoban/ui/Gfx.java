package sokoban.ui;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;

/** 작은 그리기 도우미. */
final class Gfx {
    private Gfx() {
    }

    static void centered(Graphics2D g, String s, int cx, int baselineY) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(s, cx - fm.stringWidth(s) / 2, baselineY);
    }

    static void right(Graphics2D g, String s, int rightX, int baselineY) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(s, rightX - fm.stringWidth(s), baselineY);
    }

    /** 별 하나. r = 바깥 반지름. */
    static void star(Graphics2D g, double cx, double cy, double r, boolean filled) {
        Path2D p = new Path2D.Double();
        for (int i = 0; i < 10; i++) {
            double ang = -Math.PI / 2 + i * Math.PI / 5;
            double rad = (i % 2 == 0) ? r : r * 0.42;
            double x = cx + Math.cos(ang) * rad;
            double y = cy + Math.sin(ang) * rad;
            if (i == 0) p.moveTo(x, y);
            else p.lineTo(x, y);
        }
        p.closePath();
        if (filled) {
            g.setColor(Theme.ACCENT);
            g.fill(p);
            g.setColor(Theme.shade(Theme.ACCENT, 0.65));
        } else {
            g.setColor(new Color(255, 255, 255, 40));
            g.fill(p);
            g.setColor(new Color(255, 255, 255, 90));
        }
        g.draw(p);
    }

    static void stars(Graphics2D g, double cx, double cy, double r, int filled) {
        double gap = r * 2.3;
        for (int i = 0; i < 3; i++) star(g, cx + (i - 1) * gap, cy, r, i < filled);
    }
}
