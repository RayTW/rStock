package io.github.raytw.rstock;

import java.util.Objects;

/**
 * Single ticker symbol that detail.
 *
 * @author ray_lee
 */
public class Ticker {
  private int page;
  private String id;
  private String symbol;

  /**
   * Initialize.
   *
   * @param page page
   * @param id id e.g : 2330.TW
   * @param symbol symbol e.g : "TPE:2330
   */
  public Ticker(int page, String id, String symbol) {
    this.page = page;
    this.id = id;
    this.symbol = symbol;
  }

  public int getPage() {
    return page;
  }

  public String getId() {
    return id;
  }

  public String getSymbol() {
    return symbol;
  }

  public long hashcode() {
    return Objects.hash(id, symbol);
  }

  @Override
  public String toString() {
    return "page[" + page + "],id[" + id + "],symbol[" + symbol + "]";
  }
}
