package brainwine.gameserver.zone;

import brainwine.gameserver.util.MathUtils;
import io.netty.util.internal.ThreadLocalRandom;

/**
 * TODO save weather in zone config
 */
public class WeatherManager {
    
    private static final ThreadLocalRandom random = ThreadLocalRandom.current();
    private long rainStart;
    private long rainDuration;
    private float rainPower;
    private float precipitation;
    
    public WeatherManager() {
        createRandomRain(random.nextBoolean());
    }
    
    public void tick(float deltaTime) {
        long now = System.currentTimeMillis();
        
        if(now > rainStart + rainDuration) {
            createRandomRain(rainPower > 0 ? true : false);
        }
        
        float lerp = (float)(deltaTime * MathUtils.lerp(0.02F, 0.1F, (now - rainStart) / (float)rainDuration));
        precipitation = (float)MathUtils.lerp(precipitation, rainPower, lerp);
    }
    
    public void setRain(float power, long duration) {
        rainStart = System.currentTimeMillis();
        rainPower = power;
        rainDuration = duration;
    }
    
    public void createRandomRain(boolean dry) {
        rainStart = System.currentTimeMillis();
        
        if(dry) {
            rainDuration = (long)(random.nextDouble(12, 17) * 60000);
            rainPower = 0;
        } else {
            rainDuration = (long)(random.nextDouble(2.5, 4) * 60000);
            rainPower = (float)random.nextDouble(0.33, 1.0);
        }
    }
    
    public boolean isRaining() {
        return rainPower > 0;
    }
    
    public long getRainStart() {
        return rainStart;
    }
    
    public long getRainDuration() {
        return rainDuration;
    }
    
    public float getRainPower() {
        return rainPower;
    }
    
    public float getPrecipitation() {
        return precipitation;
    }
}
