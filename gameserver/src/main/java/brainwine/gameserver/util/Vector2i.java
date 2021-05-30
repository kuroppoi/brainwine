package brainwine.gameserver.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Vector2i {
    
    private int x;
    private int y;
    
    public Vector2i(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    @JsonCreator
    private Vector2i(int[] positions) {
        if(positions.length == 2) {
            x = positions[0];
            y = positions[1];
        } else {
            x = 1;
            y = 1;
        }
    }
    
    public void setX(int x) {
        this.x = x;
    }
    
    public int getX() {
        return x;
    }
    
    public void setY(int y) {
        this.y = y;
    }
    
    public int getY() {
        return y;
    }
}
