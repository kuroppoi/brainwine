package brainwine.gameserver.entity.npc.job;

import brainwine.gameserver.Fake;
import brainwine.gameserver.dialog.Dialog;
import brainwine.gameserver.dialog.DialogHelper;
import brainwine.gameserver.dialog.DialogSection;
import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.server.messages.EntityChangeMessage;

public abstract class DialoguerJob extends Job {
    protected String choice;

    /**Return the DialogSection that contains the content specific to this job. 
     * 
     * @param me the job haver
     * @param other the entity interacting with the job haver
     * @return a dialog section to be added after the greeting
     */
    public abstract DialogSection getMainDialogSection(Npc me, Entity other);
    /**Handle the responses to the initial dialog.
     * 
     * The implementation should first check if ans[0] reflects the choice that relates to the job,
     * e. g. "joke" in case of Joker,
     * Afterwards it can take world-changing action or send the interactor more dialogs
     * 
     * @param me the job haver
     * @param other the entity interacting with the job haver
     * @param ans the array of answers filled into the dialog. If no inputs have been added this will be of length 1 and contain "cancel" etc.
     * @return
     */
    public abstract boolean handleDialogAnswers(Npc me, Entity other, Object[] ans);

    public boolean dialogue(Npc me, Entity other) {
        if (!other.isPlayer()) {
            return false;
        }

        Player player = (Player)other;
        DialogSection title = new DialogSection().setTitle(String.format("%s says:", me.getName()));
        DialogSection salutation = new DialogSection().setText(Fake.get(Fake.Type.SALUTATION));
        DialogSection mainDialog = getMainDialogSection(me, other);

        Dialog dialog = new Dialog()
            .addSection(title)
            .addSection(salutation)
            .addSection(mainDialog);
        
        if (player.isAdmin()) {
            dialog = dialog.addSection(
                new DialogSection()
                    .setText("Can I configure you?")
                    .setChoice("configure")
            );
        }

        if (!player.isV3()) {
            dialog = dialog.addSection(new DialogSection().setText(" "));
        }
        
        player.showDialog(dialog, ans -> {
                if (ans.length >= 1 && "cancel".equals(ans[0])) return;
                handleConfiguration(me, other, ans);
                this.handleDialogAnswers(me, other, ans);
            }
        );

        return true;
    }
    
    private void handleConfiguration(Npc me, Entity other, Object[] ans) {
        System.out.println((String)ans[0]);

        if (ans.length >= 1 && "configure".equals(ans[0])) {
            if(!other.isPlayer()) return;

            Player player = (Player)other;

            if(!player.isAdmin()) return;

            Dialog dialog = DialogHelper.getDialog("android.configure");

            player.showDialog(dialog, resp -> {
                if ("cancel".equals(resp[0])) return;

                String name = (String)resp[0];
                String job = (String)resp[1];

                me.setName(name);
                me.setJob(job);
                player.notify(String.format("Cool, I'm now %s the %s!", name, job));

                me.sendMessageToTrackers(new EntityChangeMessage(me.getId(), me.getStatusConfig()));
            });
        }
    }

}
