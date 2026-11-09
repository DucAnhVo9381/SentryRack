// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack.model;

import com.google.firebase.database.DataSnapshot;
import java.util.Objects;

public class RackStatus {
    public static final long STALE_AFTER_MS = 60_000;

    public enum Condition { HEALTHY, WARNING, CRITICAL }

    public final String rackId;
    public final String name;
    public final long timestamp;
    public final boolean simulated;
    public final boolean hasSensorData;
    public final double coolantLevel;
    public final boolean leak;
    public final double currentA;
    public final double powerW;
    public final boolean doorOpen;
    public final int aqi;
    public final int tvoc;
    public final int eco2;

    public RackStatus(String rackId, String name, long timestamp, boolean simulated,
                      boolean hasSensorData,
                      double coolantLevel,
                      boolean leak, double currentA, double powerW, boolean doorOpen,
                      int aqi, int tvoc, int eco2) {
        this.rackId = rackId;
        this.name = name;
        this.timestamp = timestamp;
        this.simulated = simulated;
        this.hasSensorData = hasSensorData;
        this.coolantLevel = coolantLevel;
        this.leak = leak;
        this.currentA = currentA;
        this.powerW = powerW;
        this.doorOpen = doorOpen;
        this.aqi = aqi;
        this.tvoc = tvoc;
        this.eco2 = eco2;
    }

    public static RackStatus placeholder(String rackId, String name) {
        return new RackStatus(rackId, name, 0, false, false,
                0, false, 0, 0, false, 0, 0, 0);
    }

    public static RackStatus fromSnapshot(String rackId, String name, DataSnapshot s) {
        return new RackStatus(
                rackId,
                name,
                num(s.child("timestamp").getValue()).longValue(),
                Boolean.TRUE.equals(s.child("simulated").getValue(Boolean.class)),
                s.child("coolantLevel").exists() || s.child("current").exists()
                        || s.child("door").exists() || s.child("airQuality").exists(),
                num(s.child("coolantLevel").child("value").getValue()).doubleValue(),
                Boolean.TRUE.equals(s.child("coolantLevel").child("leak").getValue(Boolean.class)),
                num(s.child("current").child("value").getValue()).doubleValue(),
                num(s.child("current").child("powerW").getValue()).doubleValue(),
                Boolean.TRUE.equals(s.child("door").child("open").getValue(Boolean.class)),
                num(s.child("airQuality").child("aqi").getValue()).intValue(),
                num(s.child("airQuality").child("tvoc").getValue()).intValue(),
                num(s.child("airQuality").child("eco2").getValue()).intValue());
    }

    private static Number num(Object val) {
        if (val instanceof Number) {
            return (Number) val;
        }
        return 0;
    }

    public boolean isStale(long maxAgeMs) {
        return timestamp == 0 || System.currentTimeMillis() - timestamp > maxAgeMs;
    }

    public Condition getCondition(long staleMs) {
        if (leak) {
            return Condition.CRITICAL;
        }
        return isStale(staleMs) ? Condition.WARNING : Condition.HEALTHY;
    }

    // Health score out of 100 minus 50 for a leak and minus 10 for each AQI level
    public int getHealthScore(long staleMs) {
        if (isStale(staleMs)) {
            return 0;
        }
        return Math.max(0, 100 - (leak ? 50 : 0) - (aqi * 10));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RackStatus)) return false;
        RackStatus r = (RackStatus) o;
        return timestamp == r.timestamp && simulated == r.simulated
                && hasSensorData == r.hasSensorData
                && Double.compare(coolantLevel, r.coolantLevel) == 0
                && leak == r.leak && Double.compare(currentA, r.currentA) == 0
                && Double.compare(powerW, r.powerW) == 0 && doorOpen == r.doorOpen
                && aqi == r.aqi && tvoc == r.tvoc && eco2 == r.eco2
                && rackId.equals(r.rackId) && Objects.equals(name, r.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rackId, name, timestamp, simulated, hasSensorData, coolantLevel, leak, currentA,
                powerW, doorOpen, aqi, tvoc, eco2);
    }
}