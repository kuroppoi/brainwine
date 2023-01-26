package brainwine.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.prefs.BackingStoreException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;

import com.formdev.flatlaf.extras.components.FlatScrollPane;
import com.formdev.flatlaf.extras.components.FlatTextField;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialDarkerIJTheme;

import brainwine.gui.event.DocumentChangeListener;
import brainwine.gui.theme.Theme;
import brainwine.gui.theme.ThemeManager;
import brainwine.util.OperatingSystem;
import brainwine.util.SwingUtils;

@SuppressWarnings("serial")
public class SettingsPanel extends JPanel {
    
    private JComboBox<Theme> themeBox;
    private JSpinner fontSizeSpinner;
    private JCheckBox embedMenuBarCheckox;
    private FlatTextField gatewayHostField;
    private FlatTextField apiHostField;
    
    public SettingsPanel() {
        // Reset Button
        JButton resetButton = new JButton("Reset to Defaults");
        resetButton.addActionListener(event -> resetSettings(true));
        
        // Clear Button
        JButton clearButton = new JButton("Clear Settings");
        clearButton.addActionListener(event -> clearSettings());
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
        buttonPanel.add(resetButton);
        buttonPanel.add(clearButton);

        // Main panel
        JPanel settingsPanel = new JPanel(new GridBagLayout());
        settingsPanel.add(createVisualSettingsPanel(), SwingUtils.createConstraints(0, 0));
        
        if(OperatingSystem.isWindows()) {
            settingsPanel.add(createGameSettingsPanel(), SwingUtils.createConstraints(0, 1));
        }
        
        settingsPanel.add(new JSeparator(), SwingUtils.createConstraints(0, 2));
        settingsPanel.add(buttonPanel, SwingUtils.createConstraints(0, 3));

        // Scroll pane (TODO doesn't actually scroll)
        FlatScrollPane scrollPane = new FlatScrollPane();
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setViewportView(settingsPanel);
        scrollPane.setShowButtons(true);
        scrollPane.setFocusable(false);
        add(scrollPane);
    }
    
    private JPanel createVisualSettingsPanel() {
        // Theme selector box
        themeBox = new JComboBox<>();
        ThemeManager.getThemes().forEach(themeBox::addItem);
        themeBox.setSelectedItem(ThemeManager.getCurrentTheme());
        themeBox.addItemListener(item -> SwingUtilities.invokeLater(() -> ThemeManager.setTheme((Theme)item.getItem())));
        
        // Font size changer
        fontSizeSpinner = new JSpinner(new SpinnerNumberModel(SwingUtils.getDefaultFontSize(), 10, 28, 1));
        fontSizeSpinner.addChangeListener(event -> SwingUtils.setDefaultFontSize((int)fontSizeSpinner.getValue()));
        
        // Menu bar embed checkbox
        embedMenuBarCheckox = new JCheckBox();
        embedMenuBarCheckox.setSelected(SwingUtils.isMenuBarEmbedded());
        embedMenuBarCheckox.addChangeListener(event -> SwingUtils.setMenuBarEmbedded(embedMenuBarCheckox.isSelected()));
        
        // Panel
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(createCategoryBorder("Visual Settings"));
        panel.add(new JLabel("Theme"), SwingUtils.createConstraints(0, 0));
        panel.add(themeBox, SwingUtils.createConstraints(1, 0));
        panel.add(new JLabel("Font Size"), SwingUtils.createConstraints(0, 1));
        panel.add(fontSizeSpinner, SwingUtils.createConstraints(1, 1));
        panel.add(new JLabel("Embed Menu Bar"), SwingUtils.createConstraints(0, 2));
        panel.add(embedMenuBarCheckox, SwingUtils.createConstraints(1, 2));
        return panel;
    }
    
    private JPanel createGameSettingsPanel() {
        // Gateway host field
        gatewayHostField = new FlatTextField() {
            @Override
            public Dimension getPreferredSize() {
                return themeBox.getPreferredSize();
            }
        };
        gatewayHostField.setText(GuiPreferences.getString(GuiPreferences.GATEWAY_HOST_KEY, "local"));
        gatewayHostField.setPlaceholderText("127.0.0.1:5001");
        gatewayHostField.getDocument().addDocumentListener(new DocumentChangeListener() {
            @Override
            public void changedUpdate(DocumentEvent event) {
                GuiPreferences.setString(GuiPreferences.GATEWAY_HOST_KEY, gatewayHostField.getText());
            }
        });
        
        // API host field
        apiHostField = new FlatTextField() {
            @Override
            public Dimension getPreferredSize() {
                return themeBox.getPreferredSize();
            }
        };
        apiHostField.setText(GuiPreferences.getString(GuiPreferences.API_HOST_KEY, "local"));
        apiHostField.setPlaceholderText("127.0.0.1:5003");
        apiHostField.getDocument().addDocumentListener(new DocumentChangeListener() {
            @Override
            public void changedUpdate(DocumentEvent event) {
                GuiPreferences.setString(GuiPreferences.API_HOST_KEY, apiHostField.getText());
            }
        });
        
        // Panel
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(createCategoryBorder("Game Settings"));
        panel.add(new JLabel("Gateway Host"), SwingUtils.createConstraints(0, 0));
        panel.add(gatewayHostField, SwingUtils.createConstraints(1, 0, 1, 1, 0, 0));
        panel.add(new JLabel("API Host"), SwingUtils.createConstraints(0, 1));
        panel.add(apiHostField, SwingUtils.createConstraints(1, 1, 1, 1, 0, 0));
        return panel;
    }
    
    public void focusHostSettings() {
        gatewayHostField.requestFocus();
    }
    
    private void resetSettings(boolean showPrompt) {
        if(!showPrompt || JOptionPane.showConfirmDialog(getRootPane(), 
                "Are you sure you want to reset all settings to their default values?", 
                "Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            themeBox.setSelectedItem(ThemeManager.getTheme(FlatMaterialDarkerIJTheme.class));
            fontSizeSpinner.setValue(14);
            embedMenuBarCheckox.setSelected(true);
            
            if(OperatingSystem.isWindows()) {
                gatewayHostField.setText("local");
                apiHostField.setText("local");
            }
            
            if(showPrompt) {
                JOptionPane.showMessageDialog(getRootPane(), "Settings reset successfully.");
            }
        }
    }
    
    private void clearSettings() {        
        if(JOptionPane.showConfirmDialog(getRootPane(), 
                "This will reset all settings to their default values and remove them from this user's preferences"
                + " until you change them again or restart the application. Are you sure you want to continue?", 
                "Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            resetSettings(false);
            
            // Invoke later because settings are not reset immediately.
            SwingUtilities.invokeLater(() -> {
                try {
                    GuiPreferences.clear();
                    JOptionPane.showMessageDialog(getRootPane(), "Settings cleared successfully.");
                } catch(BackingStoreException e) {
                    JOptionPane.showMessageDialog(getRootPane(), "Could not clear preferences: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }
    }
    
    private Border createCategoryBorder(String category) {
        return BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY), category, TitledBorder.CENTER, TitledBorder.CENTER);
    }
}
