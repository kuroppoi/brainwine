package brainwine.gui;

import java.awt.Color;

import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public class GuiConstants {
    
    public static final String GITHUB_REPOSITORY_URL = "https://github.com/kuroppoi/brainwine";
    public static final String STEAM_REGISTRY_LOCATION = "HKCU\\SOFTWARE\\Valve\\Steam";
    public static final String DEEPWORLD_PLAYERPREFS = "HKCU\\SOFTWARE\\Bytebin LLC\\Deepworld";
    public static final String DEEPWORLD_STEAM_ID = "340810";
    public static final String DEEPWORLD_ASSEMBLY_PATH = "/steamapps/common/Deepworld/Deepworld_Data/Managed/Assembly-CSharp.dll";
    public static final String RUN_GAME_URL = String.format("steam://rungameid/%s", DEEPWORLD_STEAM_ID);
    public static final String STORE_PAGE_URL = String.format("steam://store/%s", DEEPWORLD_STEAM_ID);
    public static final String COMMUNITY_HUB_URL = String.format("steam://url/GameHub/%s", DEEPWORLD_STEAM_ID);
    public static final Color ERROR_COLOR = Color.RED.darker();
    public static final Color WARNING_COLOR = Color.YELLOW.darker();
    public static final Color INFO_COLOR = Color.WHITE.darker();
    public static final Marker GUI_MARKER = MarkerManager.getMarker("gui");
}
