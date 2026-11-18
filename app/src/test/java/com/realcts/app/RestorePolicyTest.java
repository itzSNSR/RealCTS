package com.realcts.app;

import org.junit.Test;
import static org.junit.Assert.*;

public class RestorePolicyTest {
    @Test public void restoreOwnedTemporaryValue() {
        assertTrue(RestorePolicy.owns("google/vis", "google/vis", "real/entry", "real/entry"));
    }
    @Test public void preserveExternalVoiceServiceChange() {
        assertFalse(RestorePolicy.owns("other/vis", "google/vis", "real/entry", "real/entry"));
    }
    @Test public void preserveUserAssistantChange() {
        assertFalse(RestorePolicy.owns("google/vis", "google/vis", "google/assistant", "real/entry"));
    }
    @Test public void preserveRemovalAndDistinguishEmptyFromAbsent() {
        assertFalse(RestorePolicy.owns(null, "google/vis", null, null));
        assertFalse(RestorePolicy.owns("google/vis", "google/vis", "", null));
        assertTrue(RestorePolicy.owns("google/vis", "google/vis", null, null));
    }
    @Test public void missingJournalNeverOwnsSetting() {
        assertFalse(RestorePolicy.owns(null, null, "real/entry", "real/entry"));
    }
}
