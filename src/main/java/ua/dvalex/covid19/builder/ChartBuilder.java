package ua.dvalex.covid19.builder;

import java.io.IOException;
import java.util.Base64;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import ua.dvalex.covid19.domain.ChartSeries;

public class ChartBuilder {

  private final XYChart chart;

  public ChartBuilder() {
    chart = new XYChartBuilder().xAxisTitle("Date").yAxisTitle("Cases").build();
  }

  public void addSeries(String name, ChartSeries chartSeries, boolean showLegend) {
    chart.getStyler().setLegendVisible(showLegend);
    chart.addSeries(name, chartSeries.getDates(), chartSeries.getValues());
  }

  public String build() {
    byte[] bitmapBytes;
    try {
      bitmapBytes = BitmapEncoder.getBitmapBytes(chart, BitmapFormat.JPG);
      return Base64.getEncoder().encodeToString(bitmapBytes);
    } catch (IOException e) {
      e.printStackTrace();
      return "";
    }
  }
}
