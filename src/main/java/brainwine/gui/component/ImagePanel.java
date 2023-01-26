package brainwine.gui.component;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

import javax.swing.JPanel;

/**
 * A {@link JPanel} with a scaling and extending background image that always maintains its aspect ratio.
 */
@SuppressWarnings("serial")
public class ImagePanel extends JPanel {
    
    private Image image;
    
    public ImagePanel() {
        super();
    }
    
    public ImagePanel(Image image) {
        this.image = image;
    }
    
    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        
        if(image == null) {
            return;
        }
        
        // Can this be simpler?
        // Probably. Who knows. Don't really care.
        int containerWidth = getWidth();
        int containerHeight = getHeight();
        int drawWidth = containerWidth;
        int drawHeight = containerHeight;
        float targetRatio = image.getWidth(this) / (float)image.getHeight(this);
        float aspectRatio = drawWidth / (float)drawHeight;
        float inverseTargetRatio = 1.0F / targetRatio;
        
        if(aspectRatio > targetRatio) {
            drawWidth = Math.round(drawHeight * targetRatio);
        } else if(aspectRatio < targetRatio) {
            drawHeight = Math.round(drawWidth * inverseTargetRatio);
        }
        
        if(drawWidth < containerWidth) {
            drawHeight += (containerWidth - drawWidth) / targetRatio;
            drawWidth = containerWidth;
        } else if(drawHeight < containerHeight) {
            drawWidth += (containerHeight - drawHeight) / inverseTargetRatio;
            drawHeight = containerHeight;
        }
        
        int x = (containerWidth - drawWidth) / 2;
        int y = (containerHeight - drawHeight) / 2;
        Graphics2D g2d = (Graphics2D)graphics;
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(image, x, y, drawWidth, drawHeight, this);
    }
    
    public void setImage(Image image) {
        this.image = image;
    }
    
    public Image getImage() {
        return image;
    }
}
