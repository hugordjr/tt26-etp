package com.estapar.parking.api.dto.response;

import java.math.BigDecimal;

public class SectorResponse {
  private String code;
  private BigDecimal basePrice;
  private Integer maxCapacity;
  private BigDecimal totalRevenue;
  private Long vehicleCount;
  private Double occupancyRate;

  public SectorResponse(
      String code,
      BigDecimal basePrice,
      Integer maxCapacity,
      BigDecimal totalRevenue,
      Long vehicleCount,
      Double occupancyRate) {
    this.code = code;
    this.basePrice = basePrice;
    this.maxCapacity = maxCapacity;
    this.totalRevenue = totalRevenue;
    this.vehicleCount = vehicleCount;
    this.occupancyRate = occupancyRate;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
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

  public BigDecimal getTotalRevenue() {
    return totalRevenue;
  }

  public void setTotalRevenue(BigDecimal totalRevenue) {
    this.totalRevenue = totalRevenue;
  }

  public Long getVehicleCount() {
    return vehicleCount;
  }

  public void setVehicleCount(Long vehicleCount) {
    this.vehicleCount = vehicleCount;
  }

  public Double getOccupancyRate() {
    return occupancyRate;
  }

  public void setOccupancyRate(Double occupancyRate) {
    this.occupancyRate = occupancyRate;
  }
}
