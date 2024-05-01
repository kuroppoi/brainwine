package brainwine.gameserver.zone.gen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

import brainwine.gameserver.prefab.Prefab;
import brainwine.gameserver.util.Vector2i;
import brainwine.gameserver.util.WeightedMap;
import brainwine.gameserver.zone.gen.caves.CaveDecorator;
import brainwine.gameserver.zone.gen.caves.CaveType;
import brainwine.gameserver.zone.gen.models.Deposit;
import brainwine.gameserver.zone.gen.models.LayerSeparator;
import brainwine.gameserver.zone.gen.models.OreDeposit;
import brainwine.gameserver.zone.gen.models.SpecialStructure;
import brainwine.gameserver.zone.gen.models.StoneType;
import brainwine.gameserver.zone.gen.models.TerrainType;
import brainwine.gameserver.zone.gen.surface.SurfaceDecorator;
import brainwine.gameserver.zone.gen.surface.SurfaceRegionType;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GeneratorConfig {
    
    private TerrainType terrainType = TerrainType.NORMAL;
    private double minAmplitude = 15;
    private double maxAmplitude = 45;
    private int surfaceRegionSize = 50;
    private Vector2i dungeonRegion = new Vector2i(80, 64);
    private double dungeonChance = 0.25;
    private double backgroundAccentChance = 0.033;
    private double backgroundDrawingChance = 0.001;
    private LayerSeparator layerSeparator;
    private WeightedMap<StoneType> stoneTypes = new WeightedMap<>();
    private WeightedMap<Prefab> spawnBuildings = new WeightedMap<>();
    private WeightedMap<Prefab> dungeons = new WeightedMap<>();
    private SpecialStructure[] specialStructures = {};
    private Deposit[] deposits = {};
    private OreDeposit[] oreDeposits = {};
    private List<SurfaceDecorator> globalSurfaceDecorators = new ArrayList<>();
    private List<CaveDecorator> globalCaveDecorators = new ArrayList<>();
    private WeightedMap<SurfaceRegionType> surfaceRegionTypes = new WeightedMap<>();
    private List<CaveType> caveTypes = new ArrayList<>();
    
    @JsonCreator
    protected GeneratorConfig() {}
    
    public TerrainType getTerrainType() {
        return terrainType;
    }
    
    public double getMinAmplitude() {
        return minAmplitude;
    }
    
    public double getMaxAmplitude() {
        return maxAmplitude;
    }
    
    public int getSurfaceRegionSize() {
        return surfaceRegionSize;
    }
    
    @JsonSetter(value = "dungeon_region", nulls = Nulls.SKIP)
    private void setDungeonRegion(Vector2i dungeonRegion) {
        if(dungeonRegion.getX() > 0 && dungeonRegion.getY() > 0) {
            this.dungeonRegion = dungeonRegion;
        }
    }
    
    public Vector2i getDungeonRegion() {
        return dungeonRegion;
    }
    
    public double getDungeonChance() {
        return dungeonChance;
    }
    
    public double getBackgroundAccentChance() {
        return backgroundAccentChance;
    }
    
    public double getBackgroundDrawingChance() {
        return backgroundDrawingChance;
    }
    
    public LayerSeparator getLayerSeparator() {
        return layerSeparator;
    }
    
    @JsonSetter(value = "stone_types", nulls = Nulls.SKIP)
    public WeightedMap<StoneType> getStoneTypes() {
        return stoneTypes;
    }
    
    @JsonSetter(value = "spawn_buildings", nulls = Nulls.SKIP)
    public WeightedMap<Prefab> getSpawnBuildings() {
        return spawnBuildings;
    }
    
    @JsonSetter(value = "dungeons", nulls = Nulls.SKIP)
    public WeightedMap<Prefab> getDungeons() {
        return dungeons;
    }
    
    @JsonSetter(value = "special_structures", nulls = Nulls.SKIP, contentNulls = Nulls.SKIP)
    public SpecialStructure[] getSpecialStructures() {
        return specialStructures;
    }
    
    @JsonSetter(value = "deposits", nulls = Nulls.SKIP, contentNulls = Nulls.SKIP)
    public Deposit[] getDeposits() {
        return deposits;
    }
    
    @JsonSetter(value = "ore_deposits", nulls = Nulls.SKIP, contentNulls = Nulls.SKIP)
    public OreDeposit[] getOreDeposits() {
        return oreDeposits;
    }
    
    @JsonSetter(value = "global_surface_decorators", nulls = Nulls.SKIP, contentNulls = Nulls.SKIP)
    public List<SurfaceDecorator> getGlobalSurfaceDecorators() {
        return globalSurfaceDecorators;
    }
    
    @JsonSetter(value = "global_cave_decorators", nulls = Nulls.SKIP, contentNulls = Nulls.SKIP)
    public List<CaveDecorator> getGlobalCaveDecorators() {
        return globalCaveDecorators;
    }
    
    @JsonSetter(value = "surface_region_types", nulls = Nulls.SKIP, contentNulls = Nulls.SKIP)
    private void setSurfaceRegionTypes(Map<String, SurfaceRegionType> surfaceRegionTypes) {
        this.surfaceRegionTypes = new WeightedMap<>(surfaceRegionTypes.values(), SurfaceRegionType::getFrequency);
    }
    
    public WeightedMap<SurfaceRegionType> getSurfaceRegionTypes() {
        return surfaceRegionTypes;
    }
    
    @JsonSetter(value = "cave_types", nulls = Nulls.SKIP, contentNulls = Nulls.SKIP)
    private void setCaveTypes(Map<String, CaveType> caveTypes) {
        this.caveTypes = new ArrayList<>(caveTypes.values());
    }
    
    public List<CaveType> getCaveTypes() {
        return caveTypes;
    }
}
