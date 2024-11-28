package brainwine.gameserver.item.interactions;

import brainwine.gameserver.dialog.Dialog;
import brainwine.gameserver.dialog.DialogHelper;
import brainwine.gameserver.dialog.DialogSection;
import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.util.MapHelper;
import brainwine.gameserver.zone.MetaBlock;
import brainwine.gameserver.zone.Zone;

import java.util.HashMap;
import java.util.Map;

public class LandmarkInteraction implements ItemInteraction {
    private static final int VOTING_INTERVAL = 1000;
    @Override
    public void interact(Zone zone, Entity entity, int x, int y, Layer layer, Item item, int mod, MetaBlock metaBlock, Object config, Object[] data) {
        if(!entity.isPlayer()) return;

        // Do nothing if data is invalid
        if(data != null) {
            return;
        }

        Player player = (Player)entity;

        if(player.getLevel() < 10) {
            player.notify("Sorry, you must be level 10 or higher to vote on landmarks.");
            return;
        }

        long now = System.currentTimeMillis();
        if(player.getLastLandmarkVoteAt() + VOTING_INTERVAL > now) {
            player.notify("You must wait a bit before voting again.");
            return;
        }

        if(metaBlock.getOwner() == player) {
            player.notify("Sorry, you cannot upvote your own landmark.");
            return;
        }

        Map<String, Object> v = MapHelper.getMap(metaBlock.getMetadata(), "v");
        if(v != null && v.containsKey(player.getDocumentId())) {
            player.notify("You have already upvoted this landmark.");
            return;
        }

        String name = metaBlock.getStringProperty("n");
        Dialog dialog = new Dialog()
                .setTitle("Landmark Upvote")
                .setActions("Cancel", "Yes")
                .addSection(new DialogSection().setTitle("Upvote " + name + "?"));

        player.showDialog(dialog, ans -> {
            if(ans.length == 0) return;
            if("Yes".equals(ans[0])) {
                int current = metaBlock.getIntProperty("vc"); // will return 0 if null
                metaBlock.setProperty("vc", current + 1);

                Map<String, Object> currentVotes = MapHelper.getMap(metaBlock.getMetadata(), "v", new HashMap<>());
                currentVotes.put(player.getDocumentId(), now);
                metaBlock.getMetadata().put("v", currentVotes);
                zone.updateBlockMod(metaBlock.getX(), metaBlock.getY(), Layer.FRONT, 1);
                zone.sendBlockMetaUpdate(metaBlock);

                player.setLastLandmarkVoteAt(now);
                player.addExperience(10);
                player.getStatistics().trackLandmarksUpvoted();

                Player owner = metaBlock.getOwner();
                if(owner != null) {
                    owner.getStatistics().trackLandmarkVotesReceived();
                }

                player.showDialog(DialogHelper.messageDialog("Vote Received", "Thanks for your upvote!"));
            }
        });
    }
}
