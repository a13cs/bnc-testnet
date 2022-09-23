package example;

import java.util.Objects;

public class WeatherData {

  private Integer temperatureK;
  private Integer windKmh;
  private Double humidityPct;
  private Integer pressureHPa;

  public Integer getTemperatureK() {
    return temperatureK;
  }

  public void setTemperatureK(Integer temperatureK) {
    this.temperatureK = temperatureK;
  }

  public Integer getWindKmh() {
    return windKmh;
  }

  public void setWindKmh(Integer windKmh) {
    this.windKmh = windKmh;
  }

  public Double getHumidityPct() {
    return humidityPct;
  }

  public void setHumidityPct(Double humidityPct) {
    this.humidityPct = humidityPct;
  }

  public Integer getPressureHPa() {
    return pressureHPa;
  }

  public void setPressureHPa(Integer pressureHPa) {
    this.pressureHPa = pressureHPa;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    WeatherData that = (WeatherData) o;
    return Objects.equals(temperatureK, that.temperatureK) && Objects.equals(windKmh, that.windKmh) && Objects.equals(humidityPct, that.humidityPct) && Objects.equals(pressureHPa, that.pressureHPa);
  }

  @Override
  public int hashCode() {
    return Objects.hash(temperatureK, windKmh, humidityPct, pressureHPa);
  }
}