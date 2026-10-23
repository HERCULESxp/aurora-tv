// SPDX-License-Identifier: GPL-3.0-or-later
package dev.aurora.tv;

/** Epochs cancel callbacks belonging to an overlay that has already closed. */
final class HomeSession {
    private long epoch;
    private boolean active, picked;
    long begin() { active = true; picked = false; return ++epoch; }
    long token() { return epoch; }
    boolean active() { return active; }
    boolean valid(long token) { return active && epoch == token; }
    boolean handoff(long token) { return valid(token) && !picked; }
    void pick() { if (active) picked = true; }
    boolean picked() { return picked; }
    void close() { active = false; picked = false; ++epoch; }
}
