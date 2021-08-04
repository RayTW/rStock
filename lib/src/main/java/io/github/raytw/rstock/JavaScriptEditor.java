package io.github.raytw.rstock;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Simple editor.
 *
 * @author Ray Li
 */
public class JavaScriptEditor {
  private StrategyJavaScript<Ticker> strategy;
  private HashMap<String, String> cache;

  public JavaScriptEditor() {
    strategy = new StrategyJavaScript<>();
    cache = new HashMap<>();
  }

  /**
   * verify.
   *
   * @param ticker ticker detail
   */
  public void verify(Ticker ticker) {
    // TODO show a component of the text area that strategy of choice stock for the
    // user to write java script.
    System.out.println("ticker=" + ticker);

    String js = cache.get(ticker.getSymbol());

    if (js == null) {
      try {
        js = new String(Files.readAllBytes(Paths.get("test.js")));
      } catch (IOException e) {
        e.printStackTrace();
        return;
      }
    }

    JTextArea ta = new JTextArea(20, 40);
    JScrollPane sp = new JScrollPane(ta);

    ta.setText(js);

    JComponent[] inputs = new JComponent[] {sp};
    int result =
        JOptionPane.showConfirmDialog(null, inputs, "Editor", JOptionPane.OK_CANCEL_OPTION);
    if (result == JOptionPane.OK_OPTION) {
      String javaScript = ta.getText().toString();

      try {
        strategy.enableNotification(javaScript, ticker);
        cache.put(ticker.getSymbol(), javaScript);
      } catch (NotificationException e) {
        e.printStackTrace();
        ta.setText(e.getMessage());
        JOptionPane.showConfirmDialog(null, inputs, "Error", JOptionPane.CLOSED_OPTION);
      }
    } else {
      System.out.println("User canceled / closed the dialog, result = " + result);
    }
  }
}
