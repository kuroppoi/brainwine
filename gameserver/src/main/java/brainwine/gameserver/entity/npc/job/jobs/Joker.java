package brainwine.gameserver.entity.npc.job.jobs;

import brainwine.gameserver.Fake;
import brainwine.gameserver.dialog.DialogSection;
import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.entity.npc.job.DialoguerJob;

public class Joker extends DialoguerJob {

    @Override
    public DialogSection getMainDialogSection(Npc me, Entity other) {
        return new DialogSection().setText(Fake.get(Fake.Type.JOKE));
    }

    @Override
    public boolean handleDialogAnswers(Npc me, Entity other, Object[] ans) {
        return true;
    }
    
}
