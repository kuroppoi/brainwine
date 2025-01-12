package brainwine.gameserver.zone;

import brainwine.gameserver.util.RuleRecord;

public class ZoneRules extends RuleRecord {
    @Rule("auto-clean")
    private boolean autoCleanEnabled = true;
    @Rule(value="auto-clean-duration", minValue=500, maxValue=120000)
    private int autoCleanDuration = 60000;
    @Rule("do-hostile-entity-spawns")
    private boolean hostileEntitySpawnsEnabled = false;
    @Rule(value="do-peaceful-entity-spawns", adminOnly=true)
    private boolean peacefulEntitySpawnsEnabled = false;

    public static ZoneRules getPrivateDefaults() {
        ZoneRules rules = new ZoneRules();

        rules.autoCleanEnabled = false;

        return rules;
    }

    public boolean isAutoCleanEnabled() {
        return autoCleanEnabled;
    }

    public int getAutoCleanDuration() {
        return autoCleanDuration;
    }

    public boolean isHostileEntitySpawnsEnabled() {
        return hostileEntitySpawnsEnabled;
    }

    public boolean isPeacefulEntitySpawnsEnabled() {
        return peacefulEntitySpawnsEnabled;
    }
}
