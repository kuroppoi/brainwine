package brainwine.gameserver.dialog.input;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@JsonTypeInfo(use = Id.NAME, property = "type")
@JsonSubTypes({
    @Type(name = "text", value = DialogTextInput.class),
    @Type(name = "item", value = DialogItemInput.class),
    @Type(name = "color", value = DialogColorInput.class),
    @Type(names = {"text select", "select"}, value = DialogSelectInput.class)
})
@JsonInclude(Include.NON_DEFAULT)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class DialogInput {
    
    protected String key;
    
    public DialogInput setKey(String key) {
        this.key = key;
        return this;
    }
    
    public String getKey() {
        return key;
    }
}
