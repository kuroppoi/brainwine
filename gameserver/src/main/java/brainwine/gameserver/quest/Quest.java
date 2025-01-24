package brainwine.gameserver.quest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import brainwine.gameserver.GameConfiguration;
import brainwine.gameserver.util.MapHelper;

@JsonIgnoreProperties("zones")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Quest {
    private String id;

    @JsonProperty("group")
    private String group;

    @JsonProperty("title")
    private String title;

    @JsonProperty("reward")
    private QuestReward reward;

    @JsonProperty("story")
    private QuestStory story;

    @JsonProperty("desc")
    private String description;

    @JsonProperty(value = "desc_mobile", required = false)
    private String descriptionMobile = null;

    @JsonProperty(value = "actions", required = false)
    private Map<QuestAction.Type, List<QuestAction>> actions = new HashMap<>();

    @JsonProperty("tasks")
    private List<QuestTask> tasks;

    @JsonIgnore
    private Map<String, Object> pcDetails = null;
    
    @JsonIgnore
    private Map<String, Object> mobileDetails = null;

    public static Quest get(String questId) {
        Quest quest = MapHelper.get(GameConfiguration.getBaseConfig(), questId, Quest.class);

        if(quest == null) return null;

        quest.setId(questId);
        return quest;
    }

    private void computeClientDetailsIfAbsent() {
        if(pcDetails == null || mobileDetails == null) {
            pcDetails = new HashMap<>();
            mobileDetails = pcDetails;

            pcDetails.put("id", getId());
            pcDetails.put("group", getGroup());
            pcDetails.put("title", getTitle());
            pcDetails.put("xp", getReward().getXp());
            pcDetails.put("crowns", getReward().getCrowns());
            pcDetails.put("desc", getDescription());

            List<String> tasks = new ArrayList<>();

            if(getTasks() != null) for(QuestTask task : getTasks()) {
                tasks.add(task.getDescription());
            }

            pcDetails.put("tasks", tasks);

            if(getDescriptionMobile() != null) {
                mobileDetails = new HashMap<>(pcDetails);

                mobileDetails.put("desc", getDescriptionMobile());
            }
        }
    }

    public void clearDetailsCache() {
        pcDetails = null;
        mobileDetails = null;
    }

    @JsonIgnore
    public Map<String, Object> getPcDetails() {
        computeClientDetailsIfAbsent();
        return pcDetails;
    }

    @JsonIgnore
    public Map<String, Object> getMobileDetails() {
        computeClientDetailsIfAbsent();
        return mobileDetails;
    }

    public String getId() {
        return id;
    }

    public String getGroup() {
        return group;
    }

    public String getTitle() {
        return title;
    }

    public QuestReward getReward() {
        return reward;
    }

    public QuestStory getStory() {
        return story;
    }

    public String getDescription() {
        return description;
    }

    public String getDescriptionMobile() {
        return descriptionMobile;
    }

    public Map<QuestAction.Type, List<QuestAction>> getActions() {
        return actions;
    }

    public List<QuestTask> getTasks() {
        return tasks;
    }

    public Quest setId(String id) {
        this.id = id;
        return this;
    }

    public Quest setGroup(String group) {
        this.group = group;
        return this;
    }

    public Quest setTitle(String title) {
        this.title = title;
        return this;
    }

    public Quest setReward(QuestReward reward) {
        this.reward = reward;
        return this;
    }

    public Quest setStory(QuestStory story) {
        this.story = story;
        return this;
    }

    public Quest setDescription(String description) {
        this.description = description;
        return this;
    }

    public Quest setDescriptionMobile(String descriptionMobile) {
        this.descriptionMobile = descriptionMobile;
        return this;
    }

    public Quest setActions(Map<QuestAction.Type, List<QuestAction>> actions) {
        this.actions = actions;
        return this;
    }

    public Quest setTasks(List<QuestTask> tasks) {
        this.tasks = tasks;
        return this;
    }
    
}
