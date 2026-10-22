package dev.aurora.tv;

/** Bounds repeated HOME redirects without timers or background polling. */
final class RedirectGuard {
    private long last = -1, windowStart = -1;
    private int count;
    enum Decision { OPEN, IGNORE, PAUSE }
    Decision decide(long now, boolean enabled, boolean amazonHome) {
        if (!enabled || !amazonHome) return Decision.IGNORE;
        if (last >= 0 && now - last < 2000) return Decision.IGNORE;
        if (windowStart < 0 || now - windowStart >= 15000) { windowStart = now; count = 0; }
        if (count >= 3) return Decision.PAUSE;
        last = now; count++;
        return Decision.OPEN;
    }
}
