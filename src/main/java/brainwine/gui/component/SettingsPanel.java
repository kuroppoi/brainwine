package brainwine.gui.component;

import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import com.formdev.flatlaf.extras.components.FlatScrollPane;

import brainwine.gui.GuiPreferences;
import brainwine.gui.theme.Theme;
import brainwine.gui.theme.ThemeManager;
import brainwine.gui.view.MainView;
import brainwine.util.SwingUtils;

@SuppressWarnings("serial")
public class SettingsPanel extends JPanel {
    
    private final MainView mainView;
    private JComboBox<Theme> themeBox;
    private JComboBox<String> tabPlacementBox;
    private JSpinner fontSizeSpinner;
    
    public SettingsPanel(MainView mainView) {
        this.mainView = mainView;
        
        // Save button
        JButton saveButton = new JButton("Save Settings");
        saveButton.addActionListener(event -> saveSettings());
        
        // Reset Button
        JButton resetButton = new JButton("Reset to Defaults");
        resetButton.addActionListener(event -> resetSettings(true));
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
        buttonPanel.add(saveButton);
        buttonPanel.add(resetButton);

        // Main panel
        JPanel settingsPanel = new JPanel(new GridBagLayout());
        settingsPanel.add(createVisualSettingsPanel(), SwingUtils.createConstraints(0, 0));
        settingsPanel.add(buttonPanel, SwingUtils.createConstraints(0, 1));

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
        themeBox.addActionListener(event -> ThemeManager.setTheme((Theme)themeBox.getSelectedItem()));
        
        // Tabbed pane orientation box
        tabPlacementBox = new JComboBox<>(new String[] {"Top", "Left", "Bottom", "Right"});
        tabPlacementBox.setSelectedIndex(mainView.getTabPlacement() - 1);
        tabPlacementBox.addActionListener(event -> mainView.setTabPlacement(tabPlacementBox.getSelectedIndex() + 1));
        
        // Font size changer
        fontSizeSpinner = new JSpinner(new SpinnerNumberModel(SwingUtils.getDefaultFontSize(), 10, 28, 1));
        fontSizeSpinner.addChangeListener(event -> SwingUtils.setDefaultFontSize((int)fontSizeSpinner.getValue()));
        
        // Panel
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel("Theme"), SwingUtils.createConstraints(0, 0));
        panel.add(themeBox, SwingUtils.createConstraints(1, 0));
        panel.add(new JLabel("Tab Placement"), SwingUtils.createConstraints(0, 1));
        panel.add(tabPlacementBox, SwingUtils.createConstraints(1, 1));
        panel.add(new JLabel("Font Size"), SwingUtils.createConstraints(0, 2));
        panel.add(fontSizeSpinner, SwingUtils.createConstraints(1, 2));
        return panel;
    }
    
    private void saveSettings() {
        GuiPreferences.setTheme(((Theme)themeBox.getSelectedItem()).getClassName());
        GuiPreferences.setTabPlacement(tabPlacementBox.getSelectedIndex() + 1);
        GuiPreferences.setFontSize((int)fontSizeSpinner.getValue());
        JOptionPane.showMessageDialog(getRootPane(), "Settings have been saved.");
    }
    
    private void resetSettings(boolean showPrompt) {
        if(!showPrompt || JOptionPane.showConfirmDialog(getRootPane(), 
                "Are you sure you want to reset all settings to their default values?", 
                "Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            GuiPreferences.resetToDefaults();
            
            // Update components
            themeBox.setSelectedItem(ThemeManager.getTheme(GuiPreferences.getTheme()));
            tabPlacementBox.setSelectedIndex(GuiPreferences.getTabPlacement() - 1);
            fontSizeSpinner.setValue(GuiPreferences.getFontSize());
            
            if(showPrompt) {
                JOptionPane.showMessageDialog(getRootPane(), "Settings reset successfully.");
            }
        }
    }
}
