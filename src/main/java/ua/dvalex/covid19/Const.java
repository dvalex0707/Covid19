package ua.dvalex.covid19;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.File;
import java.text.SimpleDateFormat;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Const {

  public static final File DATA_FOLDER = new File("data");
  public static final File TABLES_FOLDER = new File(DATA_FOLDER, "tables");
  public static final File HISTORY_FILE = new File(TABLES_FOLDER, "history.txt");
  public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
}
