package brainwine.gameserver.achievement;

import brainwine.gameserver.util.LazyGetter;

public class LazyAchievementGetter extends LazyGetter<String, Achievement> {
    
    public LazyAchievementGetter(String in) {
        super(in);
    }
    
    @Override
    public Achievement load() {
        return AchievementManager.getAchievement(in);
    }
}
