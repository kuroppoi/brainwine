package brainwine.gameserver.zone;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.entity.player.ChatType;
import brainwine.gameserver.entity.player.NotificationType;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemRegistry;
import brainwine.gameserver.item.ItemUseType;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.item.MetaType;
import brainwine.gameserver.item.ModType;
import brainwine.gameserver.prefab.Prefab;
import brainwine.gameserver.server.Message;
import brainwine.gameserver.server.messages.BlockChangeMessage;
import brainwine.gameserver.server.messages.BlockMetaMessage;
import brainwine.gameserver.server.messages.ChatMessage;
import brainwine.gameserver.server.messages.ConfigurationMessage;
import brainwine.gameserver.server.messages.LightMessage;
import brainwine.gameserver.server.messages.ZoneExploredMessage;
import brainwine.gameserver.server.messages.ZoneStatusMessage;
import brainwine.gameserver.util.MapHelper;
import brainwine.gameserver.util.MathUtils;
import brainwine.gameserver.util.Vector2i;

public class Zone {
    
    public static final int DEFAULT_CHUNK_WIDTH = 20;
    public static final int DEFAULT_CHUNK_HEIGHT = 20;
    private static final ThreadLocalRandom random = ThreadLocalRandom.current();
    private final String documentId;
    private String name;
    private final Biome biome;
    private final int width;
    private final int height;
    private final int chunkWidth = DEFAULT_CHUNK_WIDTH;
    private final int chunkHeight = DEFAULT_CHUNK_HEIGHT;
    private final int numChunksWidth;
    private final int numChunksHeight;
    private int[] surface;
    private int[] sunlight;
    private int[] depths;
    private boolean[] chunksExplored;
    private float time = (float)Math.random(); // TODO temporary
    private float temperature = 0;
    private float acidity = 1;
    private final ChunkManager chunkManager;
    private final WeatherManager weatherManager = new WeatherManager();
    private final EntityManager entityManager = new EntityManager(this);
    private final Queue<DugBlock> digQueue = new ArrayDeque<>();
    private final Set<Integer> pendingSunlight = new HashSet<>();
    private final Map<String, Integer> dungeons = new HashMap<>();
    private final Map<Integer, MetaBlock> metaBlocks = new HashMap<>();
    private final Map<Integer, MetaBlock> globalMetaBlocks = new HashMap<>();
    private final Map<Integer, MetaBlock> fieldBlocks = new HashMap<>();
    private long lastStatusUpdate = System.currentTimeMillis();
    
    protected Zone(String documentId, ZoneConfig config, ZoneData data) {
        this(documentId, config.getName(), config.getBiome(), config.getWidth(), config.getHeight());
        surface = data.getSurface();
        sunlight = data.getSunlight();
        setDepths(data.getDepths());
        
        // Ha ha silly me!
        // TODO make nifty setters for these too, perhaps?
        if(data.getPendingSunlight() != null) {
            pendingSunlight.addAll(data.getPendingSunlight());
        }
        
        if(data.getChunksExplored() != null) {
            chunksExplored = data.getChunksExplored();
        }
    }
    
    public Zone(String documentId, String name, Biome biome, int width, int height) {
        this.documentId = documentId;
        this.name = name;
        this.biome = biome;
        this.width = width;
        this.height = height;
        numChunksWidth = width / chunkWidth;
        numChunksHeight = height / chunkHeight;
        depths = new int[] {(int)(height * 0.6), (int)(height * 0.75), (int)(height * 0.9)};
        surface = new int[width];
        sunlight = new int[width];
        chunksExplored = new boolean[numChunksWidth * numChunksHeight];
        chunkManager = new ChunkManager(this);
        Arrays.fill(surface, height);
        Arrays.fill(sunlight, height);
    }
    
    @JsonCreator
    private static Zone fromId(String id) {
        return GameServer.getInstance().getZoneManager().getZone(id);
    }
    
    public void tick(float deltaTime) {
        long now = System.currentTimeMillis();
        weatherManager.tick(deltaTime);
        entityManager.tick(deltaTime);
        
        // One full cycle = 1200 seconds = 20 minutes
        time += deltaTime * (1.0F / 1200.0F);
        
        if(time >= 1.0F) {
            time -= 1.0F;
        }
                
        if(!getPlayers().isEmpty()) {
            if(now >= lastStatusUpdate + 4000) {
                sendMessage(new ZoneStatusMessage(getStatusConfig()));
                lastStatusUpdate = now;
            }
        }
        
        if(!digQueue.isEmpty()) {
            DugBlock dugBlock = digQueue.peek();
            
            if(now >= dugBlock.getTime()) {
                digQueue.poll();
                int x = dugBlock.getX();
                int y = dugBlock.getY();
                Block block = getBlock(x, y);
                
                if(block != null && block.getFrontItem().getId() == 519) {
                    updateBlock(x, y, Layer.FRONT, dugBlock.getItem(), dugBlock.getMod());
                }
            }
        }
    }
    
    /**
     * Sends a message to each player in this zone.
     * 
     * @param message The message to send.
     */
    public void sendMessage(Message message) {
        for(Player player : getPlayers()) {
            player.sendMessage(message);
        }
    }
    
    /**
     * Sends a message to each player who is near the specified chunk.
     * 
     * @param message The message to send.
     * @param chunk The chunk near which players must be.
     */
    public void sendMessageToChunk(Message message, Chunk chunk) {
        for(Player player : getPlayers()) {
            if(player.isChunkActive(chunk)) {
                player.sendMessage(message);
            }
        }
    }
    
    public void chat(Player sender, String text) {
        chat(sender, text, ChatType.CHAT);
    }
    
    /**
     * Broadcasts a chat message to all players.
     * 
     * @param sender The player who sent the message.
     * @param text The text.
     * @param type The display type.
     */
    public void chat(Player sender, String text, ChatType type) {
        sendMessage(new ChatMessage(sender.getId(), text, type));
    }
    
    public boolean isPointVisibleFrom(int x1, int y1, int x2, int y2) {
        return raycast(x1, y1, x2, y2) == null;
    }
    
    public Vector2i raycast(int x1, int y1, int x2, int y2) {
        List<Vector2i> path = raycast(x1, y1, x2, y2, false, false, false);
        
        if(path != null && !path.isEmpty()) {
            return path.get(0);
        }
        
        return null;
    }
    
    public Vector2i raynext(int x1, int y1, int x2, int y2) {
        List<Vector2i> path = raycast(x1, y1, x2, y2, true, true, true);
        
        if(path != null && path.size() > 1) {
            return path.get(1);
        }
        
        return null;
    }
    
    // Shamelessly ported from the zone kernel (https://github.com/bytebin/deepworld-gameserver/blob/master/ext/lib/zone.c#L798)
    private List<Vector2i> raycast(int x1, int y1, int x2, int y2, boolean path, boolean all, boolean next) {
        List<Vector2i> coords = new ArrayList<>();
        int x = x1;
        int y = y1;
        int diffX = Math.abs(x2 - x1);
        int diffY = Math.abs(y2 - y1);
        int signX = (int)Math.signum(x2 - x1);
        int signY = (int)Math.signum(y2 - y1);
        boolean swap = false;
        
        if(diffY > diffX) {
            int temp = diffX;
            diffX = diffY;
            diffY = temp;
            swap = true;
        }
        
        int d = 2 * diffY - diffX;
        
        for(int i = 0; i < diffX; i++) {
            if(!all && isBlockSolid(x, y)) {
                if(path) {
                    return coords;
                }
                
                coords.add(new Vector2i(x, y));
                return coords;
            }
            
            if(path) {
                coords.add(new Vector2i(x, y));
                
                if(next && i == 1) {
                    return coords;
                }
            }
            
            while(d >= 0) {
                d = d - 2 * diffX;
                
                if(swap) {
                    x += signX;
                } else {
                    y += signY;
                }
            }
            
            d = d + 2 * diffY;
            
            if(swap) {
                y += signY;
            } else {
                x += signX;
            }
        }
        
        return all ? coords : null;
    }
    
    public boolean isBlockSolid(int x, int y) {
        return isBlockSolid(x, y, true);
    }
    
    public boolean isBlockSolid(int x, int y, boolean checkAdjacents) {
        if(!areCoordinatesInBounds(x, y) || !isChunkLoaded(x, y)) {
            return true;
        }
        
        Block block = getBlock(x, y);
        Item item = block.getItem(Layer.FRONT);
        
        if(item.isDoor() && block.getFrontMod() % 2 == 0) {
            return true;
        } else if(!item.isDoor() && item.isSolid()) {
            return true;
        }
        
        if(checkAdjacents) {
            for(int i = -3; i <= 0; i++) {
                for(int j = 0; j <= 2; j++) {
                    int x1 = x + i;
                    int y1 = y + j;
                    
                    if(!areCoordinatesInBounds(x1, y1) || !isChunkLoaded(x1, y1)) {
                        continue;
                    }
                    
                    block = getBlock(x1, y1);
                    item = block.getFrontItem();
                    
                    if(item.getBlockWidth() > Math.abs(i) && item.getBlockHeight() > Math.abs(j)
                            && isBlockSolid(x1, y1, false)) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    public boolean isBlockOccupied(int x, int y, Layer layer) {
        if(!areCoordinatesInBounds(x, y)) {
            return false;
        }
        
        Block block = getBlock(x, y);
        Item item = block.getItem(layer);
        
        return !item.isAir() && !item.canPlaceOver();
    }
    
    public boolean isBlockProtected(int x, int y) {
        return isBlockProtected(x, y, null);
    }
    
    public boolean isBlockProtected(int x, int y, Player from) {
        for(MetaBlock fieldBlock : fieldBlocks.values()) {
            Item item = fieldBlock.getItem();
            int fX = fieldBlock.getX();
            int fY = fieldBlock.getY();
            int field = fieldBlock.getItem().getField();
            
            if(from == null || !ownsMetaBlock(fieldBlock, from)) {
                if(item.isDish()) {
                    if(MathUtils.inRange(x, y, fX, fY, field)) {
                        return true;
                    }
                } else if(item.hasField()) {
                    if(x == fX && y == fY) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    public boolean willDishOverlap(int x, int y, int field, Player player) {
        for(MetaBlock fieldBlock : fieldBlocks.values()) {
            int fX = fieldBlock.getX();
            int fY = fieldBlock.getY();
            int fField = fieldBlock.getItem().getField();
            
            if(MathUtils.inRange(x, y, fX, fY, field + fField) && !ownsMetaBlock(fieldBlock, player)) {
                return true;
            }
        }
        
        return false;
    }
    
    public Prefab chop(int x, int y, int width, int height) {
        if(!areCoordinatesInBounds(x, y) || !areCoordinatesInBounds(x + width, y + height)) {
            return null;
        }
        
        Block[] blocks = new Block[width * height];
        Map<Integer, Map<String, Object>> metadata = new HashMap<>();
        
        for(int i = 0; i < width; i++) {
            for(int j = 0; j < height; j++) {
                int index = j * width + i;
                Block block = getBlock(x + i, y + j);
                blocks[index] = new Block(block.getBaseItem(), block.getBackItem(), block.getBackMod(), block.getFrontItem(), block.getFrontMod(), block.getLiquidItem(), block.getLiquidMod());
                MetaBlock metaBlock = metaBlocks.get(getBlockIndex(x + i, j + y));
                
                if(metaBlock != null) {
                    Map<String, Object> data = MapHelper.copy(metaBlock.getMetadata());
                    
                    if(!data.isEmpty()) {
                        List<List<Integer>> positions = MapHelper.getList(data, ">", Collections.emptyList());
                        
                        for(List<Integer> position : positions) {
                            position.set(0, position.get(0) - x);
                            position.set(1, position.get(1) - y);
                        }
                        
                        metadata.put(index, data);
                    }
                }
            }
        }
        
        return new Prefab(width, height, blocks, metadata);
    }
    
    public void placePrefab(Prefab prefab, int x, int y) {
        placePrefab(prefab, x, y, random);
    }
    
    public void placePrefab(Prefab prefab, int x, int y, Random random) {
        int width = prefab.getWidth();
        int height = prefab.getHeight();
        Block[] blocks = prefab.getBlocks();
        int guardBlocks = 0;
        String dungeonId = prefab.isDungeon() ? UUID.randomUUID().toString() : null;
        boolean decay = prefab.hasDecay();
        boolean mirrored = prefab.isMirrorable() && random.nextBoolean();
        Map<Item, Item> replacedItems = new HashMap<>();
        
        // Replacements
        prefab.getReplacements().forEach((item, list) -> {
            replacedItems.put(item, list.next(random));
        });
        
        // Corresponding replacements
        prefab.getCorrespondingReplacements().forEach((item, data) -> {
            Item keyReplacement = replacedItems.get(data.getKey());
            
            if(keyReplacement != null) {
                Item replacement = data.getValues().get(keyReplacement);
                
                if(replacement != null) {
                    replacedItems.put(item, replacement);
                }
            }
        });
        
        for(int i = 0; i < width; i++) {
            for(int j = 0; j < height; j++) { 
                int index = j * width + (mirrored ? width - 1 - i : i);
                Block block = blocks[index];
                Item baseItem = replacedItems.getOrDefault(block.getBaseItem(), block.getBaseItem());
                Item backItem = replacedItems.getOrDefault(block.getBackItem(), block.getBackItem());
                Item frontItem = replacedItems.getOrDefault(block.getFrontItem(), block.getFrontItem());
                Item liquidItem = replacedItems.getOrDefault(block.getLiquidItem(), block.getLiquidItem());
                int backMod = block.getBackMod();
                int frontMod = block.getFrontMod();
                int liquidMod = block.getLiquidMod();
                
                // Update base item if it isn't empty
                if(!baseItem.isAir()) {
                    updateBlock(x + i, y + j, Layer.BASE, baseItem);
                }
                
                // Update back item if it isn't empty
                if(!backItem.isAir()) {
                    // Apply decay to back block
                    if(decay && backItem.getMod() == ModType.DECAY && random.nextBoolean()) {
                        backMod = random.nextInt(4) + 1;
                    }
                    
                    updateBlock(x + i, y + j, Layer.BACK, backItem, backMod);
                }
                
                // Update front item if either the back, front or liquid item isn't empty
                if(!backItem.isAir() || !frontItem.isAir() || !liquidItem.isAir()) {
                    // Apply mods
                    if(mirrored && frontItem.getMod() == ModType.ROTATION) {
                        // If rotation == mirror, swap mods 0 and 4, otherwise 1 and 3
                        if(frontItem.isMirrorable()) {
                            frontMod = frontMod == 0 ? 4 : frontMod == 4 ? 0 : frontMod;
                        } else {
                            frontMod = frontMod == 1 ? 3 : frontMod == 3 ? 1 : frontMod;
                        }
                    } else if(decay && frontItem.getMod() == ModType.DECAY && random.nextBoolean()) {
                        frontMod = random.nextInt(4) + 1;
                    }
                    
                    int offset = mirrored ? -(frontItem.getBlockWidth() - 1) : 0;
                    
                    // Clear the block it would normally occupy
                    if(offset != 0) {
                        updateBlock(x + i, y + j, Layer.FRONT, 0);
                    }
                    
                    Map<String, Object> metadata = prefab.getMetadata(index);
                    metadata = metadata == null ? new HashMap<>() : MapHelper.copy(metadata);
                    
                    // Add dungeon id to guard blocks and containers, and increment guard block count if applicable
                    if(dungeonId != null && frontItem.hasUse(ItemUseType.CONTAINER, ItemUseType.GUARD)) {
                        metadata.put("@", dungeonId);
                        
                        if(frontItem.hasUse(ItemUseType.GUARD)) {
                            addGuardianEntities(metadata, frontItem.getGuardLevel(), y + j, random);
                            guardBlocks++;
                        }
                    }
                    
                    // Determine lootability for containers
                    if(prefab.hasLoot() && frontItem.hasUse(ItemUseType.CONTAINER)) {
                        // If the container is a "high end" container, make it lootable. Otherwise 10% chance.
                        if(frontItem.hasUse(ItemUseType.FIELDABLE) || random.nextDouble() <= 0.1) {
                            metadata.put("$", "?");
                            frontMod = 1;
                        }
                    }
                    
                    // Block is linked, offset positions
                    if(metadata.containsKey(">")) {
                        List<List<Integer>> positions = MapHelper.getList(metadata, ">", Collections.emptyList());
                        
                        for(List<Integer> position : positions) {
                            int pX = position.get(0);
                            int pY = position.get(1);
                            int pIndex = pY * width + pX;
                            int pOffset = 0;
                            
                            // Make sure that the linked block is in bounds
                            if(pIndex >= 0 && pIndex < blocks.length) {
                                // Create an offset in case the block is bigger than 1x1
                                Item linkedItem = blocks[pIndex].getFrontItem();
                                linkedItem = replacedItems.getOrDefault(linkedItem, linkedItem);
                                pOffset = -(linkedItem.getBlockWidth() - 1);
                            }
                            
                            position.set(0, (mirrored ? width - 1 - pX + pOffset : pX) + x);
                            position.set(1, position.get(1) + y);
                        }
                    }
                    
                    updateBlock(x + i + offset, y + j, Layer.FRONT, frontItem, frontMod, null, metadata);
                }
                
                // Update liquid item if any block isn't empty
                if(!liquidItem.isAir() || !baseItem.isAir() || !backItem.isAir() || !frontItem.isAir()) {
                    updateBlock(x + i, y + j, Layer.LIQUID, liquidItem, liquidMod);
                }
            }
        }
        
        if(guardBlocks > 0) {
            dungeons.put(dungeonId, guardBlocks);
        }
    }
    
    private void indexDungeons() {
        List<MetaBlock> guardBlocks = getMetaBlocksWithUse(ItemUseType.GUARD);
        
        for(MetaBlock metaBlock : guardBlocks) {
            Map<String, Object> metadata = metaBlock.getMetadata();
            String dungeonId = MapHelper.getString(metadata, "@");
            
            if(dungeonId != null) {
                addGuardianEntities(metadata, metaBlock.getItem().getGuardLevel(), metaBlock.getY());
                int numGuardBlocks = dungeons.getOrDefault(dungeonId, 0);
                numGuardBlocks++;
                dungeons.put(dungeonId, numGuardBlocks);
            }
        }
    }
    
    private void addGuardianEntities(Map<String, Object> metadata, int guardLevel, int depth) {
        addGuardianEntities(metadata, guardLevel, depth, random);
    }
    
    private void addGuardianEntities(Map<String, Object> metadata, int guardLevel, int depth, Random random) {
        List<String> guardians = MapHelper.getList(metadata, "!");
        
        if(guardians == null) {
            guardians = new ArrayList<>();
            
            if(guardLevel >= 5) {
                guardians.add("brains/large");
            } else {
                int effectiveGuardLevel = guardLevel + depth / 200;
                String[][] groups = {
                    {"creatures/bat-auto", "creatures/bat-auto"},
                    {"brains/small"},
                    {"brains/small", "creatures/bat-auto"},
                    {"brains/small", "creatures/bat-auto"},
                    {"brains/small", "creatures/bat-auto"},
                    {"brains/medium"},
                    {"brains/medium"},
                    {"brains/medium", "brains/small"},
                    {"brains/medium-dire"},
                    {"brains/medium-dire", "brains/small"},
                    {"brains/medium-dire", "brains/small"},
                };
                
                int max = Math.max(1, groups.length - 6);
                String[] group = Stream.of(groups)
                        .skip(Math.min(effectiveGuardLevel, groups.length - max))
                        .limit(max)
                        .collect(Collectors.toList())
                        .get(random.nextInt(max));
                guardians.addAll(Arrays.asList(group));
            }
            
            metadata.put("!", guardians);
        }
    }
    
    public void destroyGuardBlock(String dungeonId, Player destroyer) {
        if(dungeons.containsKey(dungeonId)) {
            int guardBlocks = dungeons.get(dungeonId);
            guardBlocks--;
            
            if(guardBlocks <= 0) {
                dungeons.remove(dungeonId);
                destroyer.getStatistics().trackDungeonRaided();
                destroyer.notify("You raided a dungeon!", NotificationType.ACCOMPLISHMENT);
                destroyer.notifyPeers(String.format("%s raided a dungeon.", destroyer.getName()), NotificationType.SYSTEM);
            } else {
                dungeons.put(dungeonId, guardBlocks);
            }
        }
    }
    
    public boolean isDungeonIntact(String id) {
        return dungeons.containsKey(id);
    }
    
    public void digBlock(int x, int y) {
        if(!areCoordinatesInBounds(x, y)) {
            return;
        }
        
        Block block = getBlock(x, y);
        digQueue.add(new DugBlock(x, y, block.getFrontItem(), block.getFrontMod(), System.currentTimeMillis() + 10000));
        updateBlock(x, y, Layer.FRONT, 519, 0);
    }
    
    public void updateBlock(int x, int y, Layer layer, int item) {
        updateBlock(x, y, layer, item, 0);
    }
    
    public void updateBlock(int x, int y, Layer layer, int item, int mod) {
        updateBlock(x, y, layer, item, mod, null);
    }
    
    public void updateBlock(int x, int y, Layer layer, int item, int mod, Player owner) {
        updateBlock(x, y, layer, item, mod, owner, null);
    }
    
    public void updateBlock(int x, int y, Layer layer, int item, int mod, Player owner, Map<String, Object> metadata) {
        updateBlock(x, y, layer, ItemRegistry.getItem(item), mod, owner, metadata);
    }
    
    public void updateBlock(int x, int y, Layer layer, Item item) {
        updateBlock(x, y, layer, item, 0);
    }
    
    public void updateBlock(int x, int y, Layer layer, Item item, int mod) {
        updateBlock(x, y, layer, item, mod, null);
    }
    
    public void updateBlock(int x, int y, Layer layer, Item item, int mod, Player owner) {
        updateBlock(x, y, layer, item, mod, owner, null);
    }
    
    public void updateBlock(int x, int y, Layer layer, Item item, int mod, Player owner, Map<String, Object> metadata) {
        if(!areCoordinatesInBounds(x, y)) {
            return;
        }
        
        Chunk chunk = getChunk(x, y);        
        chunk.getBlock(x, y).updateLayer(layer, item, mod);
        chunk.setModified(true);
        sendMessageToChunk(new BlockChangeMessage(x, y, layer, item, mod), chunk);
        
        if(layer == Layer.FRONT) {
            if(item.isWhole()) {
                updateBlock(x, y, Layer.LIQUID, Item.AIR, 0);
            }
            
            if(item.hasMeta()) {
                if(metadata == null) {
                    setMetaBlock(x, y, item, owner, new HashMap<>());
                } else {
                    setMetaBlock(x, y, item, owner, metadata);
                }
            } else if(metaBlocks.containsKey(getBlockIndex(x, y))) {
                setMetaBlock(x, y, 0);
            }
            
            entityManager.trySpawnBlockEntity(x, y);
            
            if(item.isWhole() && y < sunlight[x]) {
                sunlight[x] = y;
            } else if(!item.isWhole() && y == sunlight[x]) {
                recalculateSunlight(x, sunlight[x]);
            }
            
            sendMessageToChunk(new LightMessage(x, getSunlight(x, 1)), chunk);
        }
    }
    
    /**
     * @param x The x position of the block.
     * @param y The y position of the block.
     * @return The index of the block at the specified coordinates.
     */
    public int getBlockIndex(int x, int y) {
        return y * width + x;
    }
    
    public Block getBlock(int x, int y) {
        if(areCoordinatesInBounds(x, y)) {
            Chunk chunk = getChunk(x, y);
            return chunk.getBlock(x, y);
        }
        
        return null;
    }
    
    public void setMetaBlock(int x, int y, int item) {
        setMetaBlock(x, y, item, null, null);
    }
    
    public void setMetaBlock(int x, int y, int item, Player owner, Map<String, Object> data) {
        setMetaBlock(x, y, ItemRegistry.getItem(item), owner, data);
    }
    
    public void setMetaBlock(int x, int y, Item item, Player owner, Map<String, Object> data) {
        if(!areCoordinatesInBounds(x, y)) {
            return;
        }
        
        MetaType meta = item.getMeta();
        int index = getBlockIndex(x, y);
        Map<String, Object> metadata = data == null ? new HashMap<>() : MapHelper.copy(data);
        Map<String, Object> toSend = MapHelper.copy(metadata);
        toSend.put("i", item.getId());
        
        if(owner != null) {
            toSend.put("p", owner.getDocumentId());
        }
        
        if(item.hasMeta()) {
            MetaBlock metaBlock = new MetaBlock(x, y, item, owner, metadata);
            metaBlocks.put(index, metaBlock);
            indexMetaBlock(index, metaBlock);
        } else if(metaBlocks.containsKey(index)) {
            meta = metaBlocks.remove(index).getItem().getMeta();
            toSend.clear();
            unindexMetaBlock(index);
        }
        
        switch(meta) {
        case LOCAL:
            sendMessageToChunk(new BlockMetaMessage(x, y, toSend), getChunk(x, y));
            break;
        case GLOBAL:
            sendMessage(new BlockMetaMessage(x, y, Collections.emptyMap()));
            sendMessage(new BlockMetaMessage(x, y, toSend));
            break;
        default:
            break;
        }
    }
    
    private void indexMetaBlock(int index, MetaBlock block) {
        Item item = block.getItem();
        metaBlocks.put(index, block);
        
        if(item.getMeta() == MetaType.GLOBAL) {
            globalMetaBlocks.put(index, block);
        }
        
        if(item.hasField()) {
            fieldBlocks.put(index, block);
        }
    }
    
    private void unindexMetaBlock(int index) {
        metaBlocks.remove(index);
        globalMetaBlocks.remove(index);
        fieldBlocks.remove(index);
    }
    
    protected void setMetaBlocks(List<MetaBlock> metaBlocks) {
        for(MetaBlock metaBlock : metaBlocks) {
            int x = metaBlock.getX();
            int y = metaBlock.getY();
            
            if(areCoordinatesInBounds(x, y)) {
                indexMetaBlock(getBlockIndex(x, y), metaBlock);
            }
        }
        
        indexDungeons();
    }
    
    private boolean ownsMetaBlock(MetaBlock metaBlock, Player player) {
        if(!metaBlock.hasOwner()) {
            return false;
        }
        
        return player.getDocumentId().equals(metaBlock.getOwner());
    }
    
    public MetaBlock getMetaBlock(int x, int y) {
        return metaBlocks.get(getBlockIndex(x, y));
    }
    
    public MetaBlock getMetaBlock(int index) {
        return metaBlocks.get(index);
    }
    
    public MetaBlock getRandomSpawnBlock() {
        List<MetaBlock> spawnBlocks = getMetaBlocks(block -> block.getItem().getId() == 891 || block.getItem().getId() == 934);
        return spawnBlocks.isEmpty() ? null : spawnBlocks.get((int)(Math.random() * spawnBlocks.size()));
    }
    
    public List<MetaBlock> getMetaBlocksWithUse(ItemUseType useType) {
        return getMetaBlocks(metaBlock -> metaBlock.getItem().hasUse(useType));
    }
    
    public List<MetaBlock> getMetaBlocksWithItem(Item item) {
        return getMetaBlocksWithItem(item.getId());
    }
        
    public List<MetaBlock> getMetaBlocksWithItem(int item) {
        return getMetaBlocks(metaBlock -> metaBlock.getItem().getId() == item);
    }
    
    public List<MetaBlock> getMetaBlocks(Predicate<MetaBlock> predicate) {
        return metaBlocks.values().stream().filter(predicate).collect(Collectors.toList());
    }
    
    public Collection<MetaBlock> getMetaBlocks() {
        return Collections.unmodifiableCollection(metaBlocks.values());
    }
    
    public List<MetaBlock> getLocalMetaBlocksInChunk(int chunkIndex) {
        return getMetaBlocks(block -> block.getItem().getMeta() == MetaType.LOCAL
                && chunkIndex == getChunkIndex(block.getX(), block.getY()));
    }
    
    public Collection<MetaBlock> getGlobalMetaBlocks() {
        return Collections.unmodifiableCollection(globalMetaBlocks.values());
    }
    
    public List<Entity> getEntitiesInRange(float x, float y, float range) {
        return entityManager.getEntitiesInRange(x, y, range);
    }
    
    public Player getRandomPlayerInRange(float x, float y, float range) {
        return entityManager.getRandomPlayerInRange(x, y, range);
    }
    
    public List<Player> getPlayersInRange(float x, float y, float range) {
        return entityManager.getPlayersInRange(x, y, range);
    }
    
    public void spawnEntity(Entity entity, int x, int y) {
        entityManager.spawnEntity(entity, x, y);
    }
    
    public void spawnEntity(Entity entity, int x, int y, boolean effect) {
        entityManager.spawnEntity(entity, x, y, effect);
    }
    
    public void addEntity(Entity entity) {
        entityManager.addEntity(entity);
    }
    
    public void removeEntity(Entity entity) {
        entityManager.removeEntity(entity);
    }
    
    public Entity getEntity(int entityId) {
        return entityManager.getEntity(entityId);
    }
    
    public int getEntityCount() {
        return entityManager.getEntityCount();
    }
    
    public Collection<Entity> getEntities() {
        return entityManager.getEntities();
    }
    
    public Npc getNpc(int entityId) {
        return entityManager.getNpc(entityId);
    }
    
    public int getNpcCount() {
        return entityManager.getNpcCount();
    }
    
    public Collection<Npc> getNpcs() {
        return entityManager.getNpcs();
    }
    
    public Player getPlayer(int entityId) {
        return entityManager.getPlayer(entityId);
    }
    
    public Player getPlayer(String name) {
        return entityManager.getPlayer(name);
    }
    
    public int getPlayerCount() {
        return entityManager.getPlayerCount();
    }
    
    public Collection<Player> getPlayers() {
        return entityManager.getPlayers();
    }
    
    public boolean areCoordinatesInBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }
    
    protected void onChunkLoaded(Chunk chunk) {
        // Update sunlight
        if(!pendingSunlight.isEmpty()) {
            int chunkX = chunk.getX();
            
            for(int x = chunkX; x < chunkX + chunk.getWidth(); x++) {
                if(pendingSunlight.contains(x)) {
                    recalculateSunlight(x, sunlight[x]);
                    sendMessageToChunk(new LightMessage(x, getSunlight(x, 1)), chunk);
                }
            }
        }
        
        // Spawn block-related entities
        for(int x = 0; x < chunk.getWidth(); x++) {
            for(int y = 0; y < chunk.getHeight(); y++) {
                entityManager.trySpawnBlockEntity(chunk.getX() + x, chunk.getY() + y);
            }
        }
    }
    
    protected void onChunkUnloaded(Chunk chunk) {
        // TODO
    }
    
    public void saveChunks() {
        chunkManager.saveChunks();
    }
    
    public List<Chunk> getVisibleChunks() {
        return chunkManager.getVisibleChunks();
    }
        
    /**
     * Should only be called by zone gen.
     */
    public void putChunk(int index, Chunk chunk) {
        chunkManager.putChunk(index, chunk);
    }
    
    public boolean isChunkLoaded(int x, int y) {
        return chunkManager.isChunkLoaded(x, y);
    }
    
    public boolean isChunkLoaded(int index) {
        return chunkManager.isChunkLoaded(index);
    }
    
    public boolean isChunkIndexInBounds(int index) {
        return chunkManager.isChunkIndexInBounds(index);
    }
    
    public int getChunkIndex(int x, int y) {
        return chunkManager.getChunkIndex(x, y);
    }
    
    public Chunk getChunk(int x, int y) {
        return chunkManager.getChunk(x, y);
    }
    
    public Chunk getChunk(int index) {
        return chunkManager.getChunk(index);
    }
    
    public Collection<Chunk> getLoadedChunks() {
        return chunkManager.getLoadedChunks();
    }
    
    public void setSurface(int x, int surface) {
        if(x < 0 || x >= width) {
            return;
        }
        
        this.surface[x] = surface;
    }
    
    public int[] getSurface() {
        return surface;
    }
    
    public void recalculateSunlight(int x, int startY) {
        for(int y = startY; y < height; y++) {
            if(isChunkLoaded(x, y)) {
                Block block = getBlock(x, y);
                
                if(block.getFrontItem().isWhole()) {
                    sunlight[x] = y;
                    pendingSunlight.remove(x);
                    return;
                }
            } else {
                sunlight[x] = y;
                pendingSunlight.add(x);
                return;
            }
        }
    }
    
    public Set<Integer> getPendingSunlight() {
        return Collections.unmodifiableSet(pendingSunlight);
    }
    
    public void setSunlight(int x, int sunlight) {
        if(x < 0 || x >= width) {
            return;
        }
        
        this.sunlight[x] = sunlight;
    }
    
    public int[] getSunlight(int x, int length) {
        int[] sunlight = new int[length];
        
        if(x >= 0 && x + length < width) {
            System.arraycopy(this.sunlight, x, sunlight, 0, length);
        }
        
        return sunlight;
    }
    
    public int[] getSunlight() {
        return sunlight;
    }
    
    public void setDepths(int deep, int deeper, int deepest) {        
        depths[0] = deep;
        depths[1] = deeper;
        depths[2] = deepest;
    }
    
    public void setDepths(int[] depths) {
        if(depths != null && depths.length == 3) {
            setDepths(depths[0], depths[1], depths[2]);
        }
    }
    
    public int[] getDepths() {
        return depths;
    }
    
    public boolean exploreArea(int x, int y) {
        return exploreArea(x, y, null);
    }
    
    public boolean exploreArea(int x, int y, Player explorer) {
        if(!areCoordinatesInBounds(x, y)) {
            return false;
        }
        
        int chunkIndex = getChunkIndex(x, y);
        
        if(chunksExplored[chunkIndex]) {
            return false;
        }
        
        if(explorer != null && y - y % chunkHeight >= surface[x - x % chunkWidth]) {
            explorer.getStatistics().trackAreaExplored();
        }
        
        sendMessage(new ZoneExploredMessage(chunkIndex));
        return chunksExplored[chunkIndex] = true;
    }
    
    public File getDirectory() {
    	return new File("zones", documentId);
    }
    
    /**
     * @return A float between 0 and 1, where 0 is completely unexplored and 1 is fully explored.
     */
    public float getExplorationProgress() {
        return (float)getChunksExploredCount() / (numChunksWidth * numChunksHeight);
    }
    
    public boolean[] getChunksExplored() {
        return chunksExplored;
    }
    
    public int getChunksExploredCount() {
        int count = 0;
        
        for(boolean explored : chunksExplored) {
            if(explored) {
                count++;
            }
        }
        
        return count;
    }
    
    public String getDocumentId() {
        return documentId;
    }
    
    public int getSeed() {
        return (int)(UUID.fromString(documentId).getMostSignificantBits() >> 32);
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public Biome getBiome() {
        return biome;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public int getChunkWidth() {
        return chunkWidth;
    }
    
    public int getChunkHeight() {
        return chunkHeight;
    }
    
    public int getNumChunksWidth() {
        return numChunksWidth;
    }
    
    public int getNumChunksHeight() {
        return numChunksHeight;
    }
    
    public int getChunkCount() {
        return numChunksWidth * numChunksHeight;
    }
    
    /**
     * @return A {@link Map} containing all the data necessary for use in {@link ConfigurationMessage}.
     */
    public Map<String, Object> getClientConfig(Player player) {
        Map<String, Object> config = new HashMap<>();
        config.put("id", documentId);
        config.put("name", name);
        config.put("biome", biome);
        config.put("size", new int[]{width, height});
        config.put("chunk_size", new int[]{chunkWidth, chunkHeight});
        config.put("surface", surface);
        config.put("chunks_explored", chunksExplored);
        config.put("chunks_explored_count", getChunksExploredCount());
        Map<String, Object> depth = new HashMap<>();
        List<Object> earth = new ArrayList<>();
        
        if(player.isV3()) {
            earth.add(Arrays.asList(depths[2], "ground/earth-deepest"));
            earth.add(Arrays.asList(depths[1], "ground/earth-deeper"));
            earth.add(Arrays.asList(depths[0], "ground/earth-deep"));
            depth.put("ground/earth", earth);
        } else {
            String key = biome == Biome.PLAIN ? "temperate" : biome.getId();
            earth.add(Arrays.asList(depths[0], String.format("%s/earth-front-deep", key)));
            earth.add(Arrays.asList(depths[1], String.format("%s/earth-front-deeper", key)));
            earth.add(Arrays.asList(depths[2], String.format("%s/earth-front-deepest", key)));
            depth.put(String.format("%s/earth-front", key), earth);
        }
        
        config.put("depth", depth);
        return config;
    }
    
    /**
     * @return A {@link Map} containing all the data necessary for use in {@link ZoneStatusMessage}.
     */
    public Map<String, Object> getStatusConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("w", new int[] {
                (int)(time * 10000), 
                (int)(temperature * 10000), 
                (int)(weatherManager.getPrecipitation() * 10000), 
                (int)(weatherManager.getPrecipitation() * 10000), 
                (int)(weatherManager.getPrecipitation() * 10000), 
                (int)(acidity * 10000)
        });
        return config;
    }
}
