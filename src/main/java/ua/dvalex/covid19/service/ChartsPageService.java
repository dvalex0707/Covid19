package ua.dvalex.covid19.service;

import static org.apache.commons.lang3.time.DateUtils.addDays;
import static ua.dvalex.covid19.domain.RecordType.ACTIVE_CASES;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import ua.dvalex.covid19.builder.ChartBuilder;
import ua.dvalex.covid19.domain.ChartSeries;
import ua.dvalex.covid19.domain.Country;
import ua.dvalex.covid19.domain.DataBase;
import ua.dvalex.covid19.utils.HistoryForecast;

@Service
public class ChartsPageService {

  private static final int MAX_TREND_LENGTH = 30;
  private final DataBaseService dataBaseService;
  private final RecoveredAndReopenedService recoveredAndReopenedService;

  private static final String HTML_FORMAT =
      "<html><body><h1>%s</h1>%s</body></html>";
  private static final String CHART_FORMAT = "<div class=\"chart\"><h3>%s</h3>"
      + "<img src=\"data:image/jpg;base64, %s\" alt=\"Chart\" />%s</div>";

  public ChartsPageService(DataBaseService dataBaseService,
      RecoveredAndReopenedService recoveredAndReopenedService) {
    this.dataBaseService = dataBaseService;
    this.recoveredAndReopenedService = recoveredAndReopenedService;
  }

  public String active(int threshold) {
    DataBase dataBase = dataBaseService.load();

    List<String> countries = dataBase.getCountries().stream()
        .filter(country -> country.getApprovedCases() >= threshold)
        .map(Country::getName)
        .collect(Collectors.toList());
    String result = active(dataBase, countries);
    return createContent(dataBase, countries.size(), dataBaseService.getCurrentFileName(), result);
  }

  private String active(DataBase dataBase, List<String> countries) {
    return countries.stream()
            .map(country -> {
              ChartBuilder chartBuilder = new ChartBuilder();
              ChartSeries chartSeries;
              String extra = "";
              chartSeries = dataBase.get(ACTIVE_CASES, country);
              if (chartSeries != null) {
                chartBuilder.addSeries(country, chartSeries, false);
                HistoryForecast historyForecast = HistoryForecast.of(chartSeries);
                createForecastChartSeries(historyForecast).ifPresent(forecastChartSeries ->
                        chartBuilder.addSeries("forecast", forecastChartSeries, false));
                extra = String.format("<p>%s%s</p>",
                        historyForecast.getForecastOutput(), historyForecast.getHistoryOutput());
              }
              return String.format(CHART_FORMAT, country, chartBuilder.build(), extra);
            })
        .collect(Collectors.joining());
  }

  private Optional<ChartSeries> createForecastChartSeries(HistoryForecast historyForecast) {
    return historyForecast.getForecast().map(forecast -> {
      ChartSeries forecastChartSeries = new ChartSeries();
      List<Date> dates = forecastChartSeries.getDates();
      List<Integer> values = forecastChartSeries.getValues();
      List<Pair<Date, Integer>> records = historyForecast.getRecords();
      Pair<Date, Integer> lastRecord = records.get(records.size() - 1);
      dates.add(lastRecord.getLeft());
      values.add(lastRecord.getRight());
      Date date = forecast.getTargetDate();
      int value = 0;
      if (forecast.getRemain() > MAX_TREND_LENGTH) {
        Pair<Date, Integer> firstRecord = records.get(0);
        date = addDays(firstRecord.getLeft(), MAX_TREND_LENGTH);
        value = firstRecord.getRight() - MAX_TREND_LENGTH * forecast.getDescent();
      }
      dates.add(date);
      values.add(value);
      return forecastChartSeries;
    });
  }

  private String createContent(DataBase dataBase, int countryCount, String currentFileName,
      String result) {
    String completelyRecovered = recoveredAndReopenedService.output(dataBase);
    String dateTime = currentFileName.substring(0, currentFileName.length() - 4);
    String header = String
        .format("Active cases (%d countries). Updated: %s", countryCount, dateTime);
    return String.format(HTML_FORMAT, header, completelyRecovered + result);
  }
}
