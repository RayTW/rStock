package io.github.raytw;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * main.
 *
 * @author Ray Li
 */
public class Stock {
  private static Stock instance = new Stock();
  private OkHttpClient client;
  // TODO please use your API id that google Apps script.
  private String apiUrl = "https://script.google.com/macros/s/yourGoogleAppsScriptsApiId/exec";
  private LinkedTransferQueue<Integer> apiPositionReserve;

  private Stock() {
    client = new OkHttpClient.Builder().readTimeout(3, TimeUnit.MINUTES).build();
    apiPositionReserve = new LinkedTransferQueue<>();

    for (int i = 1; i <= 10; i++) {
      apiPositionReserve.add(i);
    }
  }

  public static Stock get() {
    return instance;
  }

  /**
   * Returns position of API.
   *
   * @return position
   */
  public Integer getPosition() {
    try {
      return apiPositionReserve.take();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Returns stock detail uses specific ticker symbol.
   *
   * @param tickerList stock symbol list, e.g : "TPE:2880,TPE:0050"
   * @param attributeList stock attribute, e.g : "price,low,high", See <a
   *     href="https://support.google.com/docs/answer/3093281?hl=zh-Hant">GOOGLEFINANCE</a>
   * @param callback callback
   * @throws IOException IOException
   */
  public void getStickerDetail(
      List<String> tickerList, List<String> attributeList, Callback callback) throws IOException {

    getStickerDetail(
        tickerList.stream().reduce("", (a, b) -> a.isEmpty() ? b : a.concat(b).concat(",")),
        attributeList.stream().reduce("", (a, b) -> a.isEmpty() ? b : a.concat(b).concat(",")),
        callback);
  }

  /**
   * Returns stock detail uses specific ticker symbol.
   *
   * @param tickerList stock symbol list, e.g : "TPE:2880,TPE:0050"
   * @param attributeList stock attribute, e.g : "price,low,high", See <a
   *     href="https://support.google.com/docs/answer/3093281?hl=zh-Hant">GOOGLEFINANCE</a>
   * @param callback callback
   * @throws IOException IOException
   */
  public void getStickerDetail(String tickerList, String attributeList, Callback callback)
      throws IOException {
    Integer position = getPosition();

    HttpUrl url =
        HttpUrl.parse(apiUrl)
            .newBuilder()
            .addQueryParameter("apiPosition", position.toString())
            .addQueryParameter("tickerList", tickerList)
            .addQueryParameter("attributeList", attributeList)
            .build();
    Request request = new Request.Builder().get().url(url).build();

    client
        .newCall(request)
        .enqueue(new ApiCallBack(callback, () -> apiPositionReserve.offer(position)));
  }

  private class ApiCallBack implements Callback {
    private Callback callback;
    private Runnable doneListener;

    public ApiCallBack(Callback callback, Runnable doneListener) {
      this.callback = callback;
      this.doneListener = doneListener;
    }

    @Override
    public void onFailure(Call arg0, IOException arg1) {
      callback.onFailure(arg0, arg1);
      doneListener.run();
    }

    @Override
    public void onResponse(Call arg0, Response arg1) throws IOException {
      callback.onResponse(arg0, arg1);
      doneListener.run();
    }
  }
}
