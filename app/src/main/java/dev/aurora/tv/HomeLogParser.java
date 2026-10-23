// SPDX-License-Identifier: GPL-3.0-or-later
// Adapted from Homestead HomeKeyWatcher, Copyright (C) 2026 Homestead contributors.
package dev.aurora.tv;

/** Reads only the timestamp and initiating UID; never retains the log line. */
final class HomeLogParser {
    static int homeStartUid(String line, long notBefore) {
        if (line == null || !line.contains("START u0 {")
                || !line.contains("cat=[android.intent.category.HOME]")) return -1;
        int u = line.indexOf(" from uid ");
        if (u < 0) return -1;
        int start = u + 10, end = start;
        while (end < line.length() && Character.isDigit(line.charAt(end))) end++;
        if (end == start) return -1;
        try {
            String trimmed = line.trim();
            double stamp = Double.parseDouble(trimmed.substring(0, trimmed.indexOf(' ')));
            if (Double.isNaN(stamp) || Double.isInfinite(stamp) || stamp * 1000 < notBefore) return -1;
            return Integer.parseInt(line.substring(start, end));
        } catch (RuntimeException invalid) { return -1; }
    }
}
