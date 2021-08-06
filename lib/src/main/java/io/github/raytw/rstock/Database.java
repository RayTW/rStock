package io.github.raytw.rstock;

import dorkbox.notify.Notify;
import dorkbox.notify.Pos;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import org.json.JSONObject;
import ra.db.StatementExecutor;
import ra.db.connection.OriginalConnection;
import ra.db.parameter.H2Parameters;
import ra.db.record.RecordCursor;

/**
 * Database manager.
 *
 * @author Ray Li
 */
public class Database {
  private OriginalConnection notifySettings;
  private String queryTickerSymbolSql =
      "SELECT ticker_symbol,settings FROM notify_settings WHERE ticker_symbol='%s';";
  private String updateTickerSymbolSql =
      "UPDATE notify_settings SET settings = '%s' FORMAT JSON WHERE ticker_symbol='%s';";
  private String insertTickerSymbolSql =
      "INSERT INTO notify_settings VALUES('%s', '%s' FORMAT JSON);";

  /** Connect to database. */
  public void connect() {
    notifySettings =
        new OriginalConnection(
            new H2Parameters.Builder()
                .localFile(new File("./data/sample").toString())
                .setName("myDb")
                .setProperties(
                    () -> {
                      Properties properties = new Properties();

                      properties.put("DATABASE_TO_UPPER", false);

                      return properties;
                    })
                .build());

    if (!notifySettings.connect()) {
      Notify.create()
          .position(Pos.TOP_RIGHT)
          .title("Error")
          .text("Failed to connect to database.")
          .darkStyle()
          .showError();
    }

    if (!createTable()) {
      Notify.create()
          .position(Pos.TOP_RIGHT)
          .title("Error")
          .text("Failed to create table.")
          .darkStyle()
          .showError();
    }
  }

  private boolean createTable() {
    try {
      String createTableSql = readAllStringFromResource("mydb.sql", "utf-8");
      notifySettings.createStatementExecutor().execute(createTableSql);
      return true;
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return false;
  }

  /**
   * Returns java script and period.
   *
   * @param tickerSymbol tickerSymbol
   * @param listener java script,period
   */
  public void getJavascriptAndPeriod(String tickerSymbol, BiConsumer<String, String> listener) {
    String sql = String.format(queryTickerSymbolSql, tickerSymbol);
    RecordCursor record = notifySettings.createStatementExecutor().executeQuery(sql);

    if (record.getRecordCount() == 0) {
      listener.accept(null, null);

      return;
    }
    record.forEach(
        row -> {
          JSONObject json = new JSONObject(new String(row.getBlob("settings")));
          listener.accept(json.optString("javascript"), json.optString("period"));
        });
  }

  /**
   * Update or insert specifies the ticker symbol of java script and notify period.
   *
   * @param tickerSymbol ticker symbol
   * @param javascript java script
   * @param notifyPeriod notify period
   */
  public void upsertJavascriptAndPeriod(
      String tickerSymbol, String javascript, String notifyPeriod) {
    JSONObject settings = new JSONObject();

    settings.put("javascript", javascript);
    settings.put("period", notifyPeriod);
    String json = settings.toString().replace("'", "''");
    String updateSql = String.format(updateTickerSymbolSql, json, tickerSymbol);

    StatementExecutor executor = notifySettings.createStatementExecutor();

    if (executor.execute(updateSql) > 0) {
      return;
    }
    String insertSql = String.format(insertTickerSymbolSql, tickerSymbol, json);

    executor.execute(insertSql);
  }

  /**
   * Read file from resource.
   *
   * @param fileName fileName
   * @param charset charset
   * @return string
   * @throws UnsupportedEncodingException UnsupportedEncodingException
   */
  public String readAllStringFromResource(String fileName, String charset)
      throws UnsupportedEncodingException {

    ClassLoader classLoader = getClass().getClassLoader();
    InputStream inputStream = classLoader.getResourceAsStream(fileName);

    if (inputStream == null) {
      throw new IllegalArgumentException("file not found! " + fileName);
    }
    return new BufferedReader(new InputStreamReader(inputStream, charset))
        .lines()
        .collect(Collectors.joining(System.lineSeparator()));
  }
}
