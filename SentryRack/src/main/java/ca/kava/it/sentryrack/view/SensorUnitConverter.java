// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack.view;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Converts and formats sensor readings for Settings > Sensor units and Decimal places.
 * Plain Java (no Android code), so it can be unit tested on the computer.
 *
 * Every reading is stored in Firebase in its BASE unit (mm, A, W, ppb, ppm).
 * This class only changes how the value is SHOWN; the data in Firebase never changes.
 *
 * @author Anvesh Rachakonda
 * @date 2026-11-16
 */
public final class SensorUnitConverter {

    public static final int MIN_DECIMALS = 0;
    public static final int MAX_DECIMALS = 3;
    public static final int DEFAULT_DECIMALS = 1;   // same as the Racks list before (%.1f)

    /** Sensors that have a unit (AQI is a 1-5 index and door is open/closed, so no unit). */
    public enum Sensor { COOLANT, CURRENT, POWER, TVOC, ECO2 }

    /** Each unit knows its sensor, its symbol and how to get there from the base unit. */
    public enum Unit {
        MILLIMETRE(Sensor.COOLANT, "mm", 1.0),          // base
        CENTIMETRE(Sensor.COOLANT, "cm", 0.1),
        INCH(Sensor.COOLANT, "in", 1.0 / 25.4),
        AMPERE(Sensor.CURRENT, "A", 1.0),               // base
        MILLIAMPERE(Sensor.CURRENT, "mA", 1000.0),
        WATT(Sensor.POWER, "W", 1.0),                   // base
        KILOWATT(Sensor.POWER, "kW", 0.001),
        PPB(Sensor.TVOC, "ppb", 1.0),                   // base
        PPM_TVOC(Sensor.TVOC, "ppm", 0.001),
        PPM(Sensor.ECO2, "ppm", 1.0),                   // base
        PERCENT(Sensor.ECO2, "%", 0.0001);

        public final Sensor sensor;
        public final String symbol;
        private final double factorFromBase;

        Unit(Sensor sensor, String symbol, double factorFromBase) {
            this.sensor = sensor;
            this.symbol = symbol;
            this.factorFromBase = factorFromBase;
        }
    }

    private SensorUnitConverter() {
        // Only static helpers
    }

    /** All units a sensor can be shown in (first one = base unit = default). */
    public static List<Unit> unitsFor(Sensor sensor) {
        List<Unit> units = new ArrayList<>();
        for (Unit unit : Unit.values()) {
            if (unit.sensor == sensor) {
                units.add(unit);
            }
        }
        return units;
    }

    /** Default unit = the unit the sensor sends (mm, A, W, ppb, ppm). */
    public static Unit defaultUnit(Sensor sensor) {
        return unitsFor(sensor).get(0);
    }

    /** Reads a saved unit name; anything missing, wrong or for another sensor gives the default. */
    public static Unit unitFromName(Sensor sensor, String name) {
        if (name != null) {
            for (Unit unit : unitsFor(sensor)) {
                if (unit.name().equals(name)) {
                    return unit;
                }
            }
        }
        return defaultUnit(sensor);
    }

    /** Keeps the decimal places between 0 and 3. */
    public static int clampDecimals(int decimals) {
        return Math.max(MIN_DECIMALS, Math.min(MAX_DECIMALS, decimals));
    }

    /** Base-unit value -> value in the chosen unit (e.g. 124.5 mm -> 12.45 cm). */
    public static double convert(double baseValue, Unit unit) {
        return baseValue * unit.factorFromBase;
    }

    /** Rounds half-up to the given decimal places (e.g. 12.345 with 2 -> 12.35). */
    public static double round(double value, int decimals) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return value;
        }
        return BigDecimal.valueOf(value)
                .setScale(clampDecimals(decimals), RoundingMode.HALF_UP)
                .doubleValue();
    }

    /** "12.45 cm" - converted, rounded and with the unit symbol, in the given locale. */
    public static String format(double baseValue, Unit unit, int decimals, Locale locale) {
        int places = clampDecimals(decimals);
        double shown = round(convert(baseValue, unit), places);
        return String.format(locale, "%." + places + "f %s", shown, unit.symbol);
    }

    /** Same as above, in the phone's language (French uses a comma: "12,45 cm"). */
    public static String format(double baseValue, Unit unit, int decimals) {
        return format(baseValue, unit, decimals, Locale.getDefault());
    }
}