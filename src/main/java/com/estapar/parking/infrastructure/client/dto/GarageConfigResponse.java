package com.estapar.parking.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

public class GarageConfigResponse {
  private List<SectorConfig> garage;
  private List<SpotConfig> spots;

  public List<SectorConfig> getGarage() {
    return garage;
  }

  public void setGarage(List<SectorConfig> garage) {
    this.garage = garage;
  }

  public List<SpotConfig> getSpots() {
    return spots;
  }

  public void setSpots(List<SpotConfig> spots) {
    this.spots = spots;
  }

  public static class SectorConfig {
    private String sector;

    @JsonProperty("base_price")
    private BigDecimal basePrice;

    @JsonProperty("max_capacity")
    private Integer maxCapacity;

    public String getSector() {
      return sector;
    }

    public void setSector(String sector) {
      this.sector = sector;
    }

    public BigDecimal getBasePrice() {
      return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
      this.basePrice = basePrice;
    }

    public Integer getMaxCapacity() {
      return maxCapacity;
    }

    public void setMaxCapacity(Integer maxCapacity) {
      this.maxCapacity = maxCapacity;
    }
  }

  public static class SpotConfig {
    private Long id;
    private String sector;
    private Double lat;
    private Double lng;

    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }

    public String getSector() {
      return sector;
    }

    public void setSector(String sector) {
      this.sector = sector;
    }

    public Double getLat() {
      return lat;
    }

    public void setLat(Double lat) {
      this.lat = lat;
    }

    public Double getLng() {
      return lng;
    }

    public void setLng(Double lng) {
      this.lng = lng;
    }
  }
}
