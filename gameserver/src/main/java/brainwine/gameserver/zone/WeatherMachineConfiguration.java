package brainwine.gameserver.zone;

import brainwine.gameserver.util.MathUtils;
import org.apache.commons.text.WordUtils;

import java.util.Map;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

public class WeatherMachineConfiguration extends WorldMachineConfiguration {
    public enum Environment {
        PLEASANT,
        DANGEROUS,
        SEVERE,
        EXTREME,
    }
    public enum DayAndNightCycleMode {
        NORMAL,
        FAST,
        SLOW,
        REALTIME,
        DAY,
        NIGHT,
    }
    public enum Precipitation {
        NORMAL,
        ALWAYS,
        NONE
    }

    private DayAndNightCycleMode dayAndNightCycleMode;
    private int timeZone;
    private Precipitation precipitation;
    private int degreeOfDanger;
    private boolean fastGrowthEnabled;
    private boolean liquidGravityEnabled;

    public WeatherMachineConfiguration() {
        reset();
    }

    @Override
    protected String getDialogName() {
        return "dialogs.world_machines.weather.configure";
    }

    protected void reset() {
        dayAndNightCycleMode = DayAndNightCycleMode.NORMAL;
        timeZone = 0;
        precipitation = Precipitation.NORMAL;
        degreeOfDanger = 3;
        fastGrowthEnabled = false;
        liquidGravityEnabled = true;
    }

    @Override
    protected void configure(Zone zone, Map<String, Object> values) throws IllegalArgumentException {
        reset();

        if(values.get("day_night_cycle") != null) {
            dayAndNightCycleMode = DayAndNightCycleMode.valueOf(expectString(values.get("day_night_cycle")).toUpperCase());
        }

        if(values.get("precipitation_frequency") != null) {
            precipitation = Precipitation.valueOf(expectString(values.get("precipitation_frequency")).toUpperCase());
        }

        if(values.get("environment") != null) {
            degreeOfDanger = 2 * Environment.valueOf(expectString(values.get("environment")).toUpperCase()).ordinal() + 1;
        }

        if(values.get("growth_index") != null) {
            fastGrowthEnabled = expectString(values.get("growth_index")).equalsIgnoreCase("High");
        }

        if(values.get("liquid_gravity") != null) {
            liquidGravityEnabled = expectString(values.get("liquid_gravity")).equalsIgnoreCase("Normal");
        }

        timeZone = MathUtils.clamp(expectInteger(defaultIfNull(values.get("day_night_cycle_time_zone"), timeZone)), -12, 12);
    }

    @Override
    protected Object getValue(String key) {
        switch(key) {
            case "day_night_cycle":
                return WordUtils.capitalizeFully(dayAndNightCycleMode.toString());
            case "day_night_cycle_time_zone":
                return Integer.toString(timeZone);
            case "precipitation_frequency":
                return WordUtils.capitalizeFully(precipitation.toString());
            case "environment":
                return WordUtils.capitalizeFully(Environment.values()[degreeOfDanger / 2].toString());
            case "growth_index":
                return fastGrowthEnabled ? "High" : "Normal";
            case "liquid_gravity":
                return liquidGravityEnabled ? "None" : "Normal";
            default:
                return null;
        }
    }

    public DayAndNightCycleMode getDayAndNightCycleMode() {
        return dayAndNightCycleMode;
    }

    public int getTimeZone() {
        return timeZone;
    }

    public Precipitation getPrecipitation() {
        return precipitation;
    }

    public int getDegreeOfDanger() {
        return degreeOfDanger;
    }

    public boolean isFastGrowthEnabled() {
        return fastGrowthEnabled;
    }

    public boolean isLiquidGravityEnabled() {
        return liquidGravityEnabled;
    }
}
