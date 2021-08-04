package io.github.raytw.rstock;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * Use stock choice strategy via java script.
 *
 * @author ray_lee
 */
public class StrategyJavaScript<V> {
  private ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
  private ScriptEngine engine = scriptEngineManager.getEngineByName("nashorn");

  /**
   * Verify strategy of notification.
   *
   * @param javaScript javaScript
   * @param ticker ticker
   * @return boolean
   * @throws NotificationException NotificationException
   */
  public boolean enableNotification(String javaScript, V ticker) throws NotificationException {
    try {
      engine.eval(javaScript);
      Invocable invocable = (Invocable) engine;
      Object result = invocable.invokeFunction("enableNotification", ticker);
      System.out.println(result);

      return Boolean.class.cast(result);
    } catch (ScriptException e) {
      throw new NotificationException("" + e.getMessage());
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
      throw new NotificationException("" + e.getMessage());
    }
  }
}
