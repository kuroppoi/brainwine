package brainwine.gameserver.minigame;

import brainwine.gameserver.player.Player;
import brainwine.gameserver.server.messages.EventMessage;

/**
 * Model for keeping track of player scores in minigames.
 */
public class Participant {
    
    private final Minigame minigame;
    private final Player player;
    private double score;
    
    public Participant(Minigame minigame, Player player) {
        this.minigame = minigame;
        this.player = player;
    }
    
    public void showInfo(String message) {
        if(isParticipating()) {
            player.sendMessage(new EventMessage("mini", message));
        }
    }
    
    public boolean isParticipating() {
        return player.isOnline() && player.getZone() == minigame.getZone();
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public void incrementScore(double score) {
        this.score += score;
    }
    
    public void deductScore(double score) {
        this.score -= score;
    }
    
    public void setScore(double score) {
        this.score = score;
    }
    
    public double getScore() {
        return score;
    }
}
