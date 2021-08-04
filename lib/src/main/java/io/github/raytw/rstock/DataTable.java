package io.github.raytw.rstock;

import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Vector;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

/**
 * Table.
 *
 * @author Ray Li
 * @param <E> E
 */
public class DataTable {
  private List<String> tableData;
  private List<String> tableFieldName;
  private TableModel<String> tableModel;
  private BiFunction<Integer, Integer, Boolean> isCellEditable;
  private JTable table;

  /** Initialize. */
  public DataTable() {
    tableFieldName = new ArrayList<String>();
    tableData = new ArrayList<String>();
    isCellEditable = (row, cel) -> true;
    tableModel =
        new TableModel<String>(tableData, tableFieldName) {
          private static final long serialVersionUID = -2438099847419741873L;

          @Override
          public boolean isCellEditable(int row, int col) {
            return isCellEditable.apply(row, col);
          }
        };
    table = new JTable(tableModel);
  }

  /**
   * Specifies cell that editable.
   *
   * @param listener listener
   */
  public void setCellEditableListener(BiFunction<Integer, Integer, Boolean> listener) {
    isCellEditable = listener;
  }

  /**
   * Sets listener that double click.
   *
   * @param listener listener
   */
  private void registerDoubleClickListener(DoubleClickListener<JTable> listener) {
    Optional<DoubleClickListener<JTable>> clickListener = Optional.ofNullable(listener);

    table.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent me) {
            if (me.getClickCount() == 2) { // to detect doble click events
              JTable target = (JTable) me.getSource();
              int row = target.getSelectedRow(); // select a row
              int col = target.getSelectedColumn(); // select a column

              clickListener.ifPresent(li -> li.onDoubleClick(row, col, target));
            }
          }
        });
  }

  /**
   * Sets listener that double click.
   *
   * @param listener listener
   */
  public void setDoubleClickListener(DoubleClickListener<Object> listener) {
    registerDoubleClickListener(
        (row, col, table) -> {
          Optional.ofNullable(listener)
              .ifPresent(li -> li.onDoubleClick(row, col, table.getValueAt(row, col)));
        });
  }

  /**
   * Sets listener that double click for single row.
   *
   * @param listener listener
   * @param column column position
   */
  public void setDoubleClickRowListener(Consumer<String> listener, int column) {
    registerDoubleClickListener(
        (row, col, table) -> {
          Optional.ofNullable(listener)
              .ifPresent(li -> li.accept(String.valueOf(table.getValueAt(row, column))));
        });
  }

  public void setRowSorter(TableRowSorter<javax.swing.table.TableModel> sorter) {
    getTable().setRowSorter(sorter);
  }

  /**
   * Replaces the column identifiers in the model. If the number of newIdentifiers is greater than
   * the current number of columns, new columns are added to the end of each row in the model. If
   * the number of newIdentifiers is less than the current number of columns, all the extra columns
   * at the end of a row are discarded.
   *
   * @param columnIdentifiers columnIdentifiers
   */
  public void setColumnsName(List<String> columnIdentifiers) {
    Vector<String> names = new Vector<>();

    columnIdentifiers.forEach(names::add);
    tableModel.setColumnIdentifiers(names);
  }

  /**
   * Set row that the height.
   *
   * @param h row height
   */
  public void setRowHeight(int h) {
    table.setRowHeight(h);
  }

  /**
   * Set font.
   *
   * @param font font
   */
  public void setFont(Font font) {
    table.setFont(font);
  }

  public TableColumnModel getColumnModel() {
    return table.getColumnModel();
  }

  /**
   * Add column name.
   *
   * @param columnName columnName
   */
  public void addColumnName(String columnName) {
    tableModel.addColumn(columnName);
  }

  /**
   * Add multiple field names.
   *
   * @param columns columns
   */
  public void addColumnsName(String[] columns) {
    for (int i = 0; i < columns.length; i++) {
      addColumnName(columns[i]);
    }
  }

  /**
   * Add multiple field names.
   *
   * @param columns columns
   */
  public void addColumnsName(List<String> columns) {
    columns.forEach(this::addColumnName);
  }

  /**
   * Add a piece of data, and the data will be added to the bottom row. When the data field is less
   * than the set value, the space is automatically filled, and the data that exceeds the set value
   * field will be lost.
   *
   * @param data data
   */
  public void addRow(List<String> data) {
    tableModel.addRow(data);
  }

  /**
   * Add a piece of data, and the data will be added to the bottom row. When the data field is less
   * than the set value, the space is automatically filled, and the data that exceeds the set value
   * field will be lost.
   *
   * @param data data
   */
  public void addRow(Object[] data) {
    tableModel.addRow(data);
  }

  public void insertLastRow(Vector<Object> data) {
    tableModel.insertRow(tableModel.getRowCount(), data);
  }

  public void insertLastRow(List<String> data) {
    tableModel.insertRow(tableModel.getRowCount(), data);
  }

  /**
   * Add data into specific row position.
   *
   * @param row row
   * @param data data
   */
  public void insertRow(int row, Object[] data) {
    tableModel.insertRow(row, data);
  }

  /**
   * Add multiple row data into after last row.
   *
   * @param data data
   */
  public void addLastMultiRow(List<List<String>> data) {
    data.forEach(this::addRow);
  }

  /**
   * Add multiple row data.
   *
   * @param mutilData multiple
   */
  public void addMutilRow(List<List<String>> mutilData) {
    mutilData.forEach(this::addRow);
  }

  /**
   * Remove specific row.
   *
   * @param row row
   */
  public void removeRow(int row) {
    tableModel.removeRow(row);
  }

  /** Remove all row. */
  public void removeAll() {
    for (int i = tableModel.getRowCount() - 1; i >= 0; i--) {
      tableModel.removeRow(i);
    }
  }

  /** Remove the selected data. */
  public void removeSelectedRows() {
    int[] row = table.getSelectedRows();
    for (int r = row.length - 1; r >= 0; r--) {
      removeRow(row[r]);
    }
  }

  /**
   * Returns the selected data.
   *
   * @return selected row.
   */
  public int[] getSelectedRows() {
    return table.getSelectedRows();
  }

  /**
   * Enable (disable) the position change feature, the default is on.
   *
   * @param enable enable
   */
  public void setReorderingAllowed(boolean enable) {
    table.getTableHeader().setReorderingAllowed(enable);
  }

  /**
   * Sets whether the table draws horizontal lines between cells. If showHorizontalLines is true it
   * does; if it is false it doesn't.
   *
   * @param showHorizontalLines showHorizontalLines - true if table view should draw horizontal
   *     lines
   */
  public void setShowHorizontalLines(boolean showHorizontalLines) {
    table.setShowHorizontalLines(showHorizontalLines);
  }

  /**
   * Set specify column that width.
   *
   * @param index index
   * @param width width
   */
  public void setColumnWidth(int index, int width) {
    getColumn(index).setPreferredWidth(width);
  }

  /**
   * Returns TableColumn.
   *
   * @param index index
   * @return TableColumn
   */
  public TableColumn getColumn(int index) {
    return table.getColumnModel().getColumn(index);
  }

  /**
   * Returns size of record.
   *
   * @return count
   */
  public int getRecordSize() {
    return tableData.size();
  }

  /**
   * Returns the number of rows in this data table.
   *
   * @return the number of rows
   */
  public int getRowCount() {
    return tableModel.getRowCount();
  }

  /**
   * Returns the number of columns in this data table.
   *
   * @return the number of columns
   */
  public int getColumnCount() {
    return tableModel.getColumnCount();
  }

  /**
   * Returns an attribute value for the cell at row and column.
   *
   * @param row row
   * @param column column
   * @return String
   */
  public String getValutAt(int row, int column) {
    return (String) tableModel.getValueAt(row, column);
  }

  /**
   * Returns JTable.
   *
   * @return JTable
   */
  public JTable getTable() {
    return table;
  }

  /**
   * Returns table model.
   *
   * @return DefaultTableModel
   */
  public DefaultTableModel getDefaultTableModel() {
    return tableModel;
  }

  /**
   * Returns JTable that has scroll feature.
   *
   * @return JScrollPane
   */
  public JScrollPane createScrollPane() {
    return new JScrollPane(table);
  }

  /**
   * Sets the object value for the cell at column and row. aValue is the new value. This method will
   * generate a tableChanged notification.
   *
   * @param value value
   * @param row row
   * @param column column
   */
  public void setValueAt(Object value, int row, int column) {
    tableModel.setValueAt(value, row, column);
  }

  /** Double Click Listener. */
  public static interface DoubleClickListener<T> {
    public void onDoubleClick(int row, int col, T data);
  }
}
