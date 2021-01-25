package ua.dvalex.covid19.domain;

import java.util.Date;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CountryDataRecord {

  private RecordType recordType;
  private Date date;
  private int value;
}
