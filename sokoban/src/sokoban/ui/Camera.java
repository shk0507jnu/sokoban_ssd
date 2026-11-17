package sokoban.ui;

import sokoban.core.Direction;

/**
 * 아이소메트릭 카메라. 수직축(z)을 중심으로 한 회전(yaw)과 기울기(pitch)를 가진다.
 * yaw 0 이 기본 시점이다. 키보드는 90° 단위로 부드럽게 돌리고, 마우스 드래그는 자유롭게 돌린 뒤
 * 놓으면 가장 가까운 90° 배수로 정렬된다. 이동 키는 항상 "화면 기준"으로 해석한다.
 */
public final class Camera {
    public static final double DEFAULT_PITCH = 30;
    public static final double MIN_PITCH = 10, MAX_PITCH = 80;

    /** 현재(화면에 보이는) yaw, 도 단위. */
    public double yaw;
    /** 목표 yaw. 키 회전 후 yaw가 여기로 수렴한다. */
    public double targetYaw;
    public double pitch = DEFAULT_PITCH;
    private boolean dragging;
    private double dragStartYaw, dragStartPitch;

    /** 90°의 배수만큼 회전 (+1 = 반시계 방향으로 장면이 돈다). */
    public void rotateQuarter(int turns) {
        targetYaw = Math.round(targetYaw / 90.0) * 90.0 + 90.0 * turns;
    }

    public void resetView() {
        targetYaw = Math.round(targetYaw / 360.0) * 360.0; // 가장 가까운 기본 방향으로 짧게 돌아간다
        pitch = DEFAULT_PITCH;
    }

    public void beginDrag() {
        dragging = true;
        dragStartYaw = yaw;
        dragStartPitch = pitch;
        targetYaw = yaw;
    }

    /** 드래그 시작점 기준 이동량(px). */
    public void drag(int dxPixels, int dyPixels) {
        if (!dragging) return;
        yaw = dragStartYaw + dxPixels * 0.45;
        targetYaw = yaw;
        pitch = Math.max(MIN_PITCH, Math.min(MAX_PITCH, dragStartPitch + dyPixels * 0.3));
    }

    public void endDrag() {
        if (!dragging) return;
        dragging = false;
        targetYaw = Math.round(yaw / 90.0) * 90.0;
    }

    public boolean isDragging() {
        return dragging;
    }

    /** 목표 yaw로 부드럽게 수렴. */
    public void update(long dtMillis) {
        if (dragging) return;
        double diff = targetYaw - yaw;
        if (Math.abs(diff) < 0.05) {
            yaw = targetYaw;
            return;
        }
        yaw += diff * (1 - Math.exp(-dtMillis / 70.0));
    }

    public boolean isRotating() {
        return dragging || Math.abs(targetYaw - yaw) >= 0.05;
    }

    /** 키 방향(화면 기준)을 현재 시점의 격자 방향으로 바꾼다. */
    public Direction mapKey(Direction key) {
        return mapKey(key, dragging ? yaw : targetYaw, pitch);
    }

    /**
     * 화면 기준 키 방향 → 격자 방향.
     * UP=화면 오른쪽 위, RIGHT=오른쪽 아래, DOWN=왼쪽 아래, LEFT=왼쪽 위. 위/아래(z) 키는 그대로.
     * 네 격자 방향을 화면에 투영해 키가 가리키는 쪽과 가장 가까운 것을 고른다.
     */
    public static Direction mapKey(Direction key, double yawDeg, double pitchDeg) {
        if (key == Direction.ABOVE || key == Direction.BELOW) return key;
        double kx, ky;
        switch (key) {
            case UP -> { kx = 1; ky = -1; }
            case RIGHT -> { kx = 1; ky = 1; }
            case DOWN -> { kx = -1; ky = 1; }
            default -> { kx = -1; ky = -1; }
        }
        double psi = Math.toRadians(45 + yawDeg);
        double sinP = Math.sin(Math.toRadians(pitchDeg));
        Direction best = Direction.UP;
        double bestDot = -Double.MAX_VALUE;
        for (Direction d : new Direction[]{Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT}) {
            double x1 = d.dx * Math.cos(psi) - d.dy * Math.sin(psi);
            double y1 = d.dx * Math.sin(psi) + d.dy * Math.cos(psi);
            double dot = kx * x1 + ky * y1 * sinP;
            if (dot > bestDot) {
                bestDot = dot;
                best = d;
            }
        }
        return best;
    }
}
