package brainwine.gameserver.dialog.input;

public class DialogItemInput extends DialogInput {
    
    private String title;
    private String options;
    
    public DialogItemInput setTitle(String title) {
        this.title = title;
        return this;
    }
    
    public String getTitle() {
        return title;
    }
    
    public DialogItemInput setOptions(String options) {
        this.options = options;
        return this;
    }
    
    public String getOptions() {
        return options;
    }
}
