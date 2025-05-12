package brainwine.gameserver.zone;

import brainwine.gameserver.zone.dynamics.ZoneDynamic;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DynamicsManager {
    Zone zone;
    Map<Class<? extends ZoneDynamic>, Deque<ZoneDynamic>> ongoingDynamics = new HashMap<>();

    public DynamicsManager(Zone zone) {
        this.zone = zone;
    }

    public boolean hasOngoingDynamic(Class<? extends ZoneDynamic> dynamicType) {
        Deque<ZoneDynamic> val = ongoingDynamics.get(dynamicType);
        return val != null && !val.isEmpty();
    }

    public void beginDynamic(ZoneDynamic dynamic) {
        Deque<ZoneDynamic> dynamicsOfType = ongoingDynamics.getOrDefault(dynamic.getClass(), new LinkedList<>());
        int maxInstances = dynamic.maxInstances();

        if(maxInstances > 0) while(dynamicsOfType.size() >= maxInstances) {
            dynamicsOfType.removeFirst().close();
        }

        dynamicsOfType.addLast(dynamic);
        ongoingDynamics.put(dynamic.getClass(), dynamicsOfType);
    }

    public void tick(float deltaTime) {
        List<Class<? extends ZoneDynamic>> keysToDelete = new ArrayList<>();

        for(Map.Entry<Class<? extends ZoneDynamic>, Deque<ZoneDynamic>> pair : ongoingDynamics.entrySet()) {
            Deque<ZoneDynamic> dynamics = pair.getValue();
            List<ZoneDynamic> finished = new ArrayList<>();
            for(ZoneDynamic dynamic : pair.getValue()) {
                dynamic.tick(deltaTime);
                if(dynamic.isFinished()) {
                    dynamic.close();
                    finished.add(dynamic);
                }
            }
            dynamics.removeAll(finished);
            if(dynamics.isEmpty()) {
                keysToDelete.add(pair.getKey());
            }
        }

        for(Class<? extends ZoneDynamic> key : keysToDelete) {
            ongoingDynamics.remove(key);
        }
    }

    public <T extends ZoneDynamic> Deque<ZoneDynamic> getOngoingDynamics(Class<T> clazz) {
        return ongoingDynamics.getOrDefault(clazz, new LinkedList<>());
    }
}
