package com.realcts.app;

import org.junit.Test;
import static org.junit.Assert.*;
import static com.realcts.app.TriggerStrategyManager.Mode.*;
import static com.realcts.app.TriggerStrategyManager.Route;
import static com.realcts.app.TriggerStrategyManager.choose;

public class StrategyTest {
    @Test public void googleMissingAlwaysStops() {
        for (TriggerStrategyManager.Mode mode : TriggerStrategyManager.Mode.values())
            assertEquals(Route.Unavailable, choose(mode, false, true, true, true, true, true));
    }
    @Test public void configuredHelperAvoidsSwitch() {
        assertEquals(Route.CSHelper, choose(Automatic, true, true, true, true, false, true));
    }
    @Test public void authenticatedServiceAvoidsSwitch() {
        assertEquals(Route.CSService, choose(Automatic, true, false, true, true, false, true));
    }
    @Test public void activeGoogleNeedsNoGrant() {
        assertEquals(Route.Direct, choose(VIS, true, false, false, true, true, false));
    }
    @Test public void authorizedSwitchOnly() {
        assertEquals(Route.Switch, choose(VIS, true, false, false, true, false, true));
        assertEquals(Route.Unavailable, choose(VIS, true, false, false, true, false, false));
    }
    @Test public void noNativeEvidenceStopsVoiceRoute() {
        assertEquals(Route.Unavailable, choose(Automatic, true, false, false, false, true, true));
    }
    @Test public void manualSelectionNeverFallsThrough() {
        assertEquals(Route.Unavailable, choose(CSHelper, true, false, true, true, true, true));
        assertEquals(Route.Unavailable, choose(CSService, true, true, false, true, true, true));
    }
}
