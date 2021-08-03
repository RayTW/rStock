package io.github.raytw.rstock;

import java.awt.BorderLayout;
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
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
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
  private Map<String, StockTable> stockPages;
  private Map<String, List<Ticker>> favoriteTicker;
  private Map<String, Integer> tickerSymbolMappingPage;
  private List<Ticker> allTicker;
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
    favoriteTicker = new ConcurrentHashMap<>();
    tickerSymbolMappingPage = new ConcurrentHashMap<>();
    allTicker = Collections.synchronizedList(new ArrayList<>());
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
  }

  private void loadSettings(String stockPath) throws JSONException, IOException {
    StockTableArguments argments = new StockTableArguments();

    apiParameters = "price,change,high,low,changepct,volume";
    argments.setColumnsName(Arrays.asList("個股", "今價", "漲跌", "最高", "最低"));
    argments.setApiResultProcess(
        (element) -> {
          String change = String.valueOf(element.get("change"));
          String changepct = String.valueOf(element.get("changepct"));
          String price = String.valueOf(element.get("price"));
          String high = String.valueOf(element.get("high"));
          String low = String.valueOf(element.get("low"));

          String changepctPercent = change + " / " + changepct + "%";

          return Arrays.asList(element.getString("ticker"), price, changepctPercent, high, low);
        });

    List<Ticker> tickers =
        loadStocks(new JSONArray(new String(Files.readAllBytes(Paths.get(stockPath)))));

    Map<String, List<Ticker>> tickersPages =
        tickers
            .stream()
            .collect(
                Collectors.groupingByConcurrent(
                    ticker -> {
                      tickerSymbolMappingPage.put(ticker.getSymbol(), ticker.getPage());
                      return String.valueOf(ticker.getPage());
                    }));

    tickersPages
        .entrySet()
        .forEach(
            element -> {
              List<Ticker> stocks = element.getValue();
              StockTable list = new StockTable(argments);

              list.setColumnDefaultRenderer(2, new StockTableCellRenderer());
              // Let each page default display that ticker symbol.
              list.setShowTickerSymbol(stocks);
              list.setDoubleClickTickerSymbolListener(
                  (tickerSymbol) -> {
                    // TODO show a component of the text area that strategy of choice stock for the
                    // user to write java script.
                    System.out.println("tickerSymbol=" + tickerSymbol);
                  });

              String page = element.getKey();

              tabbedPand.add(page, list.getScrollTable());
              tabbedPand.setSelectedIndex(0);
              stockPages.put(page, list);
            });
    allTicker = tickers;
    favoriteTicker = tickersPages;
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
    List<Ticker> tickers = allTicker.stream().distinct().collect(Collectors.toList());

    Stock.get()
        .batchTickerDetail(
            tickers,
            tickerBatch,
            apiParameters,
            new Callback() {

              @Override
              public void onFailure(Call call, IOException exception) {
                // TODO show error dialog.
                // https://github.com/dorkbox/Notify
                System.out.println("exception=" + exception);
              }

              @Override
              public void onResponse(Call call, Response response) throws IOException {
                List<JSONObject> allResultTickers = Collections.synchronizedList(new ArrayList<>());
                StreamSupport.stream(new JSONArray(response.body().string()).spliterator(), false)
                    .map(JSONObject.class::cast)
                    .map(json -> json.put("page", tickerSymbolMappingPage.get(json.get("ticker"))))
                    .forEach(allResultTickers::add);

                // group by page.
                allResultTickers
                    .stream()
                    .collect(Collectors.groupingBy(json -> String.valueOf(json.getInt("page"))))
                    .entrySet()
                    .stream()
                    .forEach(
                        element -> {
                          String page = element.getKey();
                          List<JSONObject> pageTickers = element.getValue();
                          JSONArray tickersArray = new JSONArray();
                          StockTable stockList = stockPages.get(page);

                          pageTickers.stream().forEach(tickersArray::put);

                          stockList.reload(tickersArray);
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
    List<Ticker> stocks = favoriteTicker.get(key);

    if (stocks.size() == 0) {
      return;
    }
    StockTable stockList = stockPages.get(key);

    Stock.get()
        .batchTickerDetail(
            stocks,
            tickerBatch,
            apiParameters,
            new Callback() {

              @Override
              public void onFailure(Call call, IOException exception) {
                // TODO show error dialog.
                // https://github.com/dorkbox/Notify
                System.out.println("exception=" + exception);
              }

              @Override
              public void onResponse(Call call, Response response) throws IOException {
                stockList.reload(new JSONArray(response.body().string()));
              }
            },
            this::triggerTimer);
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
