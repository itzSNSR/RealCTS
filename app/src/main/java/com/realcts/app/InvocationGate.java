package com.realcts.app;

/** Shared by successive short-lived service instances in this process. */
final class InvocationGate {
    private boolean active;
    private long endedAt = -1;

    synchronized boolean acquire(long now) {
        if (active || endedAt >= 0 && now - endedAt < 400) return false;
        active = true;
        return true;
    }
    synchronized void release(long now) {
        active = false;
        endedAt = now;
    }
}
