package brainwine.gameserver.quest;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HardcodedQuest {
    @JsonProperty("quest")
    private String questId;

    public String getQuestId() {
        return questId;
    }
}
