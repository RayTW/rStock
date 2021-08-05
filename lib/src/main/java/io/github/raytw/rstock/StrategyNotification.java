package io.github.raytw.rstock;

import dorkbox.notify.Notify;
import dorkbox.notify.Pos;
import java.awt.Dialog.ModalityType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import javax.swing.JFrame;

/**
 * Simple java script editor with timer trigger notification.
 *
 * @author Ray Li
 */
public class StrategyNotification {
  private StrategyJavaScript<Ticker> strategy;
  private HashMap<String, String> cache;

  public StrategyNotification() {
    strategy = new StrategyJavaScript<>();
    cache = new HashMap<>();
  }

  /**
   * verify.
   *
   * @param frame frame
   * @param ticker ticker detail
   */
  public void verify(JFrame frame, Ticker ticker) {
    // TODO show a component of the text area that strategy of choice stock for the
    // user to write java script.
    System.out.println("ticker=" + ticker);

    String js = cache.get(ticker.getSymbol());

    if (js == null) {
      try {
        js = new String(Files.readAllBytes(Paths.get("strategy.js")));
      } catch (IOException e) {
        e.printStackTrace();
        return;
      }
    }

    JavaScriptEditor editor = new JavaScriptEditor(frame, ModalityType.APPLICATION_MODAL);

    editor.setJavaScript(js);
    editor.setApplyAndCloseListener(
        javaScript -> {
          try {
            boolean enable = strategy.enableNotification(javaScript, ticker);
            cache.put(ticker.getSymbol(), javaScript);

            if (enable) {
              // TODO show notification
              Notify.create()
                  .position(Pos.TOP_RIGHT)
                  .title("Notification")
                  .text("Take order,id" + ticker.getId() + ",price:" + ticker.getPrice())
                  .darkStyle()
                  .showConfirm();
            }
            return Boolean.TRUE;
          } catch (NotificationException e) {
            editor.setConsole(e.getMessage());
          }
          return Boolean.FALSE;
        });

    editor.setVisible(true);
  }
}
