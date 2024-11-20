package brainwine.gameserver.quest;

import brainwine.gameserver.util.MapHelper;

import java.util.Map;

public enum RandomQuestDomain {
    ANDROID_SURVIVAL,
    ANDROID_COMBAT,
    ANDROID_COOKING,
    ANDROID_COLLECT,
    DAILY;

    private static Map<String, RandomQuestDomain> prefixToDomain = MapHelper.map(
            String.class, RandomQuestDomain.class,
            "Survive and Thrive", ANDROID_SURVIVAL,
            "The Art of War", ANDROID_COMBAT,
            "Let Them Eat Cake", ANDROID_COOKING,
            "Arts and Crafts", ANDROID_COLLECT
            );

    public static RandomQuestDomain fromCategoryTitle(String category) {
        return prefixToDomain.get(category);
    }
}
