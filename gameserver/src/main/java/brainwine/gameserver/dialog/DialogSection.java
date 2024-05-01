package brainwine.gameserver.dialog;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;

import brainwine.gameserver.dialog.input.DialogInput;
import brainwine.gameserver.util.Vector2i;

@JsonInclude(Include.NON_DEFAULT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DialogSection {
    
    private List<DialogListItem> items = new ArrayList<>();
    private String title;
    private String text;
    private String textColor;
    private double textScale;
    private Vector2i location;
    private DialogInput input;
    
    public DialogSection addItem(DialogListItem item) {
        items.add(item);
        return this;
    }
    
    @JsonProperty("list")
    public List<DialogListItem> getItems() {
        return items;
    }
    
    public DialogSection setTitle(String title) {
        this.title = title;
        return this;
    }
    
    public String getTitle() {
        return title;
    }
    
    public DialogSection setText(String text) {
        this.text = text;
        return this;
    }
    
    public String getText() {
        return text;
    }
    
    /**
     * v2 clients only!
     * For v3 clients, use {@link #setText} with HTML color tags.
     * Example: {@code <color=#FF0000>Red Text!</color>}
     */
    public DialogSection setTextColor(String textColor) {
        this.textColor = textColor;
        return this;
    }
    
    @JsonProperty("text-color")
    public String getTextColor() {
        return textColor;
    }
    
    public DialogSection setTextScale(double textScale) {
        this.textScale = textScale;
        return this;
    }
    
    @JsonProperty("text-scale")
    public double getTextScale() {
        return textScale;
    }
    
    /**
     * v2 clients only!
     */
    public DialogSection setLocation(int x, int y) {
        this.location = new Vector2i(x, y);
        return this;
    }
    
    @JsonProperty("map")
    @JsonFormat(shape = Shape.ARRAY)
    public Vector2i getLocation() {
        return location;
    }
    
    public DialogSection setInput(DialogInput input) {
        this.input = input;
        return this;
    }
    
    public DialogInput getInput() {
        return input;
    }
}
