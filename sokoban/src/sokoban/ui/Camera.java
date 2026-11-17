package sokoban.ui;

import sokoban.core.Direction;

/**
 * 아이소메트릭 카메라. 수직축(z)을 중심으로 한 회전(yaw)과 고정 기울기(pitch)를 가진다.
 * yaw 0 이 기본 시점이다. 시점은 45° 단위(8방향)로 부드럽게 돌며, 마우스로는 돌리지 않는다.
 * 이동 키는 항상 "화면 기준"으로 해석한다.
 */
public final class Camera {
    /** 시점 한 칸의 회전각(도). 8방향이므로 45°. */
    public static final double STEP_DEG = 45;
    public static final double DEFAULT_PITCH = 30;

    /** 화면 기준 평면 키 4개. 격자 회전은 이 배열의 인덱스를 돌려 계산한다. */
    private static final Direction[] PLANAR = {Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT};

    /** 현재(화면에 보이는) yaw, 도 단위. */
    public double yaw;
    /** 목표 yaw. 키 회전 후 yaw가 여기로 수렴한다. */
    public double targetYaw;
    public double pitch = DEFAULT_PITCH;

    /** 45°씩 회전 (+1 = 시점이 왼쪽으로 돈다. 화면 속 장면은 시계 방향으로 돈다). */
    public void rotateStep(int steps) {
        targetYaw = Math.round(targetYaw / STEP_DEG) * STEP_DEG + STEP_DEG * steps;
    }

    public void resetView() {
        targetYaw = Math.round(targetYaw / 360.0) * 360.0; // 가장 가까운 기본 방향으로 짧게 돌아간다
        pitch = DEFAULT_PITCH;
    }

    /** 목표 yaw로 부드럽게 수렴. */
    public void update(long dtMillis) {
        double diff = targetYaw - yaw;
        if (Math.abs(diff) < 0.05) {
            yaw = targetYaw;
            return;
        }
        yaw += diff * (1 - Math.exp(-dtMillis / 70.0));
    }

    public boolean isRotating() {
        return Math.abs(targetYaw - yaw) >= 0.05;
    }

    /** 키 방향(화면 기준)을 현재 시점의 격자 방향으로 바꾼다. */
    public Direction mapKey(Direction key) {
        return mapKey(key, targetYaw);
    }

    /**
     * 화면 기준 키 방향 → 격자 방향.
     * UP=화면 오른쪽 위, RIGHT=오른쪽 아래, DOWN=왼쪽 아래, LEFT=왼쪽 위. 위/아래(z) 키는 그대로.
     *
     * <p>시점이 45° 단위라 4방향 이동을 45°씩 그대로 돌릴 수는 없다(4방향을 반 칸 돌릴 수 없음).
     * 그래서 시점을 가장 가까운 90° 배수로 양자화하고(45°·135°·225°·315° 같은 반 걸음 시점은
     * 다음 90° 쪽으로 넘긴다) 그만큼 격자 방향을 90°씩 돌린다. 이렇게 하면 어떤 시점에서도
     * 네 키가 서로 다른 방향을 가리키고(전단사), 키가 가리키는 화면 방향과 실제로 움직이는
     * 화면 방향이 가장 잘 맞는다.
     * <ul>
     *   <li>90° 배수 시점: 키가 가리키는 화면 대각선과 격자 방향이 정확히 일치한다.</li>
     *   <li>45° 반 걸음 시점: 격자 축이 화면 축과 나란해지므로 W/A/S/D가 화면 위/왼쪽/아래/오른쪽
     *       방향으로 그대로 움직인다.</li>
     * </ul>
     * 기울기(pitch)는 어느 방향을 고르는지에 영향을 주지 않는다(회전은 격자 위에서 일어난다).
     */
    public static Direction mapKey(Direction key, double yawDeg) {
        if (key == Direction.ABOVE || key == Direction.BELOW) return key;
        return PLANAR[Math.floorMod(indexOf(key) - quarterTurns(yawDeg), 4)];
    }

    /** yaw를 90° 단위 회전 수(0~3)로 양자화한다. 45° 경계는 다음 90° 방향으로 넘긴다. */
    static int quarterTurns(double yawDeg) {
        return Math.floorMod((int) Math.floor(yawDeg / 90.0 + 0.5), 4);
    }

    private static int indexOf(Direction d) {
        for (int i = 0; i < PLANAR.length; i++) {
            if (PLANAR[i] == d) return i;
        }
        return 0; // 위/아래(z)는 mapKey에서 먼저 걸러진다
    }
}
