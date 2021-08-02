package io.github.raytw.rstock;

import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;
import javax.swing.table.DefaultTableModel;

/**
 * Table model.
 *
 * @author Ray Li
 */
public class TableModel<T> extends DefaultTableModel {
  private static final long serialVersionUID = -6800245208090134048L;

  public TableModel(Vector<String> data, Vector<String> columnNames) {
    super(data, columnNames);
  }

  /**
   * Initialize.
   *
   * @param data data
   * @param columnsName columnsName
   */
  public TableModel(List<T> data, List<T> columnsName) {
    Vector<String> d = new Vector<>();
    Vector<String> c = new Vector<>();

    data.forEach(o -> d.add(o.toString()));
    columnsName.forEach(o -> c.add(o.toString()));

    setDataVector(d, c);
  }

  public TableModel(Object[][] data, Object[] columnNames) {
    super(data, columnNames);
  }

  /**
   * Add single row data.
   *
   * @param data data
   */
  public void addRow(List<T> data) {
    Vector<String> d = new Vector<>();

    data.forEach(o -> d.add(o.toString()));

    super.addRow(d);
  }

  public void insertRow(int row, List<T> data) {
    super.insertRow(row, data.stream().collect(Collectors.toCollection(Vector<T>::new)));
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    if (getRowCount() == 0) {
      return super.getColumnClass(columnIndex);
    }
    Object obj = getValueAt(0, columnIndex);

    if (obj != null) {
      return obj.getClass();
    }
    return super.getColumnClass(columnIndex);
  }
}
