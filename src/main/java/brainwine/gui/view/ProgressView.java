package brainwine.gui.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class ProgressView {
    
    private final List<Runnable> closeListeners = new ArrayList<>();
    private final JDialog dialog;
    private final JLabel label;
    private final JProgressBar progressBar;
    
    public ProgressView(JComponent owner, String text) {
        // Create progress bar
        progressBar = new JProgressBar();
        progressBar.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        progressBar.setMinimumSize(new Dimension(300, 40));
        progressBar.setPreferredSize(progressBar.getMinimumSize());
        progressBar.setStringPainted(true);
        
        // Create main panel
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.add(label = new JLabel(text), BorderLayout.PAGE_START);
        panel.add(progressBar, BorderLayout.CENTER);
        
        // Create dialog
        JFrame frame = (JFrame)owner.getTopLevelAncestor();
        dialog = new JDialog(frame, "Please wait...");
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                closeListeners.forEach(Runnable::run);
            }
        });
        dialog.setResizable(false);
        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }
    
    public void addCloseListener(Runnable listener) {
        closeListeners.add(listener);
    }
    
    public void setText(String text) {
        label.setText(text);
    }
    
    public void setProgress(int progress) {
        progressBar.setValue(progress);
    }
    
    public void dispose() {
        dialog.dispose();
    }
}
