package io.github.raytw;

import java.awt.Font;
import java.io.IOException;
import java.util.stream.StreamSupport;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Ticker list.
 *
 * @author Ray Li
 */
public class StockTable {
  private DataTable allTicker;
  private StockTableArguments arguments;

  /** Initialize. */
  public StockTable(StockTableArguments arguments) {
    this.arguments = arguments;
    allTicker = new DataTable();
    allTicker.addColumnsName(arguments.getColumnsName());
    allTicker.getTable().setAutoCreateRowSorter(true);
    allTicker.setCellEditableListener((row, col) -> false);
    allTicker.setFont(new Font("Serif", Font.BOLD, 14));
    TableRowSorter<TableModel> sorter = new TableRowSorter<>(allTicker.getTable().getModel());
    allTicker.setRowSorter(sorter);
  }

  public DataTable getTable() {
    return allTicker;
  }

  public JScrollPane getScrollTable() {
    return allTicker.toScrollPane();
  }

  /**
   * Reload information that stock list.
   *
   * @param stocks stocks
   */
  public void reload(JSONArray stocks) {
    String tickerList =
        StreamSupport.stream(stocks.spliterator(), false)
            .map(JSONObject.class::cast)
            .map(
                element -> {
                  String symbol = element.getString("id");
                  String region = element.getString("region");

                  if ("TW".equals(region)) {
                    return "TPE:" + symbol.split("[.]")[0];
                  }

                  return symbol;
                })
            .reduce("", (a, b) -> a.isEmpty() ? b : a.concat(",").concat(b));

    try {
      Stock.get()
          .getStickerDetail(
              tickerList,
              arguments.getApiParameters(),
              new Callback() {

                @Override
                public void onFailure(Call call, IOException exception) {
                  // TODO Show error dialog.
                  // https://github.com/dorkbox/Notify
                  System.out.println("exception=" + exception);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                  JSONArray result = new JSONArray(response.body().string());
                  SwingUtilities.invokeLater(
                      () -> {
                        allTicker.removeAll();
                        StreamSupport.stream(result.spliterator(), false)
                            .map(JSONObject.class::cast)
                            .map(arguments.getApiResultProcess())
                            .forEach(allTicker::addRow);
                      });
                }
              });
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
