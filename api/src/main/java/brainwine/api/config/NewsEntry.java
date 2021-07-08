package brainwine.api.config;

import java.beans.ConstructorProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NewsEntry {
    
    public static final NewsEntry DEFAULT_NEWS = new NewsEntry("Default News", 
            "This news entry was automatically generated.\nEdit 'api.json' to make your own!", 
            "A long time ago...");
    private final String title;
    private final String content;
    private final String date;
    
    @ConstructorProperties({"title", "content", "published_at"})
    public NewsEntry(String title, String content, String date) {
        this.title = title;
        this.content = content;
        this.date = date;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getContent() {
        return content;
    }
    
    @JsonProperty("published_at")
    public String getDate() {
        return date;
    }
}
