package brainwine.gameserver.zone.gen.surface;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SurfaceRegionType {
    
    @JsonProperty("frequency")
    private double frequency = 1;
    
    @JsonProperty("decorators")
    private List<SurfaceDecorator> decorators = new ArrayList<>();
    
    @JsonCreator
    private SurfaceRegionType() {}
    
    public double getFrequency() {
        return frequency;
    }
    
    public List<SurfaceDecorator> getDecorators() {
        return decorators;
    }
}
