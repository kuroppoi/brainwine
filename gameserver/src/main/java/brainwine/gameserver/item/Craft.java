package brainwine.gameserver.item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Craft {
    @JsonProperty("crafter")
    private String crafter;

    private Map<String, List<CraftingRequirement>> options;

    @JsonCreator
    private Craft(Map<String, Object> inp) {
        crafter = (String)inp.get("crafter");
        Map<String, Map<String, Integer>> map = (Map<String, Map<String, Integer>>)inp.get("options");

        options = new HashMap<String, List<CraftingRequirement>>();

        for (Map.Entry<String, Map<String, Integer>> entry : map.entrySet()) {
            List<CraftingRequirement> requirements = entry.getValue().entrySet().stream()
                .map(e -> new CraftingRequirement(new LazyItemGetter(e.getKey()), e.getValue()))
                .toList();
            
            options.put(entry.getKey(), requirements);
        }
    }

    public String getCrafter() {
        return crafter;
    }

    public Map<String, List<CraftingRequirement>> getOptions() {
        return options;
    }
    
}
