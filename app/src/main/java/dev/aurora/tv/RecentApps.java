package dev.aurora.tv;

import java.util.LinkedHashSet;
import java.util.Set;

/** Most recently launched package first, no duplicates, at most 12 entries. */
final class RecentApps {
    static String record(String previous, String packageName) {
        Set<String> ordered = new LinkedHashSet<>();
        ordered.add(packageName);
        for (String item : previous.split(",")) {
            if (!item.isEmpty() && ordered.size() < 12) ordered.add(item);
        }
        StringBuilder result = new StringBuilder();
        for (String item : ordered) {
            if (result.length() > 0) result.append(',');
            result.append(item);
        }
        return result.toString();
    }
}
