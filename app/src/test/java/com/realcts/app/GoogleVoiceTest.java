package com.realcts.app;

import org.junit.Test;
import static org.junit.Assert.*;

public class GoogleVoiceTest {
    @Test public void activeGoogleDoesNotNeedAVisibleActivity() {
        assertTrue(Capabilities.supportsVoiceRequest(36, true, false, ""));
    }
    @Test public void missingEvidenceDoesNotEnableSwitching() {
        assertFalse(Capabilities.supportsVoiceRequest(36, false, false, ""));
    }
    @Test public void differentConfiguredProviderStopsRequest() {
        assertFalse(Capabilities.supportsVoiceRequest(36, true, true, "example.provider"));
    }
}
