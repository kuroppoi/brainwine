package brainwine.gameserver.dialog.input;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DialogTextInput extends DialogInput {
    
    private String placeHolder;
    private int maxLength;
    private boolean password;
    
    public DialogTextInput setPlaceHolder(String placeHolder) {
        this.placeHolder = placeHolder;
        return this;
    }
    
    public String getPlaceHolder() {
        return placeHolder;
    }
    
    public DialogTextInput setMaxLength(int maxLength) {
        this.maxLength = maxLength;
        return this;
    }
    
    @JsonProperty("maxlength")
    @JsonAlias("max")
    public int getMaxLength() {
        return maxLength;
    }
    
    public DialogTextInput setPassword(boolean password) {
        this.password = password;
        return this;
    }
    
    public boolean isPassword() {
        return password;
    }
}
