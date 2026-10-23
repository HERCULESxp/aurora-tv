package dev.aurora.tv;

public class HomeMechanismTest {
    private static int checks;
    static void check(boolean condition) { checks++; if (!condition) throw new AssertionError("Check " + checks); }
    public static void main(String[] args) {
        String line = "1700000000.500 100 100 I ActivityManager: START u0 {act=android.intent.action.MAIN cat=[android.intent.category.HOME] cmp=com.amazon.tv.launcher/.ui.HomeActivity_vNext} from uid 1000";
        long start = 1700000000000L;
        check(HomeLogParser.homeStartUid(line, start) == 1000);
        check(HomeLogParser.homeStartUid(line.replace("uid 1000", "uid 0"), start) == 0);
        check(HomeLogParser.homeStartUid(line.replace("uid 1000", "uid 12345"), start) == 12345);
        check(HomeLogParser.homeStartUid(line, start + 1000) == -1);
        check(HomeLogParser.homeStartUid(line.replace("START u0", "START u10"), start) == -1);
        check(HomeLogParser.homeStartUid(line.replace("category.HOME", "category.LAUNCHER"), start) == -1);
        check(HomeLogParser.homeStartUid(line.replace("1700000000.500", "invalid"), start) == -1);
        check(HomeLogParser.homeStartUid(line.replace("1700000000.500", "NaN"), start) == -1);
        check(HomeLogParser.homeStartUid(null, start) == -1);
        check(HomeLogParser.homeStartUid(line.replace("uid 1000", "uid invalid"), start) == -1);
        HomeSession session = new HomeSession();
        long first = session.begin(); check(session.handoff(first));
        session.pick(); check(!session.handoff(first)); check(session.valid(first));
        session.close(); check(!session.valid(first));
        long second = session.begin(); check(!session.valid(first)); check(session.handoff(second));
        session.close(); check(!session.handoff(second));
        System.out.println("Home mechanism: " + checks + " checks passed");
    }
}
