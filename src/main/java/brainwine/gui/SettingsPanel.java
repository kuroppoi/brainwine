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
    
    private final MainView mainView;
    private JComboBox<Theme> themeBox;
    private JComboBox<String> tabPlacementBox;
    private JSpinner fontSizeSpinner;
    private JCheckBox embedMenuBarCheckbox;
    private FlatTextField gatewayHostField;
    private FlatTextField apiHostField;
    
    public SettingsPanel(MainView mainView) {
        this.mainView = mainView;
        
        // Reset Button
        JButton resetButton = new JButton("Reset to Defaults");
        resetButton.addActionListener(event -> resetSettings(true));
        
        // Clear Button
        JButton clearButton = new JButton("Clear Settings");
        clearButton.addActionListener(event -> clearSettings());
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
        buttonPanel.setBorder(createCategoryBorder("Reset Settings"));
        buttonPanel.add(resetButton);
        buttonPanel.add(clearButton);

        // Main panel
        JPanel settingsPanel = new JPanel(new GridBagLayout());
        settingsPanel.add(createVisualSettingsPanel(), SwingUtils.createConstraints(0, 0));
        
        if(OperatingSystem.isWindows()) {
            settingsPanel.add(createGameSettingsPanel(), SwingUtils.createConstraints(0, 1));
        }
        
        settingsPanel.add(buttonPanel, SwingUtils.createConstraints(0, 2));

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
        themeBox.addActionListener(event -> setThemePreference((Theme)themeBox.getSelectedItem()));
        
        // Tabbed pane orientation box
        tabPlacementBox = new JComboBox<>(new String[] {"Top", "Left", "Bottom", "Right"});
        tabPlacementBox.setSelectedIndex(mainView.getTabPlacement() - 1);
        tabPlacementBox.addActionListener(event -> setTabPlacementPreference(tabPlacementBox.getSelectedIndex() + 1));
        
        // Font size changer
        fontSizeSpinner = new JSpinner(new SpinnerNumberModel(SwingUtils.getDefaultFontSize(), 10, 28, 1));
        fontSizeSpinner.addChangeListener(event -> setFontSizePreference((int)fontSizeSpinner.getValue()));

        // Menu bar embed checkbox
        embedMenuBarCheckbox = new JCheckBox();
        embedMenuBarCheckbox.setSelected(SwingUtils.isMenuBarEmbedded());
        embedMenuBarCheckbox.addChangeListener(event -> setEmbedMenuBarPreference(embedMenuBarCheckbox.isSelected()));
        
        // Panel
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(createCategoryBorder("Visual Settings"));
        panel.add(new JLabel("Theme"), SwingUtils.createConstraints(0, 0));
        panel.add(themeBox, SwingUtils.createConstraints(1, 0));
        panel.add(new JLabel("Tab Placement"), SwingUtils.createConstraints(0, 1));
        panel.add(tabPlacementBox, SwingUtils.createConstraints(1, 1));
        panel.add(new JLabel("Font Size"), SwingUtils.createConstraints(0, 2));
        panel.add(fontSizeSpinner, SwingUtils.createConstraints(1, 2));
        panel.add(new JLabel("Embed Menu Bar"), SwingUtils.createConstraints(0, 3));
        panel.add(embedMenuBarCheckbox, SwingUtils.createConstraints(1, 3));
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
    
    private void setThemePreference(Theme theme) {
        SwingUtilities.invokeLater(() -> {
            ThemeManager.setTheme(theme);
        });

        GuiPreferences.setString(GuiPreferences.THEME_KEY, theme.getClassName());
    }
    
    private void setTabPlacementPreference(int tabPlacement) {
        SwingUtilities.invokeLater(() -> {
            mainView.setTabPlacement(tabPlacement);
        });
        
        GuiPreferences.setInt(GuiPreferences.TAB_PLACEMENT_KEY, tabPlacement);
    }
    
    private void setFontSizePreference(int fontSize) {
        SwingUtils.setDefaultFontSize(fontSize);
        GuiPreferences.setInt(GuiPreferences.FONT_SIZE_KEY, fontSize);
    }
    
    private void setEmbedMenuBarPreference(boolean embedMenuBar) {
        SwingUtils.setMenuBarEmbedded(embedMenuBar);
        GuiPreferences.setBoolean(GuiPreferences.EMBED_MENU_BAR_KEY, embedMenuBar);
    }
    
    private void resetSettings(boolean showPrompt) {
        if(!showPrompt || JOptionPane.showConfirmDialog(getRootPane(), 
                "Are you sure you want to reset all settings to their default values?", 
                "Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            themeBox.setSelectedItem(ThemeManager.getTheme(FlatMaterialDarkerIJTheme.class));
            tabPlacementBox.setSelectedIndex(0);
            fontSizeSpinner.setValue(15);
            embedMenuBarCheckbox.setSelected(true);
            
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
