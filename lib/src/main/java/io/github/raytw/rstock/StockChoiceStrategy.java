package io.github.raytw.rstock;

/**
 * Stock choices strategy.
 *
 * @author ray_lee
 */
public interface StockChoiceStrategy<V> {
  public boolean enableNotification(V value) throws NotificationException;
}
