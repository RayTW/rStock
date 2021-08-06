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
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
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
  private JList<String> notifyPeriodList;
  private Optional<Function<JavaScriptEditor, Boolean>> listener;
  private String tickerSymbol;

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

    notifyPeriodList = new JList<>(new String[] {"NONE", "1 HOUR 1 TIMES", "1 DAY 1 TIMES"});
    notifyPeriodList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    notifyPeriodList.setLayoutOrientation(JList.VERTICAL);
    notifyPeriodList.setVisibleRowCount(-1);
    JScrollPane listScroller =
        new JScrollPane(
            notifyPeriodList,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    JButton close = new JButton("Close");
    JButton apply = new JButton("Apply And Close");
    JPanel buttonPanel = new JPanel(new GridLayout(2, 2));

    buttonPanel.add(new JLabel("通知頻率", SwingConstants.CENTER));
    buttonPanel.add(listScroller);
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
                  if (f.apply(JavaScriptEditor.this)) {
                    this.dispose();
                  }
                }));

    // TODO evaluate whether to add UndoManager features.
    // https://docs.oracle.com/javase/8/docs/api/javax/swing/undo/UndoManager.html
  }

  public String getTickerSymbol() {
    return tickerSymbol;
  }

  public String getJavaScript() {
    return scriptEditor.getText();
  }

  public String getNotifyPeriodSelectedValue() {
    return notifyPeriodList.getSelectedValue();
  }

  public void setApplyAndCloseListener(Function<JavaScriptEditor, Boolean> listener) {
    this.listener = Optional.ofNullable(listener);
  }

  @Override
  public void actionPerformed(ActionEvent arg0) {
    dispose();
  }

  /** Reset layout states. */
  public void reset() {
    setConsole("");
    notifyPeriodList.setSelectedIndex(0);
    notifyPeriodList.ensureIndexIsVisible(0);
  }

  /**
   * Sets layout states.
   *
   * @param tickerSymbol tickerSymbol
   * @param javaScript javaScript
   * @param notifyPeroid notifyPeroid
   */
  public void setVerifyTicker(String tickerSymbol, String javaScript, String notifyPeroid) {
    this.tickerSymbol = tickerSymbol;
    scriptEditor.setText(javaScript);

    if (notifyPeroid == null) {
      return;
    }
    for (int i = 0; i < notifyPeriodList.getSelectedValuesList().size(); i++) {
      if (notifyPeriodList.getSelectedValuesList().get(i).equals(notifyPeroid)) {
        notifyPeriodList.setSelectedIndex(i);
        break;
      }
    }
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
