package ca.kava.it.sentryrack;

import org.junit.Test;

import ca.kava.it.sentryrack.model.RackStatus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class RackStatusUnitTest {

    @Test
    public void placeholderHasWarningConditionAndZeroHealth() {
        RackStatus rack = RackStatus.placeholder("rack-1", "Rack 1");

        assertEquals(RackStatus.Condition.WARNING,
                rack.getCondition(RackStatus.STALE_AFTER_MS));
        assertEquals(0, rack.getHealthScore(RackStatus.STALE_AFTER_MS));
        assertFalse(rack.hasSensorData);
    }

    @Test
    public void leakIsCriticalEvenWhenReadingsAreStale() {
        RackStatus rack = statusAt(0, true, 0);

        assertEquals(RackStatus.Condition.CRITICAL,
                rack.getCondition(RackStatus.STALE_AFTER_MS));
    }

    @Test
    public void recentReadingsWithoutLeakAreHealthy() {
        RackStatus rack = statusAt(System.currentTimeMillis(), false, 0);

        assertEquals(RackStatus.Condition.HEALTHY,
                rack.getCondition(RackStatus.STALE_AFTER_MS));
        assertEquals(100, rack.getHealthScore(RackStatus.STALE_AFTER_MS));
    }

    @Test
    public void readingsOlderThanThresholdAreStale() {
        RackStatus rack = statusAt(
                System.currentTimeMillis() - RackStatus.STALE_AFTER_MS - 1, false, 0);

        assertTrue(rack.isStale(RackStatus.STALE_AFTER_MS));
        assertEquals(RackStatus.Condition.WARNING,
                rack.getCondition(RackStatus.STALE_AFTER_MS));
        assertEquals(0, rack.getHealthScore(RackStatus.STALE_AFTER_MS));
    }

    @Test
    public void healthScoreAppliesLeakAndAqiPenaltiesAndFloorsAtZero() {
        RackStatus penalized = statusAt(System.currentTimeMillis(), true, 3);
        RackStatus minimum = statusAt(System.currentTimeMillis(), true, 10);

        assertEquals(20, penalized.getHealthScore(RackStatus.STALE_AFTER_MS));
        assertEquals(0, minimum.getHealthScore(RackStatus.STALE_AFTER_MS));
    }

    @Test
    public void negativeAqiDoesNotRaiseHealthAboveOneHundred() {
        RackStatus rack = statusAt(System.currentTimeMillis(), false, -1);

        assertEquals(100, rack.getHealthScore(RackStatus.STALE_AFTER_MS));
    }

    @Test
    public void equalityAndHashCodeReflectRackFields() {
        long timestamp = System.currentTimeMillis();
        RackStatus first = statusAt(timestamp, false, 1);
        RackStatus same = statusAt(timestamp, false, 1);
        RackStatus different = new RackStatus("rack-1", "Rack 2", timestamp,
                false, true, 0, false, 0, 0, false, 1, 0, 0);

        assertEquals(first, same);
        assertEquals(first.hashCode(), same.hashCode());
        assertNotEquals(first, different);
        assertFalse(first.equals(null));
    }

    private RackStatus statusAt(long timestamp, boolean leak, int aqi) {
        return new RackStatus("rack-1", "Rack 1", timestamp,
                false, true, 0, leak, 0, 0, false, aqi, 0, 0);
    }
}
