package io.github.raytw.rstock;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * Use stock choice strategy via java script.
 *
 * @author ray_lee
 */
public class StrategyJavaScript<V> implements StockChoiceStrategy<V> {
  private ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
  private ScriptEngine engine = scriptEngineManager.getEngineByName("nashorn");

  @Override
  public boolean enableNotification(V ticker) throws NotificationException {
    try {
      String js = new String(Files.readAllBytes(Paths.get("test.js")));

      engine.eval(js);
      Invocable invocable = (Invocable) engine;
      Object result = invocable.invokeFunction("enableNotification", ticker);
      System.out.println(result);

      return Boolean.class.cast(result);

    } catch (IOException e) {
      e.printStackTrace();
    } catch (ScriptException e) {
      throw new NotificationException("" + e.toString());
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
      throw new NotificationException("" + e.toString());
    }
    return false;
  }
}
