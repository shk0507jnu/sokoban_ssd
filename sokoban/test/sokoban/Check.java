package sokoban;

/** 최소 테스트 도구. */
final class Check {
    private static int passed, failed;

    private Check() {
    }

    interface Body {
        void run() throws Exception;
    }

    static void test(String name, Body b) {
        try {
            b.run();
            passed++;
            System.out.println("  ok   " + name);
        } catch (Throwable t) {
            failed++;
            System.out.println("  FAIL " + name + " -> " + t);
            StackTraceElement[] st = t.getStackTrace();
            for (int i = 0; i < Math.min(4, st.length); i++) System.out.println("         at " + st[i]);
        }
    }

    static void finish() {
        System.out.printf("%n%d passed, %d failed%n", passed, failed);
        if (failed > 0) System.exit(1);
    }

    static void check(boolean c) {
        if (!c) throw new AssertionError("조건 불만족");
    }

    static void check(boolean c, String msg) {
        if (!c) throw new AssertionError(msg);
    }

    static void eq(Object expected, Object actual) {
        eq(expected, actual, "");
    }

    static void eq(Object expected, Object actual, String msg) {
        if (!java.util.Objects.equals(expected, actual)) {
            throw new AssertionError(msg + " 기대=" + expected + " 실제=" + actual);
        }
    }

    static void eq(int expected, int actual) {
        if (expected != actual) throw new AssertionError("기대=" + expected + " 실제=" + actual);
    }

    static void eq(int expected, int actual, String msg) {
        if (expected != actual) throw new AssertionError(msg + " 기대=" + expected + " 실제=" + actual);
    }

    static void isNull(Object o) {
        if (o != null) throw new AssertionError("null이어야 한다: " + o);
    }

    static void notNull(Object o) {
        if (o == null) throw new AssertionError("null이 아니어야 한다");
    }

    static void notNull(Object o, String msg) {
        if (o == null) throw new AssertionError(msg);
    }

    static void near(double expected, double actual) {
        if (Math.abs(expected - actual) > 1e-9) throw new AssertionError("기대=" + expected + " 실제=" + actual);
    }
}
