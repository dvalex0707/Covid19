package ua.dvalex.covid19.domain;

import java.util.Date;
import lombok.Data;

@Data
public class Country {

  private String name;
  private String link;
  private int approvedCases;
  private int remainCases;
  private Date recovered;
  private Date reopened;

  public Country(String name, String link, int approvedCases) {
    this.name = name;
    this.link = link;
    this.approvedCases = approvedCases;
  }
}
