package ua.dvalex.covid19.utils;

import static org.apache.commons.lang3.time.DateUtils.addDays;
import static ua.dvalex.covid19.utils.Utils.OUTPUT_DATE_FORMAT;
import static ua.dvalex.covid19.utils.Utils.numberFormat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import ua.dvalex.covid19.domain.ChartSeries;
import ua.dvalex.covid19.domain.Forecast;
import ua.dvalex.covid19.domain.Forecast.ForecastBuilder;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HistoryForecast {

  private final List<Pair<Date, Integer>> records = new ArrayList<>();
  private Forecast forecast;
  private String historyOutput;
  private String forecastOutput;

  public static HistoryForecast of(ChartSeries chartSeries) {
    HistoryForecast result = new HistoryForecast();
    result.createRecords(chartSeries);
    result.createHistoryOutput();
    result.createForecast();
    result.createForecastOutput();
    return result;
  }

  private void createRecords(ChartSeries chartSeries) {
    List<Date> dates = chartSeries.getDates();
    List<Integer> values = chartSeries.getValues();
    Instant limit = dates.get(dates.size() - 1).toInstant().minus(7, ChronoUnit.DAYS);
    for (int i = dates.size() - 1; i >= 0; i--) {
      Date date = dates.get(i);
      if (date.toInstant().isBefore(limit)) {
        break;
      }
      records.add(Pair.of(date, values.get(i)));
    }
  }

  private void createHistoryOutput() {
    StringBuilder result = new StringBuilder();
    for (int i = records.size() - 1; i >= 0; i--) {
      Pair<Date, Integer> record = records.get(i);
      result.append(String.format("%s: %s",
          OUTPUT_DATE_FORMAT.format(record.getLeft()),
          numberFormat(record.getRight())));
      if (i > 0) {
        result.append("<br />");
      }
    }
    historyOutput = result.toString();
  }

  private void createForecast() {
    ForecastBuilder builder = Forecast.builder();
    Pair<Date, Integer> last = records.get(0);
    Pair<Date, Integer> first = records.get(records.size() - 1);
    Integer current = last.getRight();
    if (current == 0) {
      return;
    }
    builder.current(current);
    int deltaValue = first.getRight() - current;
    if (deltaValue <= 0) {
      return;
    }
    int days = (int) ChronoUnit.DAYS
        .between(first.getLeft().toInstant(), last.getLeft().toInstant());
    int descent = deltaValue / days;
    if (descent == 0) {
      return;
    }
    builder.descent(descent);
    int remain = current / descent;
    builder.remain(remain);
    builder.targetDate(addDays(last.getLeft(), remain));
    forecast = builder.build();
  }

  private void createForecastOutput() {
    forecastOutput = Optional.ofNullable(forecast)
        .map(f -> String.format("Descent: %s cases/day. Forecast: %d days (%s)<br />",
            numberFormat(f.getDescent()),
            f.getRemain(),
            OUTPUT_DATE_FORMAT.format(f.getTargetDate())))
        .orElse("<br />");
  }

  public List<Pair<Date, Integer>> getRecords() {
    return records;
  }

  public Optional<Forecast> getForecast() {
    return Optional.ofNullable(forecast);
  }

  public String getHistoryOutput() {
    return historyOutput;
  }

  public String getForecastOutput() {
    return forecastOutput;
  }
}
