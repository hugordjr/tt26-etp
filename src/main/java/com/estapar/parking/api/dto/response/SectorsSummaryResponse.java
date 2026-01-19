package com.estapar.parking.api.dto.response;

import java.math.BigDecimal;
import java.util.List;

public class SectorsSummaryResponse {
  private List<SectorResponse> sectors;
  private BigDecimal totalRevenue;
  private Long totalVehicles;
  private Integer totalCapacity;
  private Double overallOccupancyRate;

  public SectorsSummaryResponse(
      List<SectorResponse> sectors,
      BigDecimal totalRevenue,
      Long totalVehicles,
      Integer totalCapacity,
      Double overallOccupancyRate) {
    this.sectors = sectors;
    this.totalRevenue = totalRevenue;
    this.totalVehicles = totalVehicles;
    this.totalCapacity = totalCapacity;
    this.overallOccupancyRate = overallOccupancyRate;
  }

  public List<SectorResponse> getSectors() {
    return sectors;
  }

  public void setSectors(List<SectorResponse> sectors) {
    this.sectors = sectors;
  }

  public BigDecimal getTotalRevenue() {
    return totalRevenue;
  }

  public void setTotalRevenue(BigDecimal totalRevenue) {
    this.totalRevenue = totalRevenue;
  }

  public Long getTotalVehicles() {
    return totalVehicles;
  }

  public void setTotalVehicles(Long totalVehicles) {
    this.totalVehicles = totalVehicles;
  }

  public Integer getTotalCapacity() {
    return totalCapacity;
  }

  public void setTotalCapacity(Integer totalCapacity) {
    this.totalCapacity = totalCapacity;
  }

  public Double getOverallOccupancyRate() {
    return overallOccupancyRate;
  }

  public void setOverallOccupancyRate(Double overallOccupancyRate) {
    this.overallOccupancyRate = overallOccupancyRate;
  }
}
