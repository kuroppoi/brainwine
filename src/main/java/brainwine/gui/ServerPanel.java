package brainwine.gui;

import static brainwine.gui.GuiConstants.ERROR_COLOR;
import static brainwine.gui.GuiConstants.INFO_COLOR;
import static brainwine.gui.GuiConstants.WARNING_COLOR;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import org.apache.logging.log4j.Level;

import com.formdev.flatlaf.extras.components.FlatScrollPane;
import com.formdev.flatlaf.extras.components.FlatTextField;

import brainwine.Bootstrap;
import brainwine.ListenableAppender;
import brainwine.gui.event.AutoScrollAdjustmentListener;

@SuppressWarnings("serial")
public class ServerPanel extends JPanel {
    
    private final Bootstrap bootstrap;
    private final JTextPane consoleOutput;
    private final FlatTextField consoleInput;
    private final JButton serverButton;
    
    public ServerPanel(Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(0, 3, 3, 3));
        
        // Console Output
        consoleOutput = new JTextPane() {
            @Override
            public Font getFont() {
                return UIManager.getFont("Brainwine.consoleFont");
            }
        };;
        consoleOutput.setEditable(false);
        ListenableAppender.addListener(message -> {
            Level level = message.getLevel();
            Color color = level == Level.ERROR ? ERROR_COLOR : level == Level.WARN ? WARNING_COLOR : INFO_COLOR;
            appendConsoleOutput(message.getFormattedMessage(), color);
        });
        
        // Scroll Pane
        FlatScrollPane consoleOutputPane = new FlatScrollPane();
        consoleOutputPane.setViewportView(consoleOutput);
        consoleOutputPane.setShowButtons(true);
        consoleOutputPane.setBorder(BorderFactory.createTitledBorder("Console Output"));
        consoleOutputPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        consoleOutputPane.setFocusable(false);
        consoleOutputPane.getVerticalScrollBar().addAdjustmentListener(new AutoScrollAdjustmentListener());
        add(consoleOutputPane);
        
        // Bottom Panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        add(bottomPanel, BorderLayout.PAGE_END);
        
        // Console Input
        consoleInput = new FlatTextField() {
            @Override
            public Font getFont() {
                return UIManager.getFont("Brainwine.consoleFont");
            }
        };
        consoleInput.setPadding(new Insets(0, 4, 0, 0));
        consoleInput.setPlaceholderText("Type 'help' for a list of commands. (Server must be running)");
        consoleInput.setEditable(false);
        consoleInput.setBorder(BorderFactory.createTitledBorder("Console Input"));
        consoleInput.addActionListener(event -> processConsoleInput());
        bottomPanel.add(consoleInput, BorderLayout.CENTER);
        
        // Server Toggle Button
        serverButton = new JButton("Start Server", UIManager.getIcon("Brainwine.powerIcon"));
        serverButton.addActionListener(event -> toggleServer());
        bottomPanel.add(serverButton, BorderLayout.LINE_END);
    }
    
    private void appendConsoleOutput(String text, Color color) {        
        Document document = consoleOutput.getDocument();
        StyleContext context = StyleContext.getDefaultStyleContext();
        AttributeSet attribute = context.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, color);
        
        try {
            document.insertString(document.getLength(), text, attribute);
        } catch(BadLocationException e) {
            e.printStackTrace();
        }
    }
    
    private void processConsoleInput() {
        String commandLine = consoleInput.getText().trim();
        
        if(!commandLine.isEmpty()) {
            appendConsoleOutput(String.format("> %s\n", commandLine), Color.GRAY);
            bootstrap.executeCommand(commandLine);
            consoleInput.setText(null);
        }
    }
    
    private void toggleServer() {
        if(bootstrap.isServerRunning()) {
            serverButton.setEnabled(false);
            consoleInput.setEditable(false);
            consoleInput.setText(null);
            bootstrap.stopServer();
        } else {
            serverButton.setEnabled(false);
            consoleInput.setEditable(true);
            consoleOutput.setText(null);
            bootstrap.startServer();
        }
    }
    
    public void enableServerButton() {
        if(!bootstrap.isServerRunning()) {
            consoleInput.setEditable(false);
            consoleInput.setText(null);
        }
        
        serverButton.setText(bootstrap.isServerRunning() ? "Stop Server" : "Start Server");
        serverButton.setEnabled(true);
    }
}
