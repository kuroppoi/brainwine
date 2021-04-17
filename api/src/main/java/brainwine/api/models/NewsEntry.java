package brainwine.api.models;

import java.beans.ConstructorProperties;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NewsEntry {
    
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
