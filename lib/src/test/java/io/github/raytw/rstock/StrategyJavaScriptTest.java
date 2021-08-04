package io.github.raytw.rstock;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class StrategyJavaScriptTest {

  @BeforeAll
  static void setUpBeforeClass() throws Exception {}

  @AfterAll
  static void tearDownAfterClass() throws Exception {}

  @Test
  void testCallEnableNotificationUseTicker() throws NotificationException {
    Ticker ticker = new Ticker(1, "2330.TW", "TPE:2330");

    StrategyJavaScript<Ticker> stragegy = new StrategyJavaScript<>();

    assertTrue(stragegy.enableNotification(ticker));
  }
}
