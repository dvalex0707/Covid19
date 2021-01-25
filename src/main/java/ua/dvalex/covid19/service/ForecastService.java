package ua.dvalex.covid19.service;

import static ua.dvalex.covid19.utils.Utils.OUTPUT_DATE_FORMAT;
import static ua.dvalex.covid19.utils.Utils.createTableRow;
import static ua.dvalex.covid19.utils.Utils.numberFormat;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import ua.dvalex.covid19.domain.ChartSeries;
import ua.dvalex.covid19.domain.Country;
import ua.dvalex.covid19.domain.DataBase;
import ua.dvalex.covid19.domain.Forecast;
import ua.dvalex.covid19.domain.RecordType;
import ua.dvalex.covid19.utils.HistoryForecast;

@Service
public class ForecastService {

  private static final String HTML_FORMAT =
      "<html><head><style>"
          + "h1, h2, h3, h4, p, th, td {font-family: Arial, Helvetica, sans-serif;} "
          + "table, th, td {border: 1px solid black;border-spacing: 0px;} "
          + ".right {text-align: right;} "
          + ".center {text-align: center;} "
          + "</style></head><body><h1>Forecast for %d days (%d countries). Updated: %s</h1>"
          + "<table><thead><th>Country</th><th>Initial</th><th>Remain cases</th>"
          + "<th>Descent (cases/day)</th><th>Remain days</th><th>Target date</th></thead>"
          + "%s</table></body></html>";

  private final DataBaseService dataBaseService;

  public ForecastService(DataBaseService dataBaseService) {
    this.dataBaseService = dataBaseService;
  }

  public String forecast(int currentCasesThreshold, int remainDaysThreshold) {
    DataBase dataBase = dataBaseService.load();
    List<String> rowList = dataBase.getCountries().stream()
        .map(country -> {
          ChartSeries chartSeries = dataBase.get(RecordType.ACTIVE_CASES, country.getName());
          Optional<Forecast> forecast = chartSeries != null
              ? HistoryForecast.of(chartSeries).getForecast()
              : Optional.empty();
          return Pair.of(country, forecast);
        })
        .filter(pair -> pair.getRight().isPresent())
        .filter(pair -> pair.getRight().get().getRemain() <= remainDaysThreshold)
        .filter(pair -> pair.getRight().get().getCurrent() > 0)
        .filter(pair -> pair.getLeft().getApprovedCases() >= currentCasesThreshold)
        .map(pair -> Pair.of(pair.getLeft(), pair.getRight().get()))
        .sorted(Comparator.comparing(o -> o.getRight().getTargetDate()))
        .map(pair -> {
          Country country = pair.getLeft();
          Forecast forecast = pair.getRight();
          return createTableRow(
              Pair.of(country.getName(), null),
              Pair.of(numberFormat(country.getApprovedCases()), "right"),
              Pair.of(numberFormat(forecast.getCurrent()), "right"),
              Pair.of(numberFormat(forecast.getDescent()), "right"),
              Pair.of(numberFormat(forecast.getRemain()), "right"),
              Pair.of(OUTPUT_DATE_FORMAT.format(forecast.getTargetDate()), "center"));
        })
        .collect(Collectors.toList());
    return String.format(HTML_FORMAT,
        remainDaysThreshold,
        rowList.size(),
        dataBaseService.getCurrentFileName(),
        String.join("", rowList));
  }
}
