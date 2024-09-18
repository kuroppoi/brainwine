package brainwine.gameserver.entity.npc.job;

import brainwine.gameserver.Fake;
import brainwine.gameserver.dialog.Dialog;
import brainwine.gameserver.dialog.DialogHelper;
import brainwine.gameserver.dialog.DialogSection;
import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.server.messages.EntityChangeMessage;
import brainwine.gameserver.util.MapHelper;

public abstract class DialoguerJob extends Job {
    protected String choice;

    /**Return the DialogSection that contains the content specific to this job. 
     * 
     * @param me the job haver
     * @param other the entity interacting with the job haver
     * @return a dialog section to be added after the greeting
     */
    public abstract DialogSection getMainDialogSection(Npc me, Player player);
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
    public abstract boolean handleDialogAnswers(Npc me, Player player, Object[] ans);

    /**This instance skips the main dialog section since only configuration is wanted.
     * This is only useful if the dialoguing player is admin.
     */
    public static DialoguerJob CONFIGURATION_ONLY = new DialoguerJob() {
        @Override
        public DialogSection getMainDialogSection(Npc me, Player player) {
            return null;
        }

        @Override
        public boolean handleDialogAnswers(Npc me, Player player, Object[] ans) {
            return false;
        }
    };

    public boolean dialogue(Npc me, Player player) {
        DialogSection title = new DialogSection().setTitle(String.format("%s says:", me.getName()));
        DialogSection salutation = new DialogSection().setText(Fake.get(Fake.Type.SALUTATION));
        DialogSection mainDialog = getMainDialogSection(me, player);

        Dialog dialog = new Dialog()
            .addSection(title)
            .addSection(salutation);

        if (mainDialog != null) {
            dialog = dialog.addSection(mainDialog);
        }
        
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
                handleConfiguration(me, player, ans);
                this.handleDialogAnswers(me, player, ans);
            }
        );

        return true;
    }
    
    private void handleConfiguration(Npc me, Player player, Object[] ans) {
        if (ans.length >= 1 && "configure".equals(ans[0])) {
            if(!player.isAdmin()) return;

            Dialog dialog = DialogHelper.getDialog("android.configure");

            player.showDialog(dialog, resp -> {
                if("cancel".equals(resp[0])) return;

                String name = (String)resp[0];
                String job = (String)resp[1];

                boolean validated = true;

                if("null".equals(job)) {
                    job = null;
                } else if(!job.matches("^\\s*$")) {
                    if(!Job.validateJob(job)) {
                        player.showDialog(DialogHelper.messageDialog(String.format("\"%s\" is not a valid job type.", job)));
                        validated = false;
                    }
                }

                if(validated) {
                    me.setName(name);

                    if(job == null || !job.matches("^\\s*$")) {
                        me.setJob(job);
                    }

                    player.notify(String.format("Cool, I'm now %s the %s!", me.getName(), me.getJob()));
                    me.getZone().sendMessage(new EntityChangeMessage(me.getId(), MapHelper.mapOf("n", me.getName())));
                }
            });
        }
    }

}
