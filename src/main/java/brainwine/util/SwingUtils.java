package brainwine.util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.components.FlatTextField;

public class SwingUtils {
    
    @SuppressWarnings("serial")
    public static Action createAction(String name, Icon icon, Runnable handler) {
        AbstractAction action = new AbstractAction(name, icon) {
            @Override
            public void actionPerformed(ActionEvent event) {
                handler.run();
            }
        };
        
        if(icon != null) {
            action.putValue(Action.SHORT_DESCRIPTION, name);
        }
        
        return action;
    }
    
    public static Action createAction(String name, Runnable handler) {
        return createAction(name, null, handler);
    }
    
    public static GridBagConstraints createConstraints(int x, int y) {
        return createConstraints(x, y, 1, 1);
    }
    
    public static GridBagConstraints createConstraints(int x, int y, int width, int height) {
        return createConstraints(x, y, width, height, 1, 1);
    }
    
    public static GridBagConstraints createConstraints(int x, int y, int width, int height, double weightX, double weightY) {
        return createConstraints(x, y, width, height, weightX, weightY, 8, 8);
    }
    
    public static GridBagConstraints createConstraints(int x, int y, int width, int height, double weightX, double weightY, 
            int paddingX, int paddingY) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.gridwidth = width;
        constraints.gridheight = height;
        constraints.weightx = weightX;
        constraints.weighty = weightY;
        constraints.ipadx = paddingX;
        constraints.ipady = paddingY;
        return constraints;
    }
    
    public static String getTextFieldValue(FlatTextField textField) {
        return textField.getText().isEmpty() ? textField.getPlaceholderText() : textField.getText();
    }
    
    public static void setDefaultFontSize(int size) {
        UIManager.put("defaultFont", UIManager.getFont("defaultFont").deriveFont((float)size));
        UIManager.put("Brainwine.consoleFont", UIManager.getFont("Brainwine.consoleFont").deriveFont((float)size));
        FlatLaf.updateUI();
    }
    
    public static int getDefaultFontSize() {
        return UIManager.getFont("defaultFont").getSize();
    }
    
    public static void showExceptionInfo(Component parent, String message, Throwable throwable) {
        // Create stacktrace string
        StringWriter writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));
        
        // Create text area
        JTextArea area = new JTextArea(writer.toString());
        area.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 0));
        area.setFont(UIManager.getFont("Brainwine.consoleFont"));
        area.setEditable(false);
        
        // Create scroll pane
        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize(new Dimension(600, 200));
        scrollPane.setMaximumSize(scrollPane.getPreferredSize());
        
        // Create dialog
        String label = String.format("<html><b>%s</b><br>Exception details:<br><br></html>", message);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(label), BorderLayout.PAGE_START);
        panel.add(scrollPane);
        JOptionPane.showMessageDialog(parent, panel, "An error has occured", JOptionPane.ERROR_MESSAGE);
    }
}
