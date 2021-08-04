package io.github.raytw.rstock;

import org.json.JSONObject;

/**
 * Single ticker symbol that detail.
 *
 * @author ray_lee
 */
public class Ticker {
  private int page;
  private String id;
  private String symbol;
  private String price;
  private String change;
  private String high;
  private String low;
  private String changepct;
  private String volume;

  /**
   * Initialize.
   *
   * @param page page
   * @param id id e.g : 2330.TW
   * @param symbol symbol e.g : TPE:2330
   */
  public Ticker(int page, String id, String symbol) {
    this.page = page;
    this.id = id;
    this.symbol = symbol;
  }

  public int getPage() {
    return this.page;
  }

  public String getId() {
    return this.id;
  }

  public String getSymbol() {
    return this.symbol;
  }

  /**
   * Set the stock values.
   *
   * @param json json
   */
  public Ticker setValues(JSONObject json) {
    this.price = String.valueOf(json.get("price"));
    this.change = String.valueOf(json.get("change"));
    this.low = String.valueOf(json.get("low"));
    this.high = String.valueOf(json.get("high"));
    this.changepct = String.valueOf(json.get("changepct"));

    return this;
  }

  public String getPrice() {
    return this.price;
  }

  public void setPrice(String price) {
    this.price = price;
  }

  public String getChange() {
    return this.change;
  }

  public void setChange(String change) {
    this.change = change;
  }

  public String getHigh() {
    return this.high;
  }

  public void setHigh(String high) {
    this.high = high;
  }

  public String getLow() {
    return this.low;
  }

  public void setLow(String low) {
    this.low = low;
  }

  public String getChangepct() {
    return this.changepct;
  }

  public void setChangepct(String changepct) {
    this.changepct = changepct;
  }

  public String getVolume() {
    return this.volume;
  }

  public void setVolume(String volume) {
    this.volume = volume;
  }

  @Override
  public String toString() {
    return "page[" + page + "],id[" + id + "],symbol[" + symbol + "]";
  }
}
