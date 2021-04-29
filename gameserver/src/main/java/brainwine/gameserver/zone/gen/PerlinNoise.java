package brainwine.gameserver.zone.gen;

/**
 * Borrowed from {@link https://github.com/raxod502/TerrariaClone/blob/master/src/PerlinNoise.java}
 * and slightly altered to support seeding.
 */
public class PerlinNoise {
    
    private final long seed;
    
    public PerlinNoise(long seed) {
        this.seed = seed;
    }
    
    public double perlinNoise(double x, double p, int n) {
        double total = 0;
        double frequency;
        double amplitude;
        
        for(int i = 0; i < n + 1; i++) {
            frequency = Math.pow(2, i);
            amplitude = Math.pow(p, i);
            total = total + interpolateNoise(x * frequency) * amplitude;
        }
        
        return total;
    }

    private double interpolateNoise(double x) {
        int ix = (int)x;
        double fx = x - ix;
        double v1 = smoothNoise(ix);
        double v2 = smoothNoise(ix + 1);
        return interpolate(v1, v2, fx);
    }

    private double smoothNoise(int x) {
        return noise(x) / 2 + noise(x - 1) / 4 + noise(x + 1) /4;
    }
    
    private double noise(int x) {
        x = (int)(seed ^ x);
        return (1.0 - ((x * (x * x * 15731 + 789221) + 1376312589) & 0x7fffffff) / 1073741824.0);
    }
    
    private double interpolate(double a, double b, double x) {
        double ft = x * Math.PI;
        double f = (1 - Math.cos(ft)) / 2;
        return a * (1 - f) + b * f;
    }
}
