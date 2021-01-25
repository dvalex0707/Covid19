package ua.dvalex.covid19;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.dvalex.covid19.service.ChartsPageService;
import ua.dvalex.covid19.service.ForecastService;

@RestController
public class Covid19Controller {

  private final ChartsPageService chartsPageService;
  private final ForecastService forecastService;

  public Covid19Controller(
      ChartsPageService chartsPageService,
      ForecastService forecastService) {
    this.chartsPageService = chartsPageService;
    this.forecastService = forecastService;
  }

  @GetMapping("/active")
  public String active() {
    return chartsPageService.active(100);
  }

  @GetMapping("/forecast")
  public String activeForecast() {
    return forecastService.forecast(100, 30);
  }
}
