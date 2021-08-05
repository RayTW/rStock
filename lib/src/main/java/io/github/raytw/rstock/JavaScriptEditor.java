package io.github.raytw.rstock;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Optional;
import java.util.function.Function;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

/**
 * Simple java script editor.
 *
 * @author Ray Li
 */
public class JavaScriptEditor extends JDialog implements ActionListener {
  private static final long serialVersionUID = 4382200682104638340L;
  private JTextArea scriptEditor;
  private JTextPane console;
  private Optional<Function<String, Boolean>> listener;

  /**
   * Initialize.
   *
   * @param frame frame
   * @param type type
   */
  public JavaScriptEditor(Frame frame, ModalityType type) {
    super(frame, type);
    Container pane = getContentPane();

    pane.setLayout(new BorderLayout());

    EmptyBorder eb = new EmptyBorder(new Insets(10, 10, 10, 10));
    console = new JTextPane();
    console.setBorder(eb);
    console.setMargin(new Insets(5, 5, 5, 5));

    scriptEditor = new JTextArea();
    JScrollPane sp = new JScrollPane(scriptEditor);
    JPanel centerPanel = new JPanel(new BorderLayout());

    centerPanel.add(sp, BorderLayout.CENTER);
    centerPanel.add(new JScrollPane(console), BorderLayout.SOUTH);
    pane.add(centerPanel, BorderLayout.CENTER);

    JButton close = new JButton("Close");
    JButton apply = new JButton("Apply And Close");
    JPanel buttonPanel = new JPanel(new GridLayout(1, 2));

    buttonPanel.add(close);
    buttonPanel.add(apply);
    pane.add(buttonPanel, BorderLayout.SOUTH);

    setLocation(frame.getLocation());
    setSize(frame.getWidth(), frame.getHeight());

    close.addActionListener(event -> this.dispose());

    apply.addActionListener(
        event ->
            listener.ifPresent(
                f -> {
                  if (f.apply(scriptEditor.getText())) {
                    this.dispose();
                  }
                }));

    // TODO evaluate whether to add UndoManager features.
    // https://docs.oracle.com/javase/8/docs/api/javax/swing/undo/UndoManager.html
  }

  public void setApplyAndCloseListener(Function<String, Boolean> listener) {
    this.listener = Optional.ofNullable(listener);
  }

  @Override
  public void actionPerformed(ActionEvent arg0) {
    dispose();
  }

  public void setJavaScript(String javaScript) {
    scriptEditor.setText(javaScript);
  }

  public void setConsole(String message) {
    appendToPane(console, message, Color.RED);
    console.setText(message);
  }

  private void appendToPane(JTextPane pane, String message, Color color) {
    StyleContext sc = StyleContext.getDefaultStyleContext();
    AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, color);

    aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
    aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

    int len = pane.getDocument().getLength();
    pane.setCaretPosition(len);
    pane.setCharacterAttributes(aset, false);
    pane.replaceSelection(message);
  }
}
