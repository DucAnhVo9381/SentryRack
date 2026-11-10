package ca.kava.it.sentryrack.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RackStatusTest {

    @Test
    public void missingReadingsAreWarning() {
        RackStatus rack = RackStatus.placeholder("rack-1", "Rack 1");

        assertEquals(RackStatus.Condition.WARNING,
                rack.getCondition(RackStatus.STALE_AFTER_MS));
    }

    @Test
    public void leakIsCriticalEvenWhenReadingsAreStale() {
        RackStatus rack = new RackStatus("rack-1", "Rack 1", 0, false, false,
                0, true, 0, 0, false, 0, 0, 0);

        assertEquals(RackStatus.Condition.CRITICAL,
                rack.getCondition(RackStatus.STALE_AFTER_MS));
    }

    @Test
    public void recentReadingsWithoutLeakAreHealthy() {
        RackStatus rack = new RackStatus("rack-1", "Rack 1", System.currentTimeMillis(),
                false, true, 0, false, 0, 0, false, 0, 0, 0);

        assertEquals(RackStatus.Condition.HEALTHY,
                rack.getCondition(RackStatus.STALE_AFTER_MS));
    }
}
