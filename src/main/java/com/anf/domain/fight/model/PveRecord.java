package com.anf.domain.fight.model;

import java.util.Date;

public class PveRecord {
  private Date date;
  private int xpCh;
  private String result;
  private String rival;

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public int getXpCh() {
    return xpCh;
  }

  public void setXpCh(int xpCh) {
    this.xpCh = xpCh;
  }

  public String getResult() {
    return result;
  }

  public void setResult(String result) {
    this.result = result;
  }

  public String getRival() {
    return rival;
  }

  public void setRival(String rival) {
    this.rival = rival;
  }
}
