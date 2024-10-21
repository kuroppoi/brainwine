package brainwine.gameserver.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

import brainwine.gameserver.achievement.Achievement;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.quest.QuestProgress;
import brainwine.gameserver.zone.Zone;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PlayerConfigFile {
    
    private String name;
    private String email;
    private String passwordHash;
    private Zone currentZone;
    private boolean admin;
    private int experience;
    private int skillPoints;
    private int karma;
    private int crowns;
    private Inventory inventory = new Inventory();
    private PlayerStatistics statistics = new PlayerStatistics();
    private List<String> authTokens = new ArrayList<>();
    private List<NameChange> nameChanges = new ArrayList<>();
    private List<PlayerRestriction> mutes = new ArrayList<>();
    private List<PlayerRestriction> bans = new ArrayList<>();
    private Set<String> lootCodes = new HashSet<>();
    private Set<Achievement> achievements = new HashSet<>();
    private Map<String, Float> ignoredHints = new HashMap<>();
    private Map<Skill, Integer> skills = new HashMap<>();
    private Map<Item, List<Skill>> bumpedSkills = new HashMap<>();
    private Map<String, Object> appearance = new HashMap<>();
    private Map<String, QuestProgress> questProgresses = new HashMap<>();
    
    public PlayerConfigFile(Player player) {
        this.name = player.getName();
        this.email = player.getEmail();
        this.passwordHash = player.getPassword();
        this.currentZone = player.getZone();
        this.admin = player.isAdmin();
        this.experience = player.getExperience();
        this.skillPoints = player.getSkillPoints();
        this.karma = player.getKarma();
        this.crowns = player.getCrowns();
        this.inventory = player.getInventory();
        this.statistics = player.getStatistics();
        this.authTokens = player.getAuthTokens();
        this.nameChanges = player.getNameChanges();
        this.mutes = player.getMutes();
        this.bans = player.getBans();
        this.lootCodes = player.getLootCodes();
        this.achievements = player.getAchievements();
        this.ignoredHints = player.getIgnoredHints();
        this.skills = player.getSkills();
        this.bumpedSkills = player.getBumpedSkills();
        this.appearance = player.getAppearance();
        this.questProgresses = player.getQuestProgresses();
    }
    
    @JsonCreator
    private PlayerConfigFile() {}
    
    @JsonSetter(nulls = Nulls.FAIL)
    public String getName() {
        return name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public Zone getCurrentZone() {
        return currentZone;
    }
    
    public boolean isAdmin() {
        return admin;
    }
    
    @JsonSetter(nulls = Nulls.SKIP, contentNulls = Nulls.SKIP)
    public List<String> getAuthTokens() {
        return authTokens;
    }
    
    @JsonSetter(nulls = Nulls.SKIP, contentNulls = Nulls.SKIP)
    public List<NameChange> getNameChanges() {
        return nameChanges;
    }
    
    @JsonSetter(nulls = Nulls.SKIP, contentNulls = Nulls.SKIP)
    public List<PlayerRestriction> getMutes() {
        return mutes;
    }
    
    @JsonSetter(nulls = Nulls.SKIP, contentNulls = Nulls.SKIP)
    public List<PlayerRestriction> getBans() {
        return bans;
    }
    
    public int getExperience() {
        return experience;
    } 
    
    public int getSkillPoints() {
        return skillPoints;
    }
    
    public int getKarma() {
        return karma;
    }
    
    public int getCrowns() {
        return crowns;
    }
    
    @JsonSetter(nulls = Nulls.SKIP)
    public Inventory getInventory() {
        return inventory;
    }
    
    @JsonSetter(nulls = Nulls.SKIP)
    public PlayerStatistics getStatistics() {
        return statistics;
    }
    
    @JsonSetter(nulls = Nulls.SKIP, contentNulls = Nulls.SKIP)
    public Set<String> getLootCodes() {
        return lootCodes;
    }
    
    @JsonSetter(nulls = Nulls.SKIP, contentNulls = Nulls.SKIP)
    public Set<Achievement> getAchievements() {
        return achievements;
    }
    
    @JsonSetter(nulls = Nulls.SKIP, contentNulls = Nulls.SKIP)
    public Map<String, Float> getIgnoredHints() {
        return ignoredHints;
    }
    
    @JsonSetter(nulls = Nulls.SKIP, contentNulls = Nulls.SKIP)
    public Map<Skill, Integer> getSkills() {
        return skills;
    }
    
    @JsonSetter(nulls = Nulls.SKIP, contentNulls = Nulls.SKIP)
    public Map<Item, List<Skill>> getBumpedSkills() {
        return bumpedSkills;
    }
    
    @JsonSetter(nulls = Nulls.SKIP, contentNulls = Nulls.SKIP)
    public Map<String, Object> getAppearance() {
        return appearance;
    }

    @JsonSetter(nulls = Nulls.SKIP, contentNulls = Nulls.SKIP)
    public Map<String, QuestProgress> getQuestProgresses() {
        return questProgresses;
    }
}
