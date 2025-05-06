package brainwine.api.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.beans.ConstructorProperties;

public class BetaButton {
    private String title = "Visit Forums";
    private String url = "https://forums.deepworldgame.com/";

    public BetaButton() {}

    @ConstructorProperties({"title", "url"})
    public BetaButton(String title, String url) {
        this.title = title;
        this.url = url;
    }

    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    @JsonProperty("url")
    public String getUrl() {
        return url;
    }
}
