package ua.dvalex.covid19.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
public
class ChartSeries {

  private final List<Date> dates = new ArrayList<>();

  private final List<Integer> values = new ArrayList<>();
}
