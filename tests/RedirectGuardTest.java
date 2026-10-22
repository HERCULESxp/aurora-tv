package dev.aurora.tv;

public class RedirectGuardTest {
    private static void expect(RedirectGuard.Decision expected, RedirectGuard.Decision actual) {
        if (expected != actual) throw new AssertionError("Expected " + expected + ", got " + actual);
    }
    public static void main(String[] args) {
        RedirectGuard g = new RedirectGuard();
        expect(RedirectGuard.Decision.IGNORE, g.decide(0, false, true));
        expect(RedirectGuard.Decision.IGNORE, g.decide(0, true, false));
        expect(RedirectGuard.Decision.OPEN, g.decide(0, true, true));
        expect(RedirectGuard.Decision.IGNORE, g.decide(349, true, true));
        expect(RedirectGuard.Decision.OPEN, g.decide(350, true, true));
        expect(RedirectGuard.Decision.OPEN, g.decide(700, true, true));
        expect(RedirectGuard.Decision.PAUSE, g.decide(1050, true, true));
        expect(RedirectGuard.Decision.IGNORE, g.decide(1400, false, true));
        expect(RedirectGuard.Decision.OPEN, g.decide(15000, true, true));
        System.out.println("RedirectGuard: 9 checks passed");
    }
}
