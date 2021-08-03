package io.github.raytw.rstock;

import java.io.IOException;
import java.util.Arrays;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.junit.jupiter.api.Test;

class StockTest {

  @Test
  void testStockList() throws IOException {
    Stock.get()
        .getTickerDetail(
            Arrays.asList("TPE:2880", "TPE:2881"),
            Arrays.asList("price", "low"),
            new Callback() {

              @Override
              public void onFailure(Call arg0, IOException arg1) {}

              @Override
              public void onResponse(Call arg0, Response arg1) throws IOException {
                System.out.println("stock=" + arg1.body().string());
              }
            });
  }
}
