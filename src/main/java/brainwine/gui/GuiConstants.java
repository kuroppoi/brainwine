package brainwine.gui;

import java.awt.Color;

public class GuiConstants {
    
    public static final String GITHUB_REPOSITORY_URL = "https://github.com/kuroppoi/brainwine";
    public static final String STEAM_REGISTRY_LOCATION = "HKCU\\SOFTWARE\\Valve\\Steam";
    public static final String DEEPWORLD_PLAYERPREFS = "HKCU\\SOFTWARE\\Bytebin LLC\\Deepworld";
    public static final String DEEPWORLD_STEAM_ID = "340810";
    public static final String DEEPWORLD_ASSEMBLY_PATH = "/steamapps/common/Deepworld/Deepworld_Data/Managed/Assembly-CSharp.dll";
    public static final String STEAM_RUN_GAME_URL = String.format("steam://rungameid/%s", DEEPWORLD_STEAM_ID);
    public static final String STEAM_COMMUNITY_HUB_URL = String.format("steam://url/GameHub/%s", DEEPWORLD_STEAM_ID);
    public static final String HTTP_STEAM_DOWNLOAD_URL = "https://store.steampowered.com/about";
    public static final String HTTP_COMMUNITY_HUB_URL = String.format("https://steamcommunity.com/app/%s", DEEPWORLD_STEAM_ID);
    public static final Color ERROR_COLOR = Color.RED.darker();
    public static final Color WARNING_COLOR = Color.YELLOW.darker();
    public static final Color INFO_COLOR = Color.WHITE.darker();
}
