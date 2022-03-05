package brainwine.gameserver.dialog;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_DEFAULT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Dialog {
    
    private DialogType type = DialogType.STANDARD;
    private DialogAlignment alignment = DialogAlignment.LEFT;
    private List<DialogSection> sections = new ArrayList<>();
    private String title;
    private String target;
    
    @JsonCreator
    public Dialog(String text) {
        addSection(new DialogSection().setText(text));
    }
    
    @JsonCreator
    private Dialog(List<DialogSection> sections) {
        this.sections.addAll(sections);
    }
    
    @JsonCreator
    public Dialog() {}
    
    public Dialog setType(DialogType type) {
        this.type = type;
        return this;
    }
    
    public DialogType getType() {
        return type;
    }
    
    /**
     * v2 clients only.
     */
    public Dialog setAlignment(DialogAlignment alignment) {
        this.alignment = alignment;
        return this;
    }
    
    public DialogAlignment getAlignment() {
        return alignment;
    }
    
    public Dialog addSection(DialogSection section) {
        sections.add(section);
        return this;
    }
    
    public List<DialogSection> getSections() {
        return sections;
    }
    
    public Dialog setTitle(String title) {
        this.title = title;
        return this;
    }
    
    public String getTitle() {
        return title;
    }
    
    public Dialog setTarget(String target) {
        this.target = target;
        return this;
    }
    
    public String getTarget() {
        return target;
    }
}
