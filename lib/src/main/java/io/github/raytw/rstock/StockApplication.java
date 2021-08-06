package io.github.raytw.rstock;

import dorkbox.notify.Notify;
import dorkbox.notify.Pos;
import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Application.
 *
 * @author Ray Li
 */
public class StockApplication extends JFrame {
  private static final long serialVersionUID = 4239430715284526041L;
  private JTextField searchComic;
  private JTabbedPane tabbedPand;
  private JavaScriptEditor jsEditor;
  private StrategyJavaScript<Ticker> strategy;
  private Map<String, StockTable> stockPages;
  private Map<String, Ticker> allTicker;
  private Database database;
  private Timer timer;
  private String apiParameters;
  private int tickerBatch = 3;
  private int pageReloadSeconds = 5;

  /**
   * Initialize.
   *
   * @throws IOException IOException
   */
  public StockApplication() throws IOException {
    stockPages = new ConcurrentHashMap<>();
    allTicker = new ConcurrentHashMap<>();
    database = new Database();
    strategy = new StrategyJavaScript<>();
    timer = new Timer();
    setupLayout();
  }

  private void setupLayout() throws IOException {
    setTitle("rStock");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    double scale = 0.4;
    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

    setSize((int) (screen.getWidth() * scale), (int) (screen.getHeight() * scale));

    JPanel northPanel = new JPanel();
    getContentPane().add(northPanel, BorderLayout.NORTH);

    JPanel findPanel = new JPanel(new GridLayout(1, 0));
    searchComic = new JTextField();
    findPanel.add(new JLabel("股票代號"), BorderLayout.WEST);
    findPanel.add(searchComic, BorderLayout.CENTER);
    northPanel.add(findPanel, BorderLayout.CENTER);

    JPanel centerPanel = new JPanel(new GridLayout(0, 1));
    tabbedPand = new JTabbedPane();

    centerPanel.add(tabbedPand);

    getContentPane().add(centerPanel, BorderLayout.CENTER);

    setLocationRelativeTo(null);
    setVisible(true);

    jsEditor = new JavaScriptEditor(this, ModalityType.APPLICATION_MODAL);
    jsEditor.setApplyAndCloseListener(new ApplyAndCloseImpl());
  }

  private void loadSettings(String stockPath) throws JSONException, IOException {
    StockTableArguments argments = new StockTableArguments();

    apiParameters = "price,change,high,low,changepct";
    argments.setColumnsName(Arrays.asList("個股", "今價", "漲跌", "最高", "最低"));
    argments.setApiResultProcess(
        (element) -> {
          String changepctPercent = element.getChange() + " / " + element.getChangepct() + "%";

          return Arrays.asList(
              element.getSymbol(),
              element.getPrice(),
              changepctPercent,
              element.getHigh(),
              element.getLow());
        });

    List<Ticker> tickersSymbol =
        loadStocks(new JSONArray(new String(Files.readAllBytes(Paths.get(stockPath)))));

    tickersSymbol.stream().forEach(ticker -> allTicker.put(ticker.getSymbol(), ticker));

    Map<String, List<Ticker>> eachPageTickers =
        tickersSymbol
            .stream()
            .collect(Collectors.groupingByConcurrent(ticker -> String.valueOf(ticker.getPage())));

    eachPageTickers
        .entrySet()
        .forEach(
            element -> {
              List<Ticker> stocks = element.getValue();
              StockTable list = new StockTable(argments);

              list.setColumnDefaultRenderer(2, new StockTableCellRenderer());
              // Let each page default display that ticker symbol.
              list.setShowTickerSymbol(stocks);
              list.setDoubleClickTickerSymbolListener(new ClickTickerSymbolImpl());
              list.setPeriodVerfyTickerListener(new PeriodVerfyTickerImpl());

              String page = element.getKey();

              stockPages.put(page, list);
              tabbedPand.add(page, list.getScrollTable());
              tabbedPand.setSelectedIndex(0);
            });

    database.connect();
  }

  private List<Ticker> loadStocks(JSONArray stockList) {
    return StreamSupport.stream(stockList.spliterator(), false)
        .map(JSONObject.class::cast)
        .map(
            json -> {
              int key = json.getInt("key");
              JSONArray stocks = json.getJSONArray("stocks");
              return StreamSupport.stream(stocks.spliterator(), false)
                  .map(JSONObject.class::cast)
                  .map(
                      stock -> {
                        String id = stock.getString("id");
                        String region = stock.getString("region");
                        String symbol = id;

                        if ("TW".equals(region)) {
                          symbol = "TPE:" + symbol.split("[.]")[0];
                        }

                        return new Ticker(key, id, symbol);
                      })
                  .collect(Collectors.toList());
            })
        .reduce(
            new ArrayList<Ticker>(),
            (a, b) -> {
              a.addAll(b);
              return a;
            });
  }

  /**
   * Returns stock symbol by search bar.
   *
   * @return stock symbol
   */
  public String getStockSymbol() {
    return searchComic.getText();
  }

  /**
   * Show loading dialog.
   *
   * @return JDialog
   */
  public static JDialog showLoadingDialog() {
    JDialog dialog = new JDialog();
    dialog.setLayout(new GridBagLayout());
    dialog.add(new JLabel("Loading"));
    dialog.setMaximumSize(new Dimension(150, 50));
    dialog.setResizable(false);
    dialog.setModal(false);
    dialog.setUndecorated(true);
    dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    dialog.setLocationRelativeTo(null);
    dialog.setVisible(true);
    dialog.pack();

    return dialog;
  }

  /** Load each page stock. */
  public void refreshStocksAllPage() {
    Stock.get()
        .batchTickerDetail(
            allTicker.keySet(),
            tickerBatch,
            apiParameters,
            new Callback() {

              @Override
              public void onFailure(Call call, IOException exception) {
                Notify.create()
                    .position(Pos.TOP_RIGHT)
                    .title("Error")
                    .text("Refresh failure that all page \nExceptoin : " + exception.getMessage())
                    .darkStyle()
                    .showError();
              }

              @Override
              public void onResponse(Call call, Response response) throws IOException {
                List<Ticker> tickersLatest = convertJsonToTicker(response.body().string());

                // group by page.
                tickersLatest
                    .stream()
                    .collect(Collectors.groupingBy(ticker -> ticker.getPage()))
                    .entrySet()
                    .stream()
                    .forEach(
                        element -> {
                          int page = element.getKey();
                          StockTable stockList = stockPages.get(String.valueOf(page));

                          stockList.reload(element.getValue());
                        });
              }
            },
            this::triggerTimer); // complete load all ticker detail.
  }

  /**
   * Refresh the page of current.
   *
   * @param page page
   */
  public void refreshStocksSinglePage(int page) {
    if (stockPages.size() == 0) {
      return;
    }
    String key = String.valueOf(page);
    List<String> symbols = stockPages.get(key).getColumnValues(0);

    if (symbols.size() == 0) {
      return;
    }
    Stock.get()
        .batchTickerDetail(
            new HashSet<>(symbols),
            tickerBatch,
            apiParameters,
            new Callback() {

              @Override
              public void onFailure(Call call, IOException exception) {
                Notify.create()
                    .position(Pos.TOP_RIGHT)
                    .title("Error")
                    .text(
                        "Refresh failure for page "
                            + page
                            + " \nExceptoin : "
                            + exception.getMessage())
                    .darkStyle()
                    .showError();
              }

              @Override
              public void onResponse(Call call, Response response) throws IOException {
                List<Ticker> tickersLatest = convertJsonToTicker(response.body().string());
                StockTable stockList = stockPages.get(key);
                stockList.reload(tickersLatest);
              }
            },
            this::triggerTimer);
  }

  private List<Ticker> convertJsonToTicker(String responseString) {
    JSONArray responseBody = new JSONArray(responseString);

    return Collections.synchronizedList(
        StreamSupport.stream(responseBody.spliterator(), false)
            .map(JSONObject.class::cast)
            .map(json -> allTicker.get(json.getString("ticker")).setValues(json))
            .collect(Collectors.toList()));
  }

  /** Refresh single page when each five seconds. */
  private void triggerTimer() {
    timer.schedule(
        new TimerTask() {

          @Override
          public void run() {
            if (tabbedPand.getSelectedIndex() == -1) {
              return;
            }
            refreshStocksSinglePage(tabbedPand.getSelectedIndex() + 1);
          }
        },
        pageReloadSeconds * 1000);
  }

  private class ClickTickerSymbolImpl implements Consumer<String> {

    @Override
    public void accept(String tickerSymbol) {
      // show a component of the text area that strategy of choice stock for the
      // user to write java script.
      database.getJavascriptAndPeriod(
          tickerSymbol,
          (javaScript, notifyPeroid) -> {
            if (javaScript == null) {
              try {
                javaScript = database.readAllStringFromResource("strategy.js", "utf-8");
              } catch (IOException e) {
                Notify.create()
                    .position(Pos.TOP_RIGHT)
                    .title("File not found")
                    .text(e.getMessage())
                    .darkStyle()
                    .showError();
                return;
              }
            }

            jsEditor.reset();
            jsEditor.setVerifyTicker(tickerSymbol, javaScript, notifyPeroid);
            jsEditor.setVisible(true);
          });
    }
  }

  private class ApplyAndCloseImpl implements Function<JavaScriptEditor, Boolean> {

    @Override
    public Boolean apply(JavaScriptEditor eidtor) {
      String tickerSymbol = eidtor.getTickerSymbol();
      String javaScript = eidtor.getJavaScript();
      String notifyPeriod = eidtor.getNotifyPeriodSelectedValue();

      try {
        Ticker ticker = allTicker.get(tickerSymbol);
        strategy.enableNotification(javaScript, ticker);
        database.upsertJavascriptAndPeriod(tickerSymbol, javaScript, notifyPeriod);

        return Boolean.TRUE;
      } catch (NotificationException e) {
        jsEditor.setConsole(e.getMessage());
      }
      return Boolean.FALSE;
    }
  }

  /** Verify each ticker is needed to pop-up notify when match strategy. */
  private class PeriodVerfyTickerImpl implements Consumer<String> {
    @Override
    public void accept(String tickerSymbol) {
      Ticker ticker = allTicker.get(tickerSymbol);

      if (ticker == null) {
        return;
      }
      database.getJavascriptAndPeriod(
          tickerSymbol,
          (javaScript, notifyPeroid) -> {
            if (javaScript == null) {
              return;
            }

            try {
              if (strategy.enableNotification(javaScript, ticker)) {
                Notify.create()
                    .position(Pos.TOP_RIGHT)
                    .title("Notification")
                    .text("Take order,id" + ticker.getId() + ",price:" + ticker.getPrice())
                    .darkStyle()
                    .showConfirm();
              }
            } catch (NotificationException e) {
              Notify.create()
                  .position(Pos.TOP_RIGHT)
                  .title("Java script compile failure")
                  .text(e.getMessage())
                  .darkStyle()
                  .showError();
            }
          });
    }
  }

  /**
   * main.
   *
   * @param args args
   */
  public static void main(String[] args) {
    JDialog loading = showLoadingDialog();

    loading.setVisible(true);
    try {
      StockApplication application = new StockApplication();
      application.loadSettings("stocks.txt");
      SwingUtilities.invokeLater(() -> loading.dispose());
      application.refreshStocksAllPage();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
