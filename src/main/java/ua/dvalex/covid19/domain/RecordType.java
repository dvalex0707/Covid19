package ua.dvalex.covid19.domain;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;

public enum RecordType {
  TOTAL_CASES("totalCases"),
  DAILY_CASES("dailyCases"),
  ACTIVE_CASES("activeCases"),
  TOTAL_DEATHS("totalDeaths"),
  DAILY_DEATHS("dailyDeaths");

  private static final Map<String, RecordType> map = Arrays.stream(RecordType.values())
      .collect(Collectors.toMap(RecordType::getType, o -> o));

  @Getter
  private final String type;

  RecordType(String type) {
    this.type = type;
  }

  public static RecordType get(String type) {
    return map.get(type);
  }
}
