package ua.dvalex.covid19.service;

import static ua.dvalex.covid19.utils.Utils.OUTPUT_DATE_FORMAT;
import static ua.dvalex.covid19.utils.Utils.createTableRow;
import static ua.dvalex.covid19.utils.Utils.numberFormat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import ua.dvalex.covid19.domain.ChartSeries;
import ua.dvalex.covid19.domain.Country;
import ua.dvalex.covid19.domain.DataBase;
import ua.dvalex.covid19.domain.RecordType;

@Service
public class RecoveredAndReopenedService {

  private static final String RECOVERED_OUTPUT_FORMAT =
      "<h2>Completely recovered (%d countries):</h2>"
          + "<table><thead><th>Country</th><th>Initial</th>"
          + "<th>Completely recovered</th></thead>%s</table>";

  private static final String REOPENED_OUTPUT_FORMAT =
      "<hr /><h2>Recently reopened (%d countries):</h2>"
          + "<table><thead><th>Country</th><th>Initial</th><th>Current</th>"
          + "<th>Recovered</th><th>Reopened</th></thead>%s</table>";

  private static final String NO_RECENTLY_REOPENED = "<p>There are no recently reopened countries</p>";

  public String output(DataBase dataBase) {
    List<Country> countries = dataBase.getCountries().stream()
        .peek(country -> discoverySeries(country,
            dataBase.get(RecordType.ACTIVE_CASES, country.getName())))
        .filter(country -> country.getRecovered() != null || country.getReopened() != null)
        .collect(Collectors.toList());

    List<String> recovered = countries.stream()
        .filter(country -> country.getRemainCases() <= 0)
        .map(country -> createTableRow(
            Pair.of(country.getName(), null),
            Pair.of(numberFormat(country.getApprovedCases()), "right"),
            dateWithClass(country.getRecovered())))
        .collect(Collectors.toList());

    List<String> reopened = countries.stream()
        .filter(country -> country.getReopened() != null)
        .map(country -> createTableRow(
            Pair.of(country.getName(), null),
            Pair.of(numberFormat(country.getApprovedCases()), "right"),
            Pair.of(numberFormat(country.getRemainCases()), "right"),
            Pair.of(OUTPUT_DATE_FORMAT.format(country.getRecovered()), "center"),
            Pair.of(OUTPUT_DATE_FORMAT.format(country.getReopened()), "center")))
        .collect(Collectors.toList());

    StringBuilder result = new StringBuilder(String.format(RECOVERED_OUTPUT_FORMAT,
        recovered.size(),
        String.join("", recovered)));

    result.append(reopened.isEmpty()
        ? NO_RECENTLY_REOPENED
        : String.format(REOPENED_OUTPUT_FORMAT,
            reopened.size(),
            String.join("", reopened)));

    return result.toString();
  }

  private void discoverySeries(Country country, ChartSeries chartSeries) {
    if (chartSeries != null) {
      List<Date> dates = chartSeries.getDates();
      List<Integer> values = chartSeries.getValues();

      Integer remain = values.get(values.size() - 1);
      country.setRemainCases(remain);

      if (remain <= 0) {
        traverseBack(values, values.size(), false, values.size())
            .map(dates::get)
            .ifPresent(country::setRecovered);
      } else {
        traverseBack(values, values.size(), true, 3)
            .ifPresent(reopenedPos -> traverseBack(values, reopenedPos, false, values.size())
                .ifPresent(recoveredPos -> {
                  country.setReopened(dates.get(reopenedPos));
                  country.setRecovered(dates.get(recoveredPos));
                }));
      }
    }
  }

  private Optional<Integer> traverseBack(List<Integer> list, int startPos, boolean lookForZero,
      int maxSteps) {
    while (startPos > 0 && maxSteps > 0) {
      startPos--;
      maxSteps--;
      if (lookForZero == (list.get(startPos) <= 0)) {
        return Optional.of(startPos + 1);
      }
    }
    return Optional.empty();
  }

  private Pair dateWithClass(Date recovered) {
    long days = ChronoUnit.DAYS.between(recovered.toInstant(), Instant.now());
    String dateClass = "center";
    if (days <= 1) {
      dateClass = "center red";
    } else if (days <= 3) {
      dateClass = "center blue";
    }
    return Pair.of(OUTPUT_DATE_FORMAT.format(recovered), dateClass);
  }
}
