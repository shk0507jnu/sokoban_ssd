package sokoban.ui;

import sokoban.core.Direction;

/**
 * 아이소메트릭 카메라. 수직축(z)을 중심으로 한 회전(yaw)과 기울기(pitch)를 가진다.
 * yaw 0°, pitch 30° 가 기본 3D 시점이다. 시점은 45° 단위(8방향)로 부드럽게 돌며, 마우스로는 돌리지 않는다.
 * 이동 키는 항상 "화면 기준"으로 해석한다.
 *
 * <p>2D 탑뷰(&lt;b&gt;yaw -45°, pitch 90°&lt;/b&gt;)는 기울기가 90°라 z축이 화면에서 사라지고,
 * yaw가 -45°라 격자 축이 화면 축과 정확히 나란해진다. 그래서 한 층짜리 레벨을 2D 소코반처럼
 * 반듯한 정사각 타일로 보여 줄 수 있다. 탑뷰 각도는 {@link sokoban.ui.IsoRenderer}의 투영식
 * (psi = 45° + yaw 이므로 psi = 0°)에서 나온다.
 *
 * <p>시점은 목표값(targetYaw, targetPitch)을 향해 매 프레임 지수 보간으로 수렴한다. 첫 3D 레벨에
 * 들어갈 때는 탑뷰에서 3D 시점으로 {@link #EXPAND_MS} 동안 이징하며 확장하는 연출을 따로 둔다
 * ({@link #expandFromTopView()}).
 */
public final class Camera {
    /** 시점 한 칸의 회전각(도). 8방향이므로 45°. */
    public static final double STEP_DEG = 45;
    /** 기본 3D 시점의 기울기(도). */
    public static final double DEFAULT_PITCH = 30;
    /** 2D 탑뷰의 기울기(도). 이 값에서는 z축이 화면에서 완전히 사라진다. */
    public static final double TOP_PITCH = 90;
    /** 2D 탑뷰의 yaw(도). psi = 45° + yaw = 0° 이므로 격자 축이 화면 축과 나란하다. */
    public static final double TOP_YAW = -45;
    /** 첫 3D 레벨 진입 연출(탑뷰 → 3D 시점)의 길이(ms). */
    public static final long EXPAND_MS = 900;

    /** 수렴 판정·스냅 반경(도). */
    private static final double SNAP_DEG = 0.05;
    /** yaw 보간 시상수(ms). */
    private static final double YAW_SMOOTH_MS = 70;
    /** pitch 보간 시상수(ms). 시점 초기화(V)가 기울기까지 되돌아올 때 쓴다. */
    private static final double PITCH_SMOOTH_MS = 150;

    /** 화면 기준 평면 키 4개. 격자 회전은 이 배열의 인덱스를 돌려 계산한다. */
    private static final Direction[] PLANAR = {Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT};

    /** 현재(화면에 보이는) yaw, 도 단위. */
    public double yaw;
    /** 목표 yaw. 키 회전 후 yaw가 여기로 수렴한다. */
    public double targetYaw;
    /** 현재(화면에 보이는) 기울기, 도 단위. */
    public double pitch = DEFAULT_PITCH;
    /** 목표 기울기. 3D 확장 연출이 여기로 수렴한다. */
    public double targetPitch = DEFAULT_PITCH;

    /** 진행 중인 확장 연출의 경과(ms). -1이면 연출 없음. */
    private long expandElapsed = -1;

    /** 45°씩 회전 (+1 = 시점이 왼쪽으로 돈다. 화면 속 장면은 시계 방향으로 돈다). */
    public void rotateStep(int steps) {
        finishExpansion(); // 연출 중에 시점을 조작하면 연출을 끊고 바로 3D에서 돈다
        targetYaw = Math.round(targetYaw / STEP_DEG) * STEP_DEG + STEP_DEG * steps;
    }

    /** 2D 탑뷰로 즉시 고정한다(한 층짜리 레벨). 연출 없이 바로 반듯한 탑뷰다. */
    public void snapToTopView() {
        expandElapsed = -1;
        yaw = targetYaw = TOP_YAW;
        pitch = targetPitch = TOP_PITCH;
    }

    /** 기본 3D 시점으로 즉시 맞춘다(4단계 이후 진입). */
    public void snapToDefaultView() {
        expandElapsed = -1;
        yaw = targetYaw = 0;
        pitch = targetPitch = DEFAULT_PITCH;
    }

    /**
     * 탑뷰에서 시작해 기본 3D 시점(yaw 0°, pitch 30°)으로 부드럽게 확장한다.
     * 첫 3D 레벨에 들어갈 때의 연출이다: 화면은 탑뷰로 시작해 {@link #EXPAND_MS} 동안
     * 천천히 기울고 돌아간다(가속·감속이 있는 smoothstep 이징).
     */
    public void expandFromTopView() {
        yaw = TOP_YAW;
        pitch = TOP_PITCH;
        targetYaw = 0;
        targetPitch = DEFAULT_PITCH;
        expandElapsed = 0;
    }

    /** 진행 중인 확장 연출을 즉시 끝내고 기본 3D 시점으로 맞춘다. */
    public void finishExpansion() {
        if (expandElapsed < 0) return;
        expandElapsed = -1;
        yaw = targetYaw = 0;
        pitch = targetPitch = DEFAULT_PITCH;
    }

    /** 확장 연출이 진행 중인가. */
    public boolean isExpanding() {
        return expandElapsed >= 0;
    }

    public void resetView() {
        finishExpansion();
        targetYaw = Math.round(targetYaw / 360.0) * 360.0; // 가장 가까운 기본 방향으로 짧게 돌아간다
        targetPitch = DEFAULT_PITCH;
    }

    /** 목표 yaw·pitch로 부드럽게 수렴. 확장 연출 중에는 연출 곡선을 따른다. */
    public void update(long dtMillis) {
        if (expandElapsed >= 0) {
            expandElapsed += dtMillis;
            double u = Math.min(1.0, (double) expandElapsed / EXPAND_MS);
            double e = u * u * (3 - 2 * u); // smoothstep: 천천히 시작해 천천히 끝난다
            yaw = TOP_YAW - TOP_YAW * e;
            pitch = TOP_PITCH + (DEFAULT_PITCH - TOP_PITCH) * e;
            if (u >= 1.0) {
                yaw = targetYaw = 0;
                pitch = targetPitch = DEFAULT_PITCH;
                expandElapsed = -1;
            }
            return;
        }
        yaw = approach(yaw, targetYaw, dtMillis, YAW_SMOOTH_MS);
        pitch = approach(pitch, targetPitch, dtMillis, PITCH_SMOOTH_MS);
    }

    private static double approach(double v, double target, long dtMillis, double tauMillis) {
        double diff = target - v;
        if (Math.abs(diff) < SNAP_DEG) return target;
        double next = v + diff * (1 - Math.exp(-dtMillis / tauMillis));
        return Math.abs(target - next) < SNAP_DEG ? target : next; // 마지막 한 프레임에서 정확히 목표값으로 스냅
    }

    public boolean isRotating() {
        return Math.abs(targetYaw - yaw) >= SNAP_DEG;
    }

    public boolean isTilting() {
        return Math.abs(targetPitch - pitch) >= SNAP_DEG;
    }

    /** 회전이나 기울기 연출이 아직 진행 중인가. */
    public boolean isAnimating() {
        return expandElapsed >= 0 || isRotating() || isTilting();
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
     *   <li>45° 반 걸음 시점과 탑뷰(yaw -45°): 격자 축이 화면 축과 나란해지므로 W/A/S/D가
     *       화면 위/왼쪽/아래/오른쪽 방향으로 그대로 움직인다. 탑뷰에서는 -45°가 0° 네 배수로
     *       양자화되어 매핑이 항등이 된다(격자 +x = 화면 오른쪽, +y = 화면 아래).</li>
     * </ul>
     * 기울기(pitch)는 어느 방향을 고르는지에 영향을 주지 않는다(회전은 격자 위에서 일어난다).
     */
    public static Direction mapKey(Direction key, double yawDeg) {
        if (key == Direction.ABOVE || key == Direction.BELOW) return key;
        return PLANAR[Math.floorMod(indexOf(key) - quarterTurns(yawDeg), 4)];
    }

    /**
     * yaw를 90° 단위 회전 수(0~3)로 양자화한다. 45° 경계는 다음 90° 방향으로 넘긴다.
     * 경계에 아주 가까운 값(예: -45.0000001)이 반대편으로 튀지 않도록 작은 여유를 둔다.
     */
    static int quarterTurns(double yawDeg) {
        return Math.floorMod((int) Math.floor(yawDeg / 90.0 + 0.5 + 1e-9), 4);
    }

    private static int indexOf(Direction d) {
        for (int i = 0; i < PLANAR.length; i++) {
            if (PLANAR[i] == d) return i;
        }
        return 0; // 위/아래(z)는 mapKey에서 먼저 걸러진다
    }
}
