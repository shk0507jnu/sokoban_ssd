package sokoban.ui;

import sokoban.core.Direction;

import java.util.EnumSet;
import java.util.Set;

/** 카메라 시점(45° 단위 8방향)과 화면 기준 입력 매핑 테스트. AllTests에서 호출한다. */
public final class CameraTest {
    private static final Direction[] KEYS = {Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT};

    private static void check(boolean c, String msg) {
        if (!c) throw new AssertionError(msg);
    }

    public static void defaultViewIsIdentity() {
        for (Direction k : KEYS) {
            check(Camera.mapKey(k, 0) == k, "기본 시점에서 " + k + " 는 그대로여야 한다");
        }
        check(Camera.mapKey(Direction.UP, 360) == Direction.UP, "360° 는 기본 시점과 같다");
    }

    /** 45° 단위 시점(8방향) 전부에서 네 키가 서로 다른 격자 방향을 가리킨다. */
    public static void mappingIsBijectionForEveryEighthTurn() {
        for (int step = -8; step <= 16; step++) {
            double yaw = step * Camera.STEP_DEG;
            Set<Direction> seen = EnumSet.noneOf(Direction.class);
            for (Direction k : KEYS) seen.add(Camera.mapKey(k, yaw));
            check(seen.size() == 4, "yaw " + yaw + " 에서 키가 겹친다: " + seen);
        }
    }

    /** 회전 애니메이션 중 임의의 각도(1° 간격)에서도 네 키가 겹치지 않는다. */
    public static void mappingIsBijectionAtEveryAngle() {
        for (int deg = -720; deg <= 720; deg++) {
            Set<Direction> seen = EnumSet.noneOf(Direction.class);
            for (Direction k : KEYS) seen.add(Camera.mapKey(k, deg));
            check(seen.size() == 4, "yaw " + deg + " 에서 키가 겹친다: " + seen);
        }
    }

    public static void quarterTurnRotatesMapping() {
        // 90° 돌리면 화면 오른쪽 위 키(UP)가 가리키는 격자 방향이 LEFT가 된다
        check(Camera.mapKey(Direction.UP, 90) == Direction.LEFT, "yaw 90: UP→LEFT");
        check(Camera.mapKey(Direction.UP, 180) == Direction.DOWN, "yaw 180: UP→DOWN");
        check(Camera.mapKey(Direction.UP, 270) == Direction.RIGHT, "yaw 270: UP→RIGHT");
        check(Camera.mapKey(Direction.UP, 360) == Direction.UP, "yaw 360: UP→UP");
        // 같은 키 순서가 화면에서는 항상 시계 방향이다: 키가 +1칸 돌 때 격자 방향도 같은 방향으로 돈다
        for (int q = 0; q < 8; q++) {
            int[] idx = new int[4];
            for (int i = 0; i < 4; i++) {
                Direction d = Camera.mapKey(KEYS[i], q * Camera.STEP_DEG);
                for (int j = 0; j < 4; j++) if (KEYS[j] == d) idx[i] = j;
            }
            for (int i = 0; i < 4; i++) {
                int step = (idx[(i + 1) % 4] - idx[i] + 4) % 4;
                check(step == 1 || step == 3,
                        "인접 키는 인접 방향이어야 한다 (yaw " + q * Camera.STEP_DEG + ")");
            }
        }
    }

    /** 45° 반 걸음 시점에서는 격자 축이 화면 축과 나란해져 키가 화면 방향 그대로 움직인다. */
    public static void eighthTurnMappingFollowsScreen() {
        // yaw 45°: 격자 LEFT가 화면 위, UP이 오른쪽, RIGHT가 아래, DOWN이 왼쪽에 보인다
        check(Camera.mapKey(Direction.UP, 45) == Direction.LEFT, "yaw 45: W → 화면 위(격자 LEFT)");
        check(Camera.mapKey(Direction.RIGHT, 45) == Direction.UP, "yaw 45: D → 화면 오른쪽(격자 UP)");
        check(Camera.mapKey(Direction.DOWN, 45) == Direction.RIGHT, "yaw 45: S → 화면 아래(격자 RIGHT)");
        check(Camera.mapKey(Direction.LEFT, 45) == Direction.DOWN, "yaw 45: A → 화면 왼쪽(격자 DOWN)");
        // yaw 135°: 반대쪽에서 본 같은 규칙
        check(Camera.mapKey(Direction.UP, 135) == Direction.DOWN, "yaw 135: W → 화면 위(격자 DOWN)");
        check(Camera.mapKey(Direction.RIGHT, 135) == Direction.LEFT, "yaw 135: D → 화면 오른쪽(격자 LEFT)");
        check(Camera.mapKey(Direction.DOWN, 135) == Direction.UP, "yaw 135: S → 화면 아래(격자 UP)");
        check(Camera.mapKey(Direction.LEFT, 135) == Direction.RIGHT, "yaw 135: A → 화면 왼쪽(격자 RIGHT)");
        // 225°·315° 는 각각 90° 회전한 짝이다
        check(Camera.mapKey(Direction.UP, 225) == Direction.RIGHT, "yaw 225: W → 격자 RIGHT");
        check(Camera.mapKey(Direction.RIGHT, 225) == Direction.DOWN, "yaw 225: D → 격자 DOWN");
        check(Camera.mapKey(Direction.DOWN, 225) == Direction.LEFT, "yaw 225: S → 격자 LEFT");
        check(Camera.mapKey(Direction.LEFT, 225) == Direction.UP, "yaw 225: A → 격자 UP");
        check(Camera.mapKey(Direction.UP, 315) == Direction.UP, "yaw 315: W → 격자 UP");
        check(Camera.mapKey(Direction.RIGHT, 315) == Direction.RIGHT, "yaw 315: D → 격자 RIGHT");
    }

    /** 어떤 45° 시점에서도 키가 가리키는 화면 방향과 움직이는 화면 방향이 90° 안쪽으로 맞는다. */
    public static void eighthTurnMappingStaysWithinOneQuadrant() {
        for (int step = 0; step < 8; step++) {
            double yaw = step * Camera.STEP_DEG;
            for (Direction k : KEYS) {
                Direction d = Camera.mapKey(k, yaw);
                double diff = Math.abs(screenAngleDiff(screenAngle(d, yaw), keyAngle(k)));
                check(diff < 90, "yaw " + yaw + " 에서 " + k + "→" + d + " 의 화면 방향 차 " + diff + "°");
            }
        }
    }

    /** 격자 방향이 화면에서 가리키는 각도(도, 오른쪽=0, 아래=90). {@link IsoRenderer} 투영과 같다. */
    private static double screenAngle(Direction d, double yawDeg) {
        double psi = Math.toRadians(45 + yawDeg);
        double sinP = Math.sin(Math.toRadians(Camera.DEFAULT_PITCH));
        double sx = d.dx * Math.cos(psi) - d.dy * Math.sin(psi);
        double sy = (d.dx * Math.sin(psi) + d.dy * Math.cos(psi)) * sinP;
        return Math.toDegrees(Math.atan2(sy, sx));
    }

    /** 키가 가리키는 화면 방향(오른쪽 위 / 오른쪽 아래 / 왼쪽 아래 / 왼쪽 위). */
    private static double keyAngle(Direction k) {
        double sinP = Math.sin(Math.toRadians(Camera.DEFAULT_PITCH));
        return switch (k) {
            case UP -> Math.toDegrees(Math.atan2(-sinP, 1));
            case RIGHT -> Math.toDegrees(Math.atan2(sinP, 1));
            case DOWN -> Math.toDegrees(Math.atan2(sinP, -1));
            default -> Math.toDegrees(Math.atan2(-sinP, -1));
        };
    }

    private static double screenAngleDiff(double a, double b) {
        return (a - b + 540) % 360 - 180;
    }

    public static void topViewProjectsSquareTiles() {
        // 탑뷰(yaw -45°, pitch 90°): 격자 +x = 화면 오른쪽, +y = 화면 아래, z는 화면에서 사라진다
        double[] ex = IsoRenderer.project(1, 0, 0, Camera.TOP_YAW, Camera.TOP_PITCH);
        double[] ey = IsoRenderer.project(0, 1, 0, Camera.TOP_YAW, Camera.TOP_PITCH);
        double[] ez = IsoRenderer.project(0, 0, 1, Camera.TOP_YAW, Camera.TOP_PITCH);
        near(ex[0], 1);
        near(ex[1], 0);
        near(ey[0], 0);
        near(ey[1], 1);
        near(ez[0], 0);
        near(ez[1], 0);
        // 두 격자 축이 화면에서 직교하고 길이가 같다 = 반듯한 정사각 타일
        double dot = ex[0] * ey[0] + ex[1] * ey[1];
        double lenX = Math.hypot(ex[0], ex[1]), lenY = Math.hypot(ey[0], ey[1]);
        near(dot, 0);
        near(lenX, lenY);
        // 대각선도 45°가 아니라 정사각형의 대각선(1:1)이다
        double[] diag = IsoRenderer.project(1, 1, 0, Camera.TOP_YAW, Camera.TOP_PITCH);
        near(diag[0], 1);
        near(diag[1], 1);
        // 기본 3D 시점은 반대로 마름모 타일이다: +x 와 +y 가 둘 다 비스듬히 간다
        double[] isoX = IsoRenderer.project(1, 0, 0, 0, Camera.DEFAULT_PITCH);
        double[] isoY = IsoRenderer.project(0, 1, 0, 0, Camera.DEFAULT_PITCH);
        check(Math.abs(isoX[1]) > 0.1 && Math.abs(isoY[1]) > 0.1, "기본 시점은 마름모 타일");
    }

    /** 탑뷰에서는 W/A/S/D가 화면 위/왼쪽/아래/오른쪽 그대로 움직인다. */
    public static void topViewKeyMappingFollowsScreen() {
        for (Direction k : KEYS) {
            check(Camera.mapKey(k, Camera.TOP_YAW) == k, "탑뷰에서 " + k + " 는 그대로여야 한다");
            Direction d = Camera.mapKey(k, Camera.TOP_YAW);
            double[] v = IsoRenderer.project(d.dx, d.dy, 0, Camera.TOP_YAW, Camera.TOP_PITCH);
            double moved = Math.toDegrees(Math.atan2(v[1], v[0]));
            check(Math.abs(screenAngleDiff(moved, keyAngleOnScreen(k))) < 0.001,
                    "탑뷰에서 " + k + " 는 화면 " + keyAngleOnScreen(k) + "° 쪽으로 가야 한다 (실제 " + moved + "°)");
        }
    }

    /** 탑뷰·기본 시점은 고정값으로 만들어지며 연출 없이 즉시 적용된다. */
    public static void topViewPoseConstants() {
        eq(90.0, Camera.TOP_PITCH);
        eq(-45.0, Camera.TOP_YAW);
        eq(30.0, Camera.DEFAULT_PITCH);
        check(Camera.mapKey(Direction.UP, 360 + Camera.TOP_YAW) == Direction.UP, "탑뷰 각도는 360° 주기");
        Camera c = new Camera();
        c.snapToTopView();
        eq(Camera.TOP_YAW, c.yaw);
        eq(Camera.TOP_YAW, c.targetYaw);
        eq(Camera.TOP_PITCH, c.pitch);
        eq(Camera.TOP_PITCH, c.targetPitch);
        check(!c.isAnimating(), "탑뷰 고정은 연출 없이 즉시");
        c.snapToDefaultView();
        eq(0.0, c.yaw);
        eq(Camera.DEFAULT_PITCH, c.pitch);
        check(!c.isAnimating(), "기본 3D 시점도 즉시");
    }

    /** 첫 3D 레벨 진입 연출: 탑뷰에서 3D 시점으로 부드럽게 기울며 돌아가고 결국 정확히 수렴한다. */
    public static void expansionFromTopViewAnimatesAndConverges() {
        Camera c = new Camera();
        c.expandFromTopView();
        eq(Camera.TOP_YAW, c.yaw);
        eq(Camera.TOP_PITCH, c.pitch);
        eq(0.0, c.targetYaw);
        eq(Camera.DEFAULT_PITCH, c.targetPitch);
        check(c.isExpanding() && c.isAnimating(), "연출 시작");
        check(Camera.EXPAND_MS >= 500, "연출은 눈에 보일 만큼 길어야 한다: " + Camera.EXPAND_MS + "ms");

        // 초반은 천천히: 0.24초쯤에도 아직 탑뷰에 가깝다
        for (int i = 0; i < 15; i++) c.update(16); // 약 0.24초
        check(c.pitch < Camera.TOP_PITCH && c.pitch > Camera.DEFAULT_PITCH + 5, "중간 기울기: " + c.pitch);
        check(c.yaw > Camera.TOP_YAW && c.yaw < 0, "중간 yaw: " + c.yaw);

        // 기울기는 단조 감소한다(도중에 되돌아가지 않는다)
        double prev = c.pitch;
        int frames = 15;
        while (c.isExpanding() && frames < 1000) {
            c.update(16);
            frames++;
            check(c.pitch <= prev + 1e-9, "기울기는 단조 감소");
            prev = c.pitch;
        }
        check(frames > 30, "연출은 한 번에 끝나지 않는다: " + frames + "프레임");
        check(frames <= Camera.EXPAND_MS / 16 + 2, "연출은 EXPAND_MS 안에 끝난다: " + frames + "프레임");
        eq(Camera.DEFAULT_PITCH, c.pitch);
        eq(0.0, c.yaw);
        check(!c.isAnimating(), "수렴 후 정지");

        // 정확히 절반 지점에서는 정확히 절반만큼(이징 대칭점)
        Camera d = new Camera();
        d.expandFromTopView();
        d.update(Camera.EXPAND_MS / 2);
        eq((Camera.TOP_PITCH + Camera.DEFAULT_PITCH) / 2, d.pitch);
        eq(Camera.TOP_YAW / 2, d.yaw);
        check(d.isExpanding(), "절반 지점은 아직 진행 중");
        d.update(Camera.EXPAND_MS / 2);
        check(!d.isExpanding(), "EXPAND_MS 뒤에는 끝난다");
        eq(Camera.DEFAULT_PITCH, d.pitch);

        // 연출 중 시점 조작을 하면 연출을 끊고 바로 3D에서 돈다
        Camera e = new Camera();
        e.expandFromTopView();
        e.update(100);
        e.rotateStep(1);
        check(!e.isExpanding(), "시점 조작은 연출을 끊는다");
        eq(Camera.DEFAULT_PITCH, e.pitch);
        eq(45.0, e.targetYaw);
    }

    /** 시점 초기화(V)는 회전과 기울기를 모두 기본 3D 시점으로 되돌린다. */
    public static void resetViewRestoresDefaultTilt() {
        Camera c = new Camera();
        c.snapToTopView();
        c.resetView();
        for (int i = 0; i < 300; i++) c.update(16);
        eq(Camera.DEFAULT_PITCH, c.pitch);
        eq(0.0, c.yaw);
        check(!c.isAnimating(), "초기화 후 정지");
        // 3D에서 돌린 뒤에도 기울기는 그대로 30°
        Camera d = new Camera();
        d.rotateStep(2);
        for (int i = 0; i < 200; i++) d.update(16);
        eq(Camera.DEFAULT_PITCH, d.pitch);
        eq(90.0, d.yaw);
    }

    /** 키가 가리키는 화면 방향(탑뷰). 화면 오른쪽=0°, 아래=+90°. */
    private static double keyAngleOnScreen(Direction k) {
        return switch (k) {
            case UP -> -90;
            case RIGHT -> 0;
            case DOWN -> 90;
            default -> 180;
        };
    }

    private static void near(double a, double b) {
        if (Math.abs(a - b) > 1e-9) throw new AssertionError("값이 다르다: " + a + " vs " + b);
    }

    private static void eq(double a, double b) {
        if (a != b) throw new AssertionError("값이 다르다: " + a + " vs " + b);
    }

    public static void verticalKeysIgnoreCamera() {
        for (double yaw : new double[]{0, 45, 135, 225, 315, -45}) {
            check(Camera.mapKey(Direction.ABOVE, yaw) == Direction.ABOVE, "위 키 yaw " + yaw);
            check(Camera.mapKey(Direction.BELOW, yaw) == Direction.BELOW, "아래 키 yaw " + yaw);
        }
    }

    public static void cameraStepsByEighthTurnAndAnimates() {
        Camera c = new Camera();
        check(Camera.STEP_DEG == 45, "시점 한 칸은 45°");
        check(!c.isRotating() && c.yaw == 0, "처음에는 기본 시점");
        c.rotateStep(1);
        check(c.isRotating(), "회전 시작");
        check(c.targetYaw == 45, "목표 45°: " + c.targetYaw);
        check(c.mapKey(Direction.UP) == Direction.LEFT, "목표 시점 기준으로 입력이 해석된다");
        for (int i = 0; i < 200; i++) c.update(16);
        check(!c.isRotating() && c.yaw == 45, "수렴 후 정지: " + c.yaw);
        c.rotateStep(1);
        for (int i = 0; i < 200; i++) c.update(16);
        check(c.yaw == 90, "두 칸이면 90°: " + c.yaw);
        check(c.mapKey(Direction.UP) == Direction.LEFT, "90° 시점 매핑");
        c.rotateStep(-2);
        for (int i = 0; i < 400; i++) c.update(16);
        check(c.yaw == 0, "되돌아옴: " + c.yaw);
        check(c.mapKey(Direction.UP) == Direction.UP, "기본 시점 매핑 복귀");
        c.rotateStep(8);
        for (int i = 0; i < 600; i++) c.update(16);
        check(c.yaw == 360, "8칸 = 한 바퀴: " + c.yaw);
        check(c.mapKey(Direction.UP) == Direction.UP, "한 바퀴 돌면 매핑도 제자리");
        c.resetView();
        for (int i = 0; i < 200; i++) c.update(16);
        check(c.yaw == 360, "시점 초기화는 가장 가까운 기본 방향: " + c.yaw);
        check(c.pitch == Camera.DEFAULT_PITCH, "초기화 후 기울기");
    }

    /** 한글 입력기 상태에서도 단축키가 영문 키로 인식된다. */
    public static void koreanImeKeysMapToPhysicalKeys() {
        int U = java.awt.event.KeyEvent.VK_UNDEFINED, P = 0xE5;
        check(KeyNormalizer.keyCodeOf(U, 'ㅛ', 0) == java.awt.event.KeyEvent.VK_Y, "ㅛ→Y");
        check(KeyNormalizer.keyCodeOf(P, 'ㅍ', 0) == java.awt.event.KeyEvent.VK_V, "ㅍ→V");
        check(KeyNormalizer.keyCodeOf(U, 'ㄱ', 0) == java.awt.event.KeyEvent.VK_R, "ㄱ→R");
        check(KeyNormalizer.keyCodeOf(U, 'ㅋ', 0) == java.awt.event.KeyEvent.VK_Z, "ㅋ→Z");
        // 이동키 WASD (ㅈ ㅁ ㄴ ㅇ)
        check(KeyNormalizer.keyCodeOf(U, 'ㅈ', 0) == java.awt.event.KeyEvent.VK_W, "ㅈ→W");
        check(KeyNormalizer.keyCodeOf(P, 'ㅁ', 0) == java.awt.event.KeyEvent.VK_A, "ㅁ→A");
        check(KeyNormalizer.keyCodeOf(U, 'ㄴ', 0) == java.awt.event.KeyEvent.VK_S, "ㄴ→S");
        check(KeyNormalizer.keyCodeOf(U, 'ㅇ', 0) == java.awt.event.KeyEvent.VK_D, "ㅇ→D");
        check(KeyNormalizer.keyCodeOf(P, '￿', 0x01000000 | 'ㅇ') == java.awt.event.KeyEvent.VK_D, "확장 코드 ㅇ→D");
        // 정상 코드는 그대로
        check(KeyNormalizer.keyCodeOf(java.awt.event.KeyEvent.VK_Y, 'y', 0) == java.awt.event.KeyEvent.VK_Y, "정상 키 유지");
        check(KeyNormalizer.keyCodeOf(java.awt.event.KeyEvent.VK_LEFT, '￿', 0) == java.awt.event.KeyEvent.VK_LEFT, "방향키 유지");
    }
}
