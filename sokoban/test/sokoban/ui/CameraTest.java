package sokoban.ui;

import sokoban.core.Direction;

import java.util.EnumSet;
import java.util.Set;

/** 카메라 회전과 화면 기준 입력 매핑 테스트. AllTests에서 호출한다. */
public final class CameraTest {
    private static final Direction[] KEYS = {Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT};

    private static void check(boolean c, String msg) {
        if (!c) throw new AssertionError(msg);
    }

    public static void defaultViewIsIdentity() {
        for (Direction k : KEYS) {
            check(Camera.mapKey(k, 0, Camera.DEFAULT_PITCH) == k, "기본 시점에서 " + k + " 는 그대로여야 한다");
        }
    }

    public static void mappingIsBijectionForEveryQuarterTurnAndPitch() {
        for (int quarter = -4; quarter <= 8; quarter++) {
            for (double pitch = Camera.MIN_PITCH; pitch <= Camera.MAX_PITCH; pitch += 10) {
                Set<Direction> seen = EnumSet.noneOf(Direction.class);
                for (Direction k : KEYS) seen.add(Camera.mapKey(k, quarter * 90.0, pitch));
                check(seen.size() == 4, "yaw " + quarter * 90 + " pitch " + pitch + " 에서 키가 겹친다: " + seen);
            }
        }
    }

    public static void quarterTurnRotatesMapping() {
        // 90° 돌리면 화면 오른쪽 위 키(UP)가 가리키는 격자 방향이 LEFT가 된다
        check(Camera.mapKey(Direction.UP, 90, 30) == Direction.LEFT, "yaw 90: UP→LEFT");
        check(Camera.mapKey(Direction.UP, 180, 30) == Direction.DOWN, "yaw 180: UP→DOWN");
        check(Camera.mapKey(Direction.UP, 270, 30) == Direction.RIGHT, "yaw 270: UP→RIGHT");
        check(Camera.mapKey(Direction.UP, 360, 30) == Direction.UP, "yaw 360: UP→UP");
        // 같은 키 순서가 화면에서는 항상 시계 방향이다: 키가 +1칸 돌 때 격자 방향도 같은 방향으로 돈다
        for (int q = 0; q < 4; q++) {
            int[] idx = new int[4];
            for (int i = 0; i < 4; i++) {
                Direction d = Camera.mapKey(KEYS[i], q * 90.0, 30);
                for (int j = 0; j < 4; j++) if (KEYS[j] == d) idx[i] = j;
            }
            for (int i = 0; i < 4; i++) {
                check((idx[(i + 1) % 4] - idx[i] + 4) % 4 == 1 || (idx[(i + 1) % 4] - idx[i] + 4) % 4 == 3,
                        "인접 키는 인접 방향이어야 한다 (yaw " + q * 90 + ")");
            }
        }
    }

    public static void verticalKeysIgnoreCamera() {
        check(Camera.mapKey(Direction.ABOVE, 135, 55) == Direction.ABOVE, "위 키");
        check(Camera.mapKey(Direction.BELOW, 135, 55) == Direction.BELOW, "아래 키");
    }

    public static void cameraAnimatesAndSnaps() {
        Camera c = new Camera();
        c.rotateQuarter(1);
        check(c.isRotating(), "회전 시작");
        check(c.mapKey(Direction.UP) == Direction.LEFT, "목표 시점 기준으로 입력이 해석된다");
        for (int i = 0; i < 200; i++) c.update(16);
        check(!c.isRotating() && c.yaw == 90, "수렴 후 정지: " + c.yaw);
        c.rotateQuarter(-1);
        for (int i = 0; i < 200; i++) c.update(16);
        check(c.yaw == 0, "되돌아옴");
        // 드래그 후 놓으면 가장 가까운 90°로 정렬
        c.beginDrag();
        c.drag(120, 0); // 120*0.45 = 54°
        check(Math.abs(c.yaw - 54) < 1e-9, "드래그 중 자유 회전");
        c.endDrag();
        check(c.targetYaw == 90, "54° → 90° 정렬: " + c.targetYaw);
        for (int i = 0; i < 200; i++) c.update(16);
        check(c.yaw == 90, "정렬 완료");
        // 기울기는 범위 제한
        c.beginDrag();
        c.drag(0, 10000);
        check(c.pitch == Camera.MAX_PITCH, "pitch 상한");
        c.drag(0, -10000);
        check(c.pitch == Camera.MIN_PITCH, "pitch 하한");
        c.endDrag();
        c.resetView();
        check(c.pitch == Camera.DEFAULT_PITCH, "시점 초기화");
    }

    /** 한글 입력기 상태에서도 단축키가 영문 키로 인식된다. */
    public static void koreanImeKeysMapToPhysicalKeys() {
        int U = java.awt.event.KeyEvent.VK_UNDEFINED, P = 0xE5;
        check(KeyNormalizer.keyCodeOf(U, 'ㅛ', 0) == java.awt.event.KeyEvent.VK_Y, "ㅛ→Y");
        check(KeyNormalizer.keyCodeOf(P, 'ㅍ', 0) == java.awt.event.KeyEvent.VK_V, "ㅍ→V");
        check(KeyNormalizer.keyCodeOf(U, 'ㄱ', 0) == java.awt.event.KeyEvent.VK_R, "ㄱ→R");
        check(KeyNormalizer.keyCodeOf(U, 'ㅋ', 0) == java.awt.event.KeyEvent.VK_Z, "ㅋ→Z");
        check(KeyNormalizer.keyCodeOf(P, '￿', 0x01000000 | 'ㅇ') == java.awt.event.KeyEvent.VK_D, "확장 코드 ㅇ→D");
        // 정상 코드는 그대로
        check(KeyNormalizer.keyCodeOf(java.awt.event.KeyEvent.VK_Y, 'y', 0) == java.awt.event.KeyEvent.VK_Y, "정상 키 유지");
        check(KeyNormalizer.keyCodeOf(java.awt.event.KeyEvent.VK_LEFT, '￿', 0) == java.awt.event.KeyEvent.VK_LEFT, "방향키 유지");
    }
}
