package brainwine.gameserver.entity.player;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import brainwine.gameserver.achievements.CraftingAchievement;
import brainwine.gameserver.achievements.DiscoveryAchievement;
import brainwine.gameserver.achievements.ExploringAchievement;
import brainwine.gameserver.achievements.HuntingAchievement;
import brainwine.gameserver.achievements.LooterAchievement;
import brainwine.gameserver.achievements.MiningAchievement;
import brainwine.gameserver.achievements.RaiderAchievement;
import brainwine.gameserver.achievements.ScavengingAchievement;
import brainwine.gameserver.achievements.SidekickAchievement;
import brainwine.gameserver.achievements.SpawnerStoppageAchievement;
import brainwine.gameserver.entity.EntityConfig;
import brainwine.gameserver.item.Item;

@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlayerStatistics {
    
    private Map<Item, Integer> itemsMined = new HashMap<>();
    private Map<Item, Integer> itemsCrafted = new HashMap<>();
    private Map<Item, Integer> discoveries = new HashMap<>();
    private Map<EntityConfig, Integer> kills = new HashMap<>();
    private Map<EntityConfig, Integer> assists = new HashMap<>();
    private float playTime;
    private int itemsPlaced;
    private int areasExplored;
    private int containersLooted;
    private int dungeonsRaided;
    private int mawsPlugged;
    private int deaths;
    
    @JsonIgnore
    private Player player;
        
    @JsonCreator
    protected PlayerStatistics() {}
    
    protected PlayerStatistics(Player player) {
        this.player = player;
    }
    
    protected void setPlayer(Player player) {
        this.player = player;
    }
    
    public void trackItemMined(Item item) {
        if(!itemsMined.containsKey(item)) {
            player.addExperience(150, "New item mined!");
        }
        
        itemsMined.put(item, getItemsMined(item) + 1);
        player.addExperience(item.getExperienceYield());
        player.updateAchievementProgress(MiningAchievement.class);
        player.updateAchievementProgress(ScavengingAchievement.class);
    }
    
    public void setItemsMined(Map<Item, Integer> itemsMined) {
        this.itemsMined = itemsMined;
    }
    
    public int getTotalItemsMined() {
        return itemsMined.values().stream()
                .reduce(Integer::sum)
                .orElse(0);
    }
    
    public int getUniqueItemsMined() {
        return (int)itemsMined.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .count();
    }
    
    public int getItemsMined(Item item) {
        return itemsMined.getOrDefault(item, 0);
    }
    
    public Map<Item, Integer> getItemsMined() {
        return Collections.unmodifiableMap(itemsMined);
    }
    
    public void trackItemCrafted(Item item) {
        trackItemCrafted(item, 1);
    }
    
    public void trackItemCrafted(Item item, int quantity) {
        if(!itemsCrafted.containsKey(item)) {
            player.addExperience(150, "New item crafted!");
        }
        
        itemsCrafted.put(item, getItemsCrafted(item) + quantity);
        player.updateAchievementProgress(CraftingAchievement.class);
    }
    
    public void setItemsCrafted(Map<Item, Integer> itemsCrafted) {
        this.itemsCrafted = itemsCrafted;
    }
    
    public int getTotalItemsCrafted() {
        return itemsCrafted.values().stream()
                .reduce(Integer::sum)
                .orElse(0);
    }
    
    public int getUniqueItemsCrafted() {
        return (int)itemsCrafted.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .count();
    }
    
    public int getTotalItemsWorkshopped() {
        return itemsCrafted.entrySet().stream()
                .filter(entry -> entry.getKey().requiresWorkshop())
                .map(Entry::getValue)
                .reduce(Integer::sum)
                .orElse(0);
    }
    
    public int getUniqueItemsWorkshopped() {
        return (int)itemsCrafted.entrySet().stream()
                .filter(entry -> entry.getValue() > 0 && entry.getKey().requiresWorkshop())
                .count();
    }
    
    public int getItemsCrafted(Item item) {
        return itemsCrafted.getOrDefault(item, 0);
    }
    
    public Map<Item, Integer> getItemsCrafted() {
        return Collections.unmodifiableMap(itemsCrafted);
    }
    
    public void trackDiscovery(Item item) {
        discoveries.put(item, getDiscoveries(item) + 1);
        player.addExperience(100);
        player.updateAchievementProgress(DiscoveryAchievement.class);
    }
        
    public void setDiscoveries(Map<Item, Integer> discoveries) {
        this.discoveries = discoveries;
    }
    
    public int getTotalDiscoveries() {
        return discoveries.values().stream()
                .reduce(Integer::sum)
                .orElse(0);
    }
    
    public int getUniqueDiscoveries() {
        return (int)discoveries.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .count();
    }
    
    public int getDiscoveries(Item item) {
        return discoveries.getOrDefault(item, 0);
    }
    
    public Map<Item, Integer> getDiscoveries() {
        return Collections.unmodifiableMap(discoveries);
    }
    
    public void trackKill(EntityConfig entity) {
        if(!kills.containsKey(entity)) {
            player.addExperience(150, "First kill!");
        }
        
        kills.put(entity, getKills(entity) + 1);
        player.addExperience(entity.getExperienceYield());
        player.updateAchievementProgress(HuntingAchievement.class);
    }
    
    public void setKills(Map<EntityConfig, Integer> kills) {
        this.kills = kills;
    }
    
    public int getTotalKills() {
        return kills.values().stream()
                .reduce(Integer::sum)
                .orElse(0);
    }
    
    public int getKills(EntityConfig entity) {
        return kills.getOrDefault(entity, 0);
    }
    
    public Map<EntityConfig, Integer> getKills() {
        return Collections.unmodifiableMap(kills);
    }
    
    public void trackAssist(EntityConfig entity) {
        assists.put(entity, getAssists(entity) + 1);
        player.updateAchievementProgress(SidekickAchievement.class);
    }
    
    public void setAssists(Map<EntityConfig, Integer> assists) {
        this.assists = assists;
    }
    
    public int getTotalAssists() {
        return assists.values().stream()
                .reduce(Integer::sum)
                .orElse(0);
    }
    
    public int getAssists(EntityConfig entity) {
        return assists.getOrDefault(entity, 0);
    }
    
    public Map<EntityConfig, Integer> getAssists() {
        return Collections.unmodifiableMap(assists);
    }
    
    public void trackPlayTime(float deltaTime) {
        playTime += deltaTime;
    }
    
    public void setPlayTime(float playTime) {
        this.playTime = playTime;
    }
    
    public float getPlayTime() {
        return playTime;
    }
    
    public void trackItemPlaced() {
        itemsPlaced++;
    }
    
    public void setItemsPlaced(int itemsPlaced) {
        this.itemsPlaced = itemsPlaced;
    }
    
    public int getItemsPlaced() {
        return itemsPlaced;
    }
    
    public void trackAreaExplored() {
        areasExplored++;
        player.addExperience(5, "New area explored!");
        player.updateAchievementProgress(ExploringAchievement.class);
    }
    
    public void setAreasExplored(int areasExplored) {
        this.areasExplored = areasExplored;
    }
    
    public int getAreasExplored() {
        return areasExplored;
    }
    
    public void trackContainerLooted() {
        trackContainerLooted(null);
    }
    
    public void trackContainerLooted(Item container) {
        containersLooted++;
        player.updateAchievementProgress(LooterAchievement.class);
        
        if(container != null) {
            player.addExperience(container.getLootExperienceYield());
        }
    }
    
    public void setContainersLooted(int containersLooted) {
        this.containersLooted = containersLooted;
    }
    
    public int getContainersLooted() {
        return containersLooted;
    }
    
    public void trackDungeonRaided() {
        dungeonsRaided++;
        player.addExperience(100);
        player.updateAchievementProgress(RaiderAchievement.class);
    }
    
    public void setDungeonsRaided(int dungeonsRaided) {
        this.dungeonsRaided = dungeonsRaided;
    }
    
    public int getDungeonsRaided() {
        return dungeonsRaided;
    }
    
    public void trackMawPlugged() {
        mawsPlugged++;
        player.addExperience(5);
        player.updateAchievementProgress(SpawnerStoppageAchievement.class);
    }
    
    public void setMawsPlugged(int mawsPlugged) {
        this.mawsPlugged = mawsPlugged;
    }
    
    public int getMawsPlugged() {
        return mawsPlugged;
    }
    
    public void trackDeath() {
        deaths++;
    }
    
    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }
    
    public int getDeaths() {
        return deaths;
    }
}
