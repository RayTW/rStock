package io.github.raytw;

import java.util.List;
import java.util.function.Function;
import org.json.JSONObject;

/**
 * The arguments of StockTable.
 *
 * @author ray_lee
 */
public class StockTableArguments {
  private List<String> columnsName;
  private String apiParameters;
  private Function<JSONObject, List<String>> apiResultProcess;

  public void setColumnsName(List<String> columnsName) {
    this.columnsName = columnsName;
  }

  public List<String> getColumnsName() {
    return columnsName;
  }

  public void setApiParameters(String apiParameters) {
    this.apiParameters = apiParameters;
  }

  public String getApiParameters() {
    return apiParameters;
  }

  public void setApiResultProcess(Function<JSONObject, List<String>> processListener) {
    this.apiResultProcess = processListener;
  }

  public Function<JSONObject, List<String>> getApiResultProcess() {
    return apiResultProcess;
  }
}
