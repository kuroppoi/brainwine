package brainwine.gameserver.zone.gen.surface;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

import brainwine.gameserver.item.ItemRegistry;
import brainwine.gameserver.util.WeightedMap;
import brainwine.gameserver.zone.gen.models.RubbleType;

@JsonIgnoreProperties({"items", "mods"})
public class RubbleSurfaceDecorator extends ItemSurfaceDecorator {
    
    @JsonSetter(value = "rubble_types", nulls = Nulls.SKIP, contentNulls = Nulls.SKIP)
    protected void setRubbleTypes(WeightedMap<RubbleType> types) {        
        types.getEntries().forEach((type, weight) -> {
            int[] itemIds = type.getItemIds();
            
            for(int itemId : itemIds) {
                items.addEntry(ItemRegistry.getItem(itemId), weight / (double)itemIds.length);
            }
        });
    }
}
