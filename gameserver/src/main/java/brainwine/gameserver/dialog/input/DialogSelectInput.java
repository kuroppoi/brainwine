package brainwine.gameserver.dialog.input;

import java.util.Arrays;
import java.util.Collection;

public class DialogSelectInput extends DialogInput {
    
    private Collection<String> options;
    
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
}
