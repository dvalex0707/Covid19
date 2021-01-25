package ua.dvalex.covid19.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;

public class DataBase {

  @Getter
  private final List<Country> countries = new ArrayList<>();

  private final Map<Pair<RecordType, String>, ChartSeries> map = new HashMap<>();

  public void put(RecordType recordType, String country, Date date, int value) {
    Pair<RecordType, String> key = Pair.of(recordType, country);
    ChartSeries chartSeries = map.computeIfAbsent(key, k -> new ChartSeries());
    chartSeries.getDates().add(date);
    chartSeries.getValues().add(value);
  }

  public ChartSeries get(RecordType recordType, String country) {
    return map.get(Pair.of(recordType, country));
  }
}
