package io.github.raytw;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.StreamSupport;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableCellRenderer;
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
  private Map<String, JSONArray> favoriteStocks;

  /**
   * Initialize.
   *
   * @throws IOException IOException
   */
  public void initialize() throws IOException {
    stockPages = new HashMap<>();
    favoriteStocks = new HashMap<>();
    setupLayout();
    loadSettings();
    refreshStocksPage(1);
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

    tabbedPand.addChangeListener(
        event -> {
          JTabbedPane pane = (JTabbedPane) event.getSource();

          refreshStocksPage(pane.getSelectedIndex() + 1);
        });

    centerPanel.add(tabbedPand);

    getContentPane().add(centerPanel, BorderLayout.CENTER);

    setLocationRelativeTo(null);
    setVisible(true);
  }

  private void loadSettings() throws JSONException, IOException {
    StockTableArguments argments = new StockTableArguments();

    argments.setApiParameters("price,change,high,low,changepct,volume");
    argments.setColumnsName(Arrays.asList("個股", "今價", "漲跌", "最高", "最低", "成交量"));
    argments.setApiResultProcess(
        (element) -> {
          String change = String.valueOf(element.get("change"));
          String changepct = change + " / " + String.valueOf(element.get("changepct") + "%");

          return Arrays.asList(
              element.getString("ticker"),
              String.valueOf(element.get("price")),
              changepct,
              String.valueOf(element.get("high")),
              String.valueOf(element.get("low")),
              String.valueOf(element.get("volume")));
        });

    JSONArray stockList = new JSONArray(new String(Files.readAllBytes(Paths.get("stocks.txt"))));

    loadStocks(
        stockList,
        (page, stocks) -> {
          String key = String.valueOf(page);
          favoriteStocks.put(key, stocks);
          StockTable list = new StockTable(argments);
          Color green = new Color(20, 255, 126);

          list.setColumnDefaultRenderer(
              2,
              new DefaultTableCellRenderer() {
                private static final long serialVersionUID = 7138175900961908856L;

                @Override
                public Component getTableCellRendererComponent(
                    JTable table,
                    Object value,
                    boolean isSelected,
                    boolean hasFocus,
                    int row,
                    int column) {
                  Component c =
                      super.getTableCellRendererComponent(
                          table, value, isSelected, hasFocus, row, column);

                  if (column == 2) {
                    String[] valueSplit = value.toString().split(" / ");
                    String change = valueSplit[0];
                    double doubleValue = Double.parseDouble(change);

                    if (doubleValue == 0.0) {
                      c.setForeground(Color.BLACK);
                    } else if (doubleValue > 0.0) {
                      c.setForeground(Color.RED);
                    } else {
                      c.setForeground(green);
                    }
                  }

                  return c;
                }
              });

          tabbedPand.add(key, list.getScrollTable());
          stockPages.put(key, list);
        });
  }

  private void loadStocks(JSONArray stockList, BiConsumer<Integer, JSONArray> pageWithStocks) {
    StreamSupport.stream(stockList.spliterator(), false)
        .map(JSONObject.class::cast)
        .forEach(
            json -> {
              Integer key = json.getInt("key");
              JSONArray stocks = json.getJSONArray("stocks");

              pageWithStocks.accept(key, stocks);
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

  /**
   * Refresh the page of current.
   *
   * @param page page
   */
  public void refreshStocksPage(int page) {
    if (stockPages.size() == 0) {
      return;
    }
    String key = String.valueOf(page);
    JSONArray stocks = favoriteStocks.get(key);

    if (stocks.length() == 0) {
      return;
    }
    StockTable stockList = stockPages.get(key);

    stockList.reload(stocks);
  }

  /**
   * main.
   *
   * @param args args
   */
  public static void main(String[] args) {
    JDialog loading = showLoadingDialog();

    loading.setVisible(true);

    SwingUtilities.invokeLater(
        () -> {
          StockApplication application = new StockApplication();
          try {
            application.initialize();
          } catch (IOException e) {
            e.printStackTrace();
          }

          loading.dispose();
        });
  }
}
