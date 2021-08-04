package io.github.raytw.rstock;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

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
    allTicker.setColumnsName(arguments.getColumnsName());
    allTicker.getTable().setAutoCreateRowSorter(true);
    allTicker.setCellEditableListener((row, col) -> false);
    allTicker.setFont(new Font("Serif", Font.BOLD, 14));
    TableRowSorter<TableModel> sorter = new TableRowSorter<>(allTicker.getTable().getModel());
    allTicker.setRowSorter(sorter);
    allTicker.setShowHorizontalLines(true);
  }

  public DataTable getTable() {
    return allTicker;
  }

  public JScrollPane getScrollTable() {
    return allTicker.createScrollPane();
  }

  /**
   * Returns the values of single column.
   *
   * @param column column
   * @return row value
   */
  public List<String> getColumnValues(int column) {
    List<String> ret = new ArrayList<String>();

    for (int i = 0; i < allTicker.getRowCount(); i++) {
      ret.add(allTicker.getValutAt(i, column));
    }

    return ret;
  }

  public void setColumnDefaultRenderer(int column, DefaultTableCellRenderer renderer) {
    allTicker.getColumnModel().getColumn(column).setCellRenderer(renderer);
  }

  /**
   * The column default display that ticker symbol.
   *
   * @param stocks stocks
   */
  public void setShowTickerSymbol(List<Ticker> stocks) {
    SwingUtilities.invokeLater(
        () -> {
          allTicker.removeAll();
          stocks
              .stream()
              .map(
                  ticker -> {
                    Object[] objs = new Object[arguments.getColumnsName().size()];
                    Arrays.fill(objs, "");
                    objs[0] = ticker.getSymbol();
                    return objs;
                  })
              .forEach(allTicker::addRow);
        });
  }

  /**
   * Sets listener that returns the ticker symbol when double click single row.
   *
   * @param listener listener
   */
  public void setDoubleClickTickerSymbolListener(Consumer<String> listener) {
    allTicker.setDoubleClickRowListener(listener, 0);
  }

  /**
   * Reload information that stock list.
   *
   * @param stocks stocks
   */
  public void reload(List<Ticker> stocks) {
    SwingUtilities.invokeLater(
        () -> {
          for (int i = 0; i < allTicker.getRowCount(); i++) {
            String tickerSymbol = allTicker.getValutAt(i, 0);

            Optional<List<String>> oneRow =
                stocks
                    .stream()
                    .filter(element -> tickerSymbol.equals(element.getSymbol()))
                    .map(arguments.getApiResultProcess())
                    .findFirst();

            final int columnIndex = i;
            oneRow.ifPresent(
                dataVector -> {
                  String newValue = null;
                  for (int j = 0; j < allTicker.getColumnCount(); j++) {
                    newValue = dataVector.get(j);
                    if (newValue.contains("#ERROR!")) {
                      continue;
                    }
                    allTicker.setValueAt(newValue, columnIndex, j);
                  }
                });
          }
        });
  }
}
