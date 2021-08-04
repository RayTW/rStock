package io.github.raytw.rstock;

/**
 * Notification exception.
 *
 * @author Ray Li
 */
public class NotificationException extends Exception {
  private static final long serialVersionUID = 4109490190134291319L;

  public NotificationException() {
    super();
  }

  public NotificationException(String arg0, Throwable arg1) {
    super(arg0, arg1);
  }

  public NotificationException(String arg0) {
    super(arg0);
  }

  public NotificationException(Throwable arg0) {
    super(arg0);
  }
}
