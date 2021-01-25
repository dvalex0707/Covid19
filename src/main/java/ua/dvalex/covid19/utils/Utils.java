package ua.dvalex.covid19.utils;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Utils {

  public static final SimpleDateFormat OUTPUT_DATE_FORMAT = new SimpleDateFormat("dd.MM.yy");

  public static String numberFormat(int number) {
    String s = String.valueOf(number);
    for (int c : Arrays.asList(6, 3)) {
      int p = s.length() - c;
      if (p > 0) {
        s = s.substring(0, p) + " " + s.substring(p);
      }
    }
    return s;
  }

  public static String createTableRow(Pair... elements) {
    return "<tr>"
        + Arrays.stream(elements)
        .map(pair -> {
          String classPart = pair.getRight() == null
              ? "" : String.format(" class=\"%s\"", pair.getRight());
          return String.format("<td%s>%s", classPart, pair.getLeft());
        })
        .collect(Collectors.joining("</td>"))
        + "</td></tr>";
  }
}
