package io.github.raytw.rstock;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
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
  private String apiUrl = null;
  private LinkedTransferQueue<Integer> apiPositionReserve;

  private Stock() {
    apiUrl = Configuration.getProperty("api.url");
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
  public void getTickerDetail(
      List<String> tickerList, List<String> attributeList, Callback callback) throws IOException {

    getTickerDetail(
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
  public void getTickerDetail(String tickerList, String attributeList, Callback callback)
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

  /**
   * Obtain ticker detail in batches and multiple threads concurrently.
   *
   * @param tickersSymbol tickersSymbol
   * @param chunkSize chunkSize
   * @param attributeList stock attribute, e.g : "price,low,high", See <a
   *     href="https://support.google.com/docs/answer/3093281?hl=zh-Hant">GOOGLEFINANCE</a>
   * @param callback callback
   */
  public void batchTickerDetail(
      Set<String> tickersSymbol, int chunkSize, String attributeList, Callback callback) {
    batchTickerDetail(tickersSymbol, chunkSize, attributeList, callback, null);
  }

  /**
   * Obtain ticker detail in batches and multiple threads concurrently.
   *
   * @param tickersSymbol tickersSymbol
   * @param chunkSize chunkSize
   * @param attributeList stock attribute, e.g : "price,low,high", See <a
   *     href="https://support.google.com/docs/answer/3093281?hl=zh-Hant">GOOGLEFINANCE</a>
   * @param callback callback
   * @param done done
   */
  public void batchTickerDetail(
      Set<String> tickersSymbol,
      int chunkSize,
      String attributeList,
      Callback callback,
      Runnable done) {
    AtomicInteger counter = new AtomicInteger();

    Collection<List<String>> result =
        tickersSymbol
            .stream()
            .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / chunkSize))
            .values();

    Optional<CountDownLatch> letchRef =
        Optional.ofNullable(done == null ? null : new CountDownLatch(result.size()));

    letchRef.ifPresent(
        o -> {
          new Thread(
                  () -> {
                    try {
                      o.await();
                      done.run();
                    } catch (InterruptedException e) {
                      e.printStackTrace();
                    }
                  })
              .start();
        });

    result
        .parallelStream()
        .forEach(
            tickers -> {
              String tickerList =
                  tickers.stream().reduce("", (a, b) -> a.isEmpty() ? b : a.concat(",").concat(b));

              try {
                getTickerDetail(
                    tickerList,
                    attributeList,
                    new ApiCallBack(callback, () -> letchRef.ifPresent(ref -> ref.countDown())) {});
              } catch (IOException e) {
                e.printStackTrace();
              }
            });
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
      doneListener.run();
      callback.onFailure(arg0, arg1);
    }

    @Override
    public void onResponse(Call arg0, Response arg1) throws IOException {
      doneListener.run();
      callback.onResponse(arg0, arg1);
    }
  }
}
