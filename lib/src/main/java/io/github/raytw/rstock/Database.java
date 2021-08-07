package io.github.raytw.rstock;

import dorkbox.notify.Notify;
import dorkbox.notify.Pos;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;
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
  private String queryTickerSql =
      "SELECT ticker_symbol FROM notify_settings WHERE ticker_symbol='%s';";
  private String queryTickerSymbolSql =
      "SELECT ticker_symbol,javascript,notify_period FROM notify_settings"
          + " WHERE ticker_symbol='%s';";
  private String queryAllEnableNotifySql =
      "SELECT ticker_symbol,javascript,notify_period FROM notify_settings"
          + " WHERE notify_period!=0";
  private String updateTickerSymbolSql =
      "UPDATE notify_settings SET javascript = '%s'"
          + " FORMAT JSON, notify_period = %d WHERE ticker_symbol='%s';";
  private String insertTickerSymbolSql =
      "INSERT INTO notify_settings VALUES('%s', '%s' FORMAT JSON, %d);";

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
   * @param listener java script,notify period
   */
  public void getJavascriptAndPeriod(String tickerSymbol, BiConsumer<String, Integer> listener) {
    String sql = String.format(queryTickerSymbolSql, tickerSymbol);
    RecordCursor record = notifySettings.createStatementExecutor().executeQuery(sql);

    if (record.getRecordCount() == 0) {
      listener.accept(null, null);

      return;
    }
    record.forEach(
        row -> {
          JSONObject json = new JSONObject(new String(row.getBlob("javascript")));
          int notifyPeriod = row.getInt("notify_period");

          listener.accept(json.optString("javascript"), notifyPeriod);
        });
  }

  /**
   * Returns all ticker symbols that enable notify.
   *
   * @return ticker symbol
   */
  public List<String> getAllEnableNotifyTickerSymbols() {
    StatementExecutor executor = notifySettings.createStatementExecutor();

    return executor
        .executeQuery(queryAllEnableNotifySql)
        .stream()
        .map(row -> row.getString("ticker_symbol"))
        .collect(Collectors.toList());
  }

  /**
   * Update or insert specifies the ticker symbol of java script and notify period.
   *
   * @param tickerSymbol ticker symbol
   * @param javascript java script
   * @param notifyPeriod notify period
   */
  public void upsertJavascriptAndPeriod(String tickerSymbol, String javascript, int notifyPeriod) {
    JSONObject settings = new JSONObject();

    settings.put("javascript", javascript);
    String json = settings.toString().replace("'", "''");
    StatementExecutor executor = notifySettings.createStatementExecutor();

    if (executor.executeQuery(String.format(queryTickerSql, tickerSymbol)).getRecordCount() > 0) {
      String updateSql = String.format(updateTickerSymbolSql, json, notifyPeriod, tickerSymbol);
      executor.execute(updateSql);
    } else {
      String insertSql = String.format(insertTickerSymbolSql, tickerSymbol, json, notifyPeriod);
      executor.execute(insertSql);
    }
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
