package brainwine.gui.event;

import java.awt.Adjustable;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

public class AutoScrollAdjustmentListener implements AdjustmentListener {
    
    private int previousMax = -1;
    
    @Override
    public void adjustmentValueChanged(AdjustmentEvent event) {
        Adjustable adjustable = event.getAdjustable();
        int max = adjustable.getMaximum();
        
        if(previousMax == -1 || previousMax == adjustable.getValue() + adjustable.getVisibleAmount()) {
            adjustable.setValue(max);
        }
        
        previousMax = max;
    }
}
