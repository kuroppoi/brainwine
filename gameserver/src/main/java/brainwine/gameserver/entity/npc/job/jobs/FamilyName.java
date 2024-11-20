package brainwine.gameserver.entity.npc.job.jobs;

import brainwine.gameserver.Naming;
import brainwine.gameserver.dialog.DialogSection;
import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.entity.npc.job.DialoguerJob;
import brainwine.gameserver.player.Player;

public class FamilyName extends DialoguerJob {

    @Override
    public DialogSection getMainDialogSection(Npc me, Player player) {
        String familyName = Naming.ENTITY_LAST_NAMES[(int)(Math.random() * Naming.ENTITY_LAST_NAMES.length)];
        return new DialogSection().setText("Ah, another survivor. I can see you're still a little dazed from that whole apocalypse business... " +
                String.format("you've forgotten your Family Name, yes? I can help. I can see it in your eyes. You're a %s! A more than worthy ancestry. ", familyName) +
                "What you do with that Name is up to you. Perhaps you will bring glory to your house. I wish you luck!");
    }

    @Override
    public boolean handleDialogAnswers(Npc me, Player player, Object[] ans) {
        return true;
    }
    
}
