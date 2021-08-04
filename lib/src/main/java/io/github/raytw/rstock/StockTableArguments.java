package io.github.raytw.rstock;

import java.util.List;
import java.util.function.Function;

/**
 * The arguments of StockTable.
 *
 * @author ray_lee
 */
public class StockTableArguments {
  private List<String> columnsName;
  private Function<Ticker, List<String>> apiResultProcess;

  public void setColumnsName(List<String> columnsName) {
    this.columnsName = columnsName;
  }

  public List<String> getColumnsName() {
    return columnsName;
  }

  public void setApiResultProcess(Function<Ticker, List<String>> processListener) {
    this.apiResultProcess = processListener;
  }

  public Function<Ticker, List<String>> getApiResultProcess() {
    return apiResultProcess;
  }
}
