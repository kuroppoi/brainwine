package brainwine.gameserver.zone;

import brainwine.gameserver.util.RuleRecord;

public class ZoneRules extends RuleRecord {
    @Rule("auto-clean")
    public boolean autoCleanEnabled = true;
    @Rule("do-hostile-entity-spawns")
    public boolean hostileEntitySpawnsEnabled = false;

    public static ZoneRules getPrivateDefaults() {
        ZoneRules rules = new ZoneRules();

        rules.autoCleanEnabled = false;

        return rules;
    }

    public boolean isAutoCleanEnabled() {
        return autoCleanEnabled;
    }

    public boolean isHostileEntitySpawnsEnabled() {
        return hostileEntitySpawnsEnabled;
    }

}
