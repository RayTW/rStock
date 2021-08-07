package io.github.raytw.rstock;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * Configuration of application.
 *
 * @author Ray Li
 */
public class Configuration {
  private static Properties properties;

  /**
   * Reads a property list from path.
   *
   * @param path path
   * @throws FileNotFoundException FileNotFoundException
   * @throws IOException IOException
   */
  public static void loadProperties(String path) throws FileNotFoundException, IOException {
    properties = new Properties();
    properties.load(new FileInputStream(path));
  }

  /**
   * Returns the value by key.
   *
   * @param key key
   */
  public static String getProperty(String key) {
    return properties.getProperty(key, null);
  }

  /**
   * Returns the value by key.
   *
   * @param key key
   * @param defaultValue value
   */
  public static String getProperty(String key, String defaultValue) {
    return properties.getProperty(key, defaultValue);
  }

  /** 獲取properties屬性. */
  public static Properties getProperties() {
    return properties;
  }
}
