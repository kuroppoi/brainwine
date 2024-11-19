package brainwine.gameserver.entity.npc.job;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.entity.npc.job.jobs.*;
import brainwine.gameserver.player.Player;

public enum JobType {
    @JsonEnumDefaultValue
    JOKER(new Joker()),
    CRAFTER(new Crafter()),
    QUESTER(new Quester());

    private Job job;

    private JobType(Job job) {
        this.job = job;
    }

    public static JobType fromString(String j) {
        if (j == null) return null;
        switch(j.toLowerCase()) {
            case "joker":
                return JOKER;
            case "crafter":
                return CRAFTER;
            case "quester":
                return QUESTER;
            default:
                return null;
        }
    }

    public boolean dialogue(Npc me, Player player) {
        return this.job.dialogue(me, player);
    }

    public Job get() {
        return job;
    }

}
