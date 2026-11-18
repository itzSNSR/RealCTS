package com.realcts.app;

import org.junit.Test;
import static org.junit.Assert.*;

public class InvocationGateTest {
    @Test public void inFlightRequestRejectsDuplicatesRegardlessOfElapsedTime() {
        InvocationGate gate = new InvocationGate();
        assertTrue(gate.acquire(5));
        assertFalse(gate.acquire(6000));
    }
    @Test public void rapidRepeatSuppressedThenLaterRequestAccepted() {
        InvocationGate gate = new InvocationGate();
        assertTrue(gate.acquire(10));
        gate.release(90);
        assertFalse(gate.acquire(489));
        assertTrue(gate.acquire(490));
    }
}
