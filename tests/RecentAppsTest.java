package dev.aurora.tv;

public final class RecentAppsTest {
    private static void expect(String want, String actual) {
        if (!want.equals(actual)) throw new AssertionError("Expected " + want + ", got " + actual);
    }
    public static void main(String[] args) {
        expect("tv.app", RecentApps.record("", "tv.app"));
        expect("tv.a,tv.b,tv.c", RecentApps.record("tv.b,tv.a,tv.c", "tv.a"));
        expect("tv.a,tv.b", RecentApps.record("tv.a,,tv.b,tv.b", "tv.a"));
        String history = "";
        for (int i = 0; i < 20; i++) history = RecentApps.record(history, "tv.app" + i);
        expect("tv.app19,tv.app18,tv.app17,tv.app16,tv.app15,tv.app14,tv.app13,tv.app12,tv.app11,tv.app10,tv.app9,tv.app8", history);
        System.out.println("RecentApps: 4 checks passed");
    }
}
