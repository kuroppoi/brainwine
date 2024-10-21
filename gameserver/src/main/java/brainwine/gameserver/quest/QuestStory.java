package brainwine.gameserver.quest;

import com.fasterxml.jackson.annotation.JsonProperty;

import brainwine.gameserver.player.Player;

public class QuestStory {
    private String intro;
    private String accept;
    private String begin;
    @JsonProperty("begin_mobile")
    private String beginMobile;
    private String incomplete;
    private String complete;

    public String getIntro() {
        return intro;
    }
    public String getAccept() {
        return accept;
    }
    public String getBegin() {
        return begin;
    }
    public String getBeginMobile() {
        return beginMobile;
    }
    public String getIncomplete() {
        return incomplete;
    }
    public String getComplete() {
        return complete;
    }
    public String getBegin(Player player) {
        // TODO: check mobile device player properly
        if(getBeginMobile() == null) {
            return getBegin();
        }
        
        if(player.isV3()) {
            return getBegin();
        } else {
            return getBeginMobile();
        }
    }
}
