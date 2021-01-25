package ua.dvalex.covid19.service;

import static ua.dvalex.covid19.Const.DATE_FORMAT;
import static ua.dvalex.covid19.Const.HISTORY_FILE;
import static ua.dvalex.covid19.Const.TABLES_FOLDER;

import au.com.bytecode.opencsv.CSVReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.stereotype.Service;
import ua.dvalex.covid19.domain.Country;
import ua.dvalex.covid19.domain.DataBase;
import ua.dvalex.covid19.domain.RecordType;

@Service
public class DataBaseService {

  @Getter
  private String currentFileName;

  @PostConstruct
  private void init() {
    if (!TABLES_FOLDER.exists()) {
      //noinspection ResultOfMethodCallIgnored
      TABLES_FOLDER.mkdirs();
    }
  }

  public DataBase load() {
    currentFileName = extractCurrentFileName();
    DataBase dataBase = new DataBase();
    List<Country> countries = dataBase.getCountries();
    Set<String> countrySet = new HashSet<>();
    try (CSVReader reader = new CSVReader(new FileReader(new File(TABLES_FOLDER, currentFileName)),
        ',', '"', 1)) {
      Country currentCountry = null;
      String[] line;
      while ((line = reader.readNext()) != null) {
        addToMap(dataBase, line);
        String countryName = line[0];
        RecordType recordType = RecordType.get(line[1]);
        int value = Integer.parseInt(line[3]);
        if (!countrySet.contains(countryName)) {
          currentCountry = new Country(countryName, "", 0);
          countries.add(currentCountry);
          countrySet.add(countryName);
        }
        if (recordType == RecordType.TOTAL_CASES && value > currentCountry.getApprovedCases()) {
          currentCountry.setApprovedCases(value);
        }
      }
      return dataBase;
    } catch (IOException e) {
      throw new RuntimeException();
    }
  }

  private String extractCurrentFileName() {
    try {
      return Files.readAllLines(HISTORY_FILE.toPath()).get(0);
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException();
    }
  }

  private void addToMap(DataBase dataBase, String[] line) {
    try {
      String countryName = line[0];
      RecordType recordType = RecordType.get(line[1]);
      Date date = DATE_FORMAT.parse(line[2]);
      int value = Integer.parseInt(line[3]);
      dataBase.put(recordType, countryName, date, value);
    } catch (ParseException e) {
      throw new RuntimeException();
    }
  }
}
