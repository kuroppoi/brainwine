package brainwine.gameserver.dialog.input;

public class DialogColorInput extends DialogInput {
    
    private String title;
    private String options;
    
    public DialogColorInput setTitle(String title) {
        this.title = title;
        return this;
    }
    
    public String getTitle() {
        return title;
    }
    
    public DialogColorInput setOptions(String options) {
        this.options = options;
        return this;
    }
    
    public String getOptions() {
        return options;
    }
}
