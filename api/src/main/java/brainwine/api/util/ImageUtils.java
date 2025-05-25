package brainwine.api.util;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class ImageUtils {
    
    /**
     * Creates an image with a pixel count as close to the desired pixel count as possible
     * while also retaining the same aspect ratio.
     */
    public static BufferedImage createImage(int pixelCount, double scaleX, double scaleY) {
        double factor = Math.sqrt(scaleX * scaleY / pixelCount);
        int width = (int)Math.round(scaleX / factor);
        int height = (int)Math.round(scaleY / factor);
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }
    
    /**
     * Fast image copying function.
     */
    public static BufferedImage copyImage(BufferedImage image) {
        if(image.getType() != BufferedImage.TYPE_INT_ARGB) {
            throw new IllegalArgumentException("Image type must be TYPE_INT_ARGB");
        }
        
        BufferedImage copy = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        int[] src = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
        int[] dst = ((DataBufferInt)copy.getRaster().getDataBuffer()).getData();
        System.arraycopy(src, 0, dst, 0, dst.length);
        return copy;
    }
}
