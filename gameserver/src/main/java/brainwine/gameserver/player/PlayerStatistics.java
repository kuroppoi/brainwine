package brainwine.gameserver.player;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import brainwine.gameserver.achievement.CraftingAchievement;
import brainwine.gameserver.achievement.DeliveranceAchievement;
import brainwine.gameserver.achievement.DiscoveryAchievement;
import brainwine.gameserver.achievement.ExploringAchievement;
import brainwine.gameserver.achievement.HuntingAchievement;
import brainwine.gameserver.achievement.LooterAchievement;
import brainwine.gameserver.achievement.MiningAchievement;
import brainwine.gameserver.achievement.RaiderAchievement;
import brainwine.gameserver.achievement.ScavengingAchievement;
import brainwine.gameserver.achievement.SidekickAchievement;
import brainwine.gameserver.achievement.SpawnerStoppageAchievement;
import brainwine.gameserver.achievement.TrappingAchievement;
import brainwine.gameserver.achievement.UndertakerAchievement;
import brainwine.gameserver.entity.EntityConfig;
import brainwine.gameserver.item.Item;

@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlayerStatistics {
    
    private Map<Item, Integer> itemsMined = new HashMap<>();
    private Map<Item, Integer> itemsScavenged = new HashMap<>();
    private Map<Item, Integer> itemsCrafted = new HashMap<>();
    private Map<Item, Integer> discoveries = new HashMap<>();
    private Map<EntityConfig, Integer> kills = new HashMap<>();
    private Map<EntityConfig, Integer> assists = new HashMap<>();
    private Map<EntityConfig, Integer> trappings = new HashMap<>();
    private float playTime;
    private int itemsPlaced;
    private int areasExplored;
    private int containersLooted;
    private int dungeonsRaided;
    private int mawsPlugged;
    private int undertakings;
    private int deliverances;
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
        itemsMined.put(item, getItemsMined(item) + 1);
        player.updateAchievementProgress(MiningAchievement.class);
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
    
    public void trackItemScavenged(Item item) {
        if(!itemsScavenged.containsKey(item)) {
            player.addExperience(150, "New item mined!");
        }
        
        itemsScavenged.put(item, getItemsScavenged(item) + 1);
        player.addExperience(item.getExperienceYield());
        player.updateAchievementProgress(ScavengingAchievement.class);
    }
    
    public void setItemsScavenged(Map<Item, Integer> itemsScavenged) {
        this.itemsScavenged = itemsScavenged;
    }
    
    public int getTotalItemsScavenged() {
        return itemsScavenged.values().stream()
                .reduce(Integer::sum)
                .orElse(0);
    }
    
    public int getUniqueItemsScavenged() {
        return (int)itemsScavenged.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .count();
    }
    
    public int getItemsScavenged(Item item) {
        return itemsScavenged.getOrDefault(item, 0);
    }
    
    public Map<Item, Integer> getItemsScavenged() {
        return Collections.unmodifiableMap(itemsScavenged);
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
    
    public void trackTrapping(EntityConfig entity) {
        trappings.put(entity, getTrappings(entity) + 1);
        player.addExperience(5);
        player.updateAchievementProgress(TrappingAchievement.class);
    }
    
    public void setTrappings(Map<EntityConfig, Integer> trappings) {
        this.trappings = trappings;
    }
    
    public int getTotalTrappings() {
        return trappings.values().stream()
                .reduce(Integer::sum)
                .orElse(0);
    }
    
    public int getTrappings(EntityConfig entity) {
        return trappings.getOrDefault(entity, 0);
    }
    
    public Map<EntityConfig, Integer> getTrappings() {
        return Collections.unmodifiableMap(trappings);
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
    
    public void trackUndertaking() {
        undertakings++;
        player.addExperience(25);
        player.updateAchievementProgress(UndertakerAchievement.class);
    }
    
    public void setUndertakings(int undertakings) {
        this.undertakings = undertakings;
    }
    
    public int getUndertakings() {
        return undertakings;
    }
    
    public void trackDeliverance() {
        trackDeliverances(1);
    }
    
    public void trackDeliverances(int amount) {
        deliverances += amount;
        player.addExperience(25 * amount);
        player.updateAchievementProgress(DeliveranceAchievement.class);
    }
    
    public void setDeliverances(int deliverances) {
        this.deliverances = deliverances;
    }
    
    public int getDeliverances() {
        return deliverances;
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
