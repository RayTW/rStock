package io.github.raytw.rstock;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Single column.
 *
 * @author ray_lee
 */
public class StockTableCellRenderer extends DefaultTableCellRenderer {
  private static final long serialVersionUID = 7138175900961908856L;
  private Color green = new Color(15, 210, 102);

  @Override
  public Component getTableCellRendererComponent(
      JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    Component c =
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

    if (value == null) {
      return c;
    }

    if (column == 2) {
      String[] valueSplit = value.toString().split(" / ");
      String change = valueSplit[0];

      try {
        double doubleValue = Double.parseDouble(change);

        if (doubleValue == 0.0) {
          c.setForeground(Color.BLACK);
        } else if (doubleValue > 0.0) {
          c.setForeground(Color.RED);
        } else {
          c.setForeground(green);
        }
      } catch (NumberFormatException e) {
        c.setForeground(Color.BLACK);
      }
    }

    return c;
  }
}
