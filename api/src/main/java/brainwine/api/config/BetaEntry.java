package brainwine.api.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.beans.ConstructorProperties;

public class BetaEntry {
    private String title = "Learn More";
    private String content = "Thank you for joining us on this BrainWine server!";
    private BetaButton button = new BetaButton();

    public BetaEntry() {}

    @ConstructorProperties({"title", "content", "button"})
    public BetaEntry(String title, String content, BetaButton button) {
        this.title = title;
        this.content = content;
        this.button = button;
    }

    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    @JsonProperty("content")
    public String getContent() {
        return content;
    }

    @JsonProperty("button")
    public BetaButton getButton() {
        return button;
    }
}
