package brainwine.gameserver.dialog.input;

import java.util.Arrays;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DialogSelectInput extends DialogInput {
    
    private Collection<String> options;
    private int maxColumns;
    
    public DialogSelectInput setOptions(String... options) {
        return setOptions(Arrays.asList(options));
    }
    
    public DialogSelectInput setOptions(Collection<String> options) {
        this.options = options;
        return this;
    }
    
    public Collection<String> getOptions() {
        return options;
    }
    
    public DialogSelectInput setMaxColumns(int maxColumns) {
        this.maxColumns = maxColumns;
        return this;
    }
    
    @JsonProperty("max columns")
    public int getMaxColumns() {
        return maxColumns;
    }
}
