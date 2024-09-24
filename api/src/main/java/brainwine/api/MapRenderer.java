package brainwine.api;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import brainwine.api.models.ZoneInfo;
import brainwine.api.util.ImageUtils;

/**
 * Functions for rendering zone maps.
 */
public class MapRenderer {
     
    private static final Logger logger = LogManager.getLogger();
    private static final double[] depths = { 0.03, 0.05, 0.08, 0.12, 0.17, 0.26, 0.3 };
    private static final Map<String, int[]> colorMap = new HashMap<>();
    private static final BufferedImage mapCrossImage;
    
    static {
        // Set color map data
        colorMap.put("plain", new int[] { 0xFFFFFF, 0x5DB830, 0x417431, 0x414E1C, 0x4E441C, 0x2A240C, 0x18150B });
        colorMap.put("arctic", new int[] { 0xFFFFFF, 0x75A49E, 0x456B74, 0x33535F, 0x2D4F5D, 0x142E3F, 0x0B1A25 });
        colorMap.put("hell", new int[] { 0xFFFFFF, 0xBE8D6F, 0x905548, 0x7F3C32, 0x6F3932, 0x5F1814, 0x380E0D });
        colorMap.put("brain", new int[] { 0xFFFFFF, 0xA19599, 0x705C6E, 0x5D4257, 0x4E3E55, 0x3B1C36, 0x2A0B28 });
        colorMap.put("desert", new int[] { 0xECDE93, 0xB18E58, 0x7B5822, 0x614312, 0x4E350C, 0x322209, 0x1E1506 });
        colorMap.put("space", new int[] { 0xFFFFFF, 0xEEEEEE, 0xDDDDDD, 0xCCCCCC, 0xBBBBBB, 0xAAAAAA, 0x999999 });
        
        // Load image resources
        mapCrossImage = loadImageResource("/map/crossMark.png");
    }
    
    private static BufferedImage loadImageResource(String name) {
        try(InputStream inputStream = MapRenderer.class.getResourceAsStream(name)) {
            return ImageIO.read(inputStream);
        } catch(Exception e) {
            logger.error("Failed to load image resource '{}'", name, e);
        }
        
        return null;
    }
    
    /**
     * Creates a surface map render that is visually almost identical to V2 client map renders.
     */
    public static BufferedImage drawSurfaceMap(ZoneInfo zone) {
        BufferedImage image = ImageUtils.createImage(300000, zone.getWidth(), zone.getHeight());
        Graphics2D g2d = image.createGraphics();
        int[] surfaceArray = zone.getSurface();
        int[] colors = colorMap.getOrDefault(zone.getBiome(), colorMap.get("plain"));
        int[] raster = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
        int surfaceMin = zone.getHeight();
        int surfaceMax = 0;
        
        // Find highest & lowest surface points
        for(int surface : surfaceArray) {
            surfaceMin = Math.min(surfaceMin, surface);
            surfaceMax = Math.max(surfaceMax, surface);
        }
        
        int surfaceCenter = (int)Math.floor((surfaceMax - surfaceMin) * 0.5 + surfaceMin);
        double scaleX = (double)image.getWidth() / zone.getWidth();
        double scaleY = (double)image.getHeight() / zone.getHeight();
        
        // Fill raster with background color fast
        int length = (int)(surfaceMax * scaleY) * image.getWidth();
        raster[0] = 0x80808080;
        
        for(int i = 1; i < length; i += i) {
            System.arraycopy(raster, 0, raster, i, length - i < i ? length - i : i);
        }
        
        // Draw zone surface
        // TODO potentially slow (largely because of Java2D) and can probably be optimized further
        for(int i = 0; i < image.getWidth(); i++) {            
            int surface = surfaceArray[(int)(i / scaleX)];
            int distanceToPeak = surface - surfaceMin;
            double y = (zone.getHeight() - surface) * scaleY;
            double layerHeight = 1.0;
            int start = image.getHeight() - (int)(y * layerHeight);
            
            for(int j = 0; j < depths.length; j++) {
                double scale = (double)distanceToPeak / zone.getHeight() / depths.length * (j < 2 ? 2.0 : 0.5);
                layerHeight -= (depths[j] - scale);
                int end = j + 1 >= depths.length ? image.getHeight() : image.getHeight() - (int)(y * layerHeight);
                g2d.setColor(new Color(colors[j > 0 || surface < surfaceCenter ? j : 1])); // Only use snow color if surface is above surface center
                g2d.fillRect(i, start, 1, end - start);
                start = end;
            }    
        }
        
        g2d.dispose();
        return image;
    }
    
    /**
     * Draws a red cross mark on an image at the given zone coordinates.
     */
    public static void drawCrossMark(ZoneInfo zone, int x, int y, BufferedImage mapImage) {
        double scaleX = (double)mapImage.getWidth() / zone.getWidth();
        double scaleY = (double)mapImage.getHeight() / zone.getHeight();
        Graphics2D g2d = mapImage.createGraphics();
        g2d.drawImage(mapCrossImage, (int)(x * scaleX) - 24, (int)(y * scaleY) - 25, 50, 60, null);
        g2d.dispose();
    }
}
