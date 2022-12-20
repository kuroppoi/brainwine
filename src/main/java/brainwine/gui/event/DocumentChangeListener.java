package brainwine.gui.event;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public abstract class DocumentChangeListener implements DocumentListener {

    @Override
    public final void insertUpdate(DocumentEvent event) {
        changedUpdate(event);
    }

    @Override
    public final void removeUpdate(DocumentEvent event) {
        changedUpdate(event);
    }

    @Override
    public abstract void changedUpdate(DocumentEvent event);
}
