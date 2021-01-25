package ua.dvalex.covid19.domain;

import java.util.Date;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Forecast {

  private final int current;
  private final int descent;
  private final int remain;
  private final Date targetDate;
}
