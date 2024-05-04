package brainwine.gameserver.zone.gen.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.prefab.Prefab;
import brainwine.gameserver.util.Vector2i;
import brainwine.gameserver.util.WeightedMap;
import brainwine.gameserver.zone.Biome;
import brainwine.gameserver.zone.EcologicalMachine;
import brainwine.gameserver.zone.MetaBlock;
import brainwine.gameserver.zone.gen.GeneratorConfig;
import brainwine.gameserver.zone.gen.GeneratorContext;
import brainwine.gameserver.zone.gen.caves.Cave;
import brainwine.gameserver.zone.gen.caves.CaveDecorator;
import brainwine.gameserver.zone.gen.caves.CaveType;
import brainwine.gameserver.zone.gen.caves.StructureCaveDecorator;
import brainwine.gameserver.zone.gen.models.SpecialStructure;
import brainwine.gameserver.zone.gen.models.TerrainType;
import brainwine.gameserver.zone.gen.surface.StructureSurfaceDecorator;
import brainwine.gameserver.zone.gen.surface.SurfaceDecorator;
import brainwine.gameserver.zone.gen.surface.SurfaceRegion;

public class StructureGeneratorTask implements GeneratorTask {
    
    private final boolean filled;
    private final Vector2i dungeonRegion;
    private final double dungeonChance;
    private final WeightedMap<Prefab> spawnBuildings;
    private final WeightedMap<Prefab> dungeons;
    private final SpecialStructure[] specialStructures;
    private final List<SurfaceDecorator> globalSurfaceDecorators;
    private final List<CaveDecorator> globalCaveDecorators;
    
    public StructureGeneratorTask(GeneratorConfig config) {
        filled = config.getTerrainType() == TerrainType.FILLED;
        dungeonRegion = config.getDungeonRegion();
        dungeonChance = config.getDungeonChance();
        spawnBuildings = config.getSpawnBuildings();
        dungeons = config.getDungeons();
        specialStructures = config.getSpecialStructures();
        globalSurfaceDecorators = config.getGlobalSurfaceDecorators();
        globalCaveDecorators = config.getGlobalCaveDecorators();
    }
    
    @Override
    public void generate(GeneratorContext ctx) {
        int width = ctx.getWidth();
        int height = ctx.getHeight();
        
        // Generate spawn buildings
        if(!spawnBuildings.isEmpty()) {
            placeRandomSpawnBuilding(ctx, (int)(width * (ctx.nextDouble() * 0.05 + 0.175)));
            placeRandomSpawnBuilding(ctx, (int)(width * (ctx.nextDouble() * 0.05 + 0.475)));
            placeRandomSpawnBuilding(ctx, (int)(width * (ctx.nextDouble() * 0.05 + 0.775)));
        }
        
        // Generate special structures
        for(SpecialStructure structure : specialStructures) {
            float amount = width * height / (float)structure.getBlocksPerSpawn();
            int structureCount = (int)amount;
            
            if(ctx.nextDouble() < (amount - structureCount)) {
                structureCount++;
            }
            
            structureCount = Math.max(structure.getMin(), Math.min(structure.getMax(), structureCount));
            
            for(int i = 0; i < structureCount; i++) {
                int x = ctx.nextInt(width);
                int minY = ctx.getSurface(x);
                int y = ctx.nextInt(height - minY) + minY;
                ctx.placePrefab(structure.getPrefab(), x, y);
            }
        }
        
        // Generate dungeons
        if(!dungeons.isEmpty()) {
            for(int x = 0; x < width; x += dungeonRegion.getX()) {
                for(int y = 0; y < height; y += dungeonRegion.getY()) {
                    if(ctx.nextDouble() <= dungeonChance) {
                        Prefab dungeon = dungeons.next(ctx.getRandom());
                        int prefabWidth = dungeon.getWidth();
                        int prefabHeight = dungeon.getHeight();
                        
                        if(ctx.isUnderground(x, y) && ctx.isUnderground(x + prefabWidth, y)) {
                            int placeX = x + (prefabWidth >= dungeonRegion.getX() ? 0 : ctx.nextInt(dungeonRegion.getX() - prefabWidth));
                            int placeY = y + (prefabHeight >= dungeonRegion.getY() ? 0 : ctx.nextInt(dungeonRegion.getY() - prefabHeight));
                            ctx.placePrefab(dungeon, placeX, placeY);
                        }
                    }
                }
            }
        }
        
        // Generate surface structures
        for(SurfaceRegion region : ctx.getSurfaceRegions()) {
            // Global decorators
            for(SurfaceDecorator decorator : globalSurfaceDecorators) {
                if(decorator instanceof StructureSurfaceDecorator && ctx.nextDouble() <= decorator.getChance()) {
                    decorator.decorate(ctx, region);
                }
            }
            
            // Type-exclusive decorators
            for(SurfaceDecorator decorator : region.getType().getDecorators()) {
                if(decorator instanceof StructureSurfaceDecorator && ctx.nextDouble() <= decorator.getChance()) {
                    decorator.decorate(ctx, region);
                }
            }
        }
        
        // Generate cave structures
        for(Cave cave : ctx.getCaves()) {
            // Global decorators
            for(CaveDecorator decorator : globalCaveDecorators) {
                if(decorator instanceof StructureCaveDecorator && ctx.nextDouble() <= decorator.getChance()) {
                    decorator.decorate(ctx, cave);
                }
            }
            
            // Type-exclusive decorators
            CaveType type = cave.getType();
            
            if(type != null) {
                for(CaveDecorator decorator : type.getDecorators()) {
                    if(decorator instanceof StructureCaveDecorator && ctx.nextDouble() <= decorator.getChance()) {
                        decorator.decorate(ctx, cave);
                    }
                }
            }
        }
        
        // Fetch list of replaceable containers
        List<MetaBlock> containers = ctx.getZone().getMetaBlocks(metaBlock
                -> ctx.isUnderground(metaBlock.getX(), metaBlock.getY())
                && (metaBlock.getItem().hasId("containers/chest-mechanical-large") 
                || (metaBlock.getItem().hasId("containers/chest") && !metaBlock.getMetadata().containsKey("@"))));
        Collections.shuffle(containers, ctx.getRandom());
        
        // TODO
        placeComponentChests(ctx, containers);
        placeBrokenTeleporters(ctx, containers);
        
    }
    
    private void placeComponentChests(GeneratorContext ctx, List<MetaBlock> containers) {
        // TODO remove test code
        Biome biome = ctx.getZone().getBiome();
        List<EcologicalMachine> machines = new ArrayList<>();
        
        switch(biome) {
            case PLAIN:
                machines.add(EcologicalMachine.PURIFIER);
                machines.add(ctx.nextDouble() < 0.5 ? EcologicalMachine.RECYCLER : EcologicalMachine.COMPOSTER);
                break;
            case ARCTIC:
                machines.add(ctx.nextDouble() < 0.5 ? EcologicalMachine.RECYCLER : EcologicalMachine.COMPOSTER);
                break;
            case HELL:
                machines.add(EcologicalMachine.EXPIATOR);
                break;
            case DESERT:
                machines.add(EcologicalMachine.PURIFIER);
                break;
            case DEEP:
                machines.add(EcologicalMachine.PURIFIER);
                break;
            default:
                break;
        }
        
        // Get list of machine parts to distribute
        List<Item> parts = machines.stream().map(EcologicalMachine::getParts).flatMap(Collection::stream).collect(Collectors.toList());
        Iterator<Item> iterator = parts.iterator();
        
        // Distribute parts
        while(iterator.hasNext() && !containers.isEmpty()) {
            MetaBlock container = containers.remove(0);
            String dungeonId = container.getStringProperty("@");
            int x = container.getX();
            int y = container.getY();
            ctx.updateBlock(x, y, Layer.FRONT, "containers/chest-industrial", 1);
            MetaBlock componentChest = ctx.getZone().getMetaBlock(x, y);
            componentChest.setProperty("@", dungeonId);
            componentChest.setProperty("$", iterator.next().getId());
            iterator.remove();
        }
        
        // If there are still parts left but no containers, just discover 'em.
        for(Item part : parts) {
            ctx.getZone().discoverMachinePart(part);
        }
    }
    
    private void placeBrokenTeleporters(GeneratorContext ctx, List<MetaBlock> containers) {
        // Calculate the number of broken teleporters to place
        int amount = Math.min(containers.size(), Math.max(1, ctx.getWidth() * ctx.getHeight() / (ctx.nextInt(80000) + 120000)));
        
        // Place teleporters
        for(int i = 0; i < amount; i++) {
            MetaBlock container = containers.remove(0);
            int x = container.getX();
            int y = container.getY();
            ctx.updateBlock(x, y, Layer.FRONT, "mechanical/teleporter");
            ctx.getZone().removeMetaBlock(x, y); // Broken teleporters should have no metadata
        }
    }
    
    private void placeRandomSpawnBuilding(GeneratorContext ctx, int x) {
        Prefab spawnBuilding = spawnBuildings.next(ctx.getRandom());
        
        if(filled) {
            int min = ctx.getHeight() / 32;
            int y = min + ctx.nextInt(Math.max(1, ctx.nextInt(min)));
            ctx.placePrefab(spawnBuilding, x, y);
        } else {
            ctx.placePrefabSurface(spawnBuilding, x);
        }
    }
}
