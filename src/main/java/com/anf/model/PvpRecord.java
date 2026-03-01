package com.anf.model;

import java.util.Date;

public class PvpRecord {

  private Date date;
  private int ratingCh;
  private String result;
  private String rival;

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public int getRatingCh() {
    return ratingCh;
  }

  public void setRatingCh(int ratingCh) {
    this.ratingCh = ratingCh;
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

  public PvpRecord(Date date, int ratingCh, String result, String rival) {
    this.date = date;
    this.ratingCh = ratingCh;
    this.result = result;
    this.rival = rival;
  }

  public PvpRecord() {}
}
