package brainwine.gameserver.zone.dynamics;

import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.util.MathUtils;
import brainwine.gameserver.util.Vector2i;
import brainwine.gameserver.util.WeightedMap;
import brainwine.gameserver.zone.Zone;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Invasion extends ZoneDynamic {
    private long lastInvasionWaveAt = System.currentTimeMillis();
    private long timeUntilNextInvasionWave;
    private int currentInvasionWave = 0;
    private int totalWaves = 4;
    private final List<Integer> invaders = new ArrayList<>();
    private WeightedMap<String> invaderTable;

    public Invasion(Zone zone, WeightedMap<String> invaderTable, int totalWaves) {
        super(zone);
        this.invaderTable = invaderTable;
        this.totalWaves = totalWaves;
    }

    public List<Entity> getTargets() {
        return new ArrayList<>();
    }

    public int getNumInvaders() {
        return currentInvasionWave >= 3 && Math.random() < 0.5 ? 2 : 1;
    }

    @Override
    public void tick(float deltaTime) {
        if(currentInvasionWave >= totalWaves) return;
        currentInvasionWave = Math.max(0, currentInvasionWave);

        List<Entity> targets = getTargets().stream()
                .filter(p -> (p.getZone() == zone && (!p.isPlayer() || ((Player)p).isOnline() && !((Player)p).isGodMode())))
                .collect(Collectors.toList());
        if(targets.isEmpty()) {
            currentInvasionWave = totalWaves;
            return;
        }

        if(lastInvasionWaveAt + timeUntilNextInvasionWave < System.currentTimeMillis()) {
            int numInvaders = getNumInvaders();

            List<Vector2i> eligiblePositions = new ArrayList<>(8);
            for(Entity currentInvasionTarget : targets) {
                if(currentInvasionTarget.isDead()) continue;

                boolean found = false;
                for(int x = -1; x <= 1; x++) {
                    for(int y = -1; y <= 1; y++) {
                        if(x == 0 && y == 0) continue;
                        int blockX = currentInvasionTarget.getBlockX() + x;
                        int blockY = currentInvasionTarget.getBlockY() + y;
                        if(zone.areCoordinatesInBounds(blockX, blockY) && !zone.isBlockOccupied(blockX, blockY, Layer.FRONT)) {
                            eligiblePositions.add(new Vector2i(blockX, blockY));
                            found = true;
                        }
                    }
                }
                if(!found) {
                    eligiblePositions.add(new Vector2i(currentInvasionTarget.getBlockX(), currentInvasionTarget.getBlockY()));
                }
            }

            if(!eligiblePositions.isEmpty()) for(int i = 0; i < numInvaders; i++) {
                Vector2i pos = eligiblePositions.get((int)(Math.random() * eligiblePositions.size()));
                Npc npc = zone.spawnEntity(invaderTable.next(), pos.getX(), pos.getY());
                this.invaders.add(npc.getId());
            }

            // Determine interval until next wave
            double minInterval = new double[] {3000, 1000, 500, 0}[Math.min(3, currentInvasionWave)];
            double maxInterval = new double[] {4000, 2000, 1500, 1000}[Math.min(3, currentInvasionWave)];
            timeUntilNextInvasionWave = (long) MathUtils.lerp(minInterval, maxInterval, Math.random());

            currentInvasionWave++;
            lastInvasionWaveAt = System.currentTimeMillis();
        }
    }

    @Override
    public boolean isFinished() {
        return currentInvasionWave >= totalWaves && (
                zone.getPlayers().isEmpty()
                        || invaders.stream().allMatch(id -> zone.getEntity(id) == null || zone.getEntity(id).isDead())
        );
    }

    @Override
    public void close() {
        for(int entityId : invaders) {
            Entity e = zone.getEntity(entityId);
            if(e != null) {
                zone.spawnEffect(e.getX(), e.getY(), "bomb-teleport", 4);
                e.setHealth(0.0f);
            }
        }
    }
}
