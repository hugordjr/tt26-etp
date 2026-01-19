package com.estapar.parking.api.dto.response;

import java.time.OffsetDateTime;

public class SpotResponse {
  private Long id;
  private String sectorCode;
  private Double lat;
  private Double lng;
  private Boolean occupied;
  private VehicleInfo vehicle;

  public SpotResponse(
      Long id, String sectorCode, Double lat, Double lng, Boolean occupied, VehicleInfo vehicle) {
    this.id = id;
    this.sectorCode = sectorCode;
    this.lat = lat;
    this.lng = lng;
    this.occupied = occupied;
    this.vehicle = vehicle;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getSectorCode() {
    return sectorCode;
  }

  public void setSectorCode(String sectorCode) {
    this.sectorCode = sectorCode;
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

  public Boolean getOccupied() {
    return occupied;
  }

  public void setOccupied(Boolean occupied) {
    this.occupied = occupied;
  }

  public VehicleInfo getVehicle() {
    return vehicle;
  }

  public void setVehicle(VehicleInfo vehicle) {
    this.vehicle = vehicle;
  }

  public static class VehicleInfo {
    private String licensePlate;
    private String status;
    private OffsetDateTime entryTime;
    private OffsetDateTime parkedTime;

    public VehicleInfo(String licensePlate, String status, OffsetDateTime entryTime, OffsetDateTime parkedTime) {
      this.licensePlate = licensePlate;
      this.status = status;
      this.entryTime = entryTime;
      this.parkedTime = parkedTime;
    }

    public String getLicensePlate() {
      return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
      this.licensePlate = licensePlate;
    }

    public String getStatus() {
      return status;
    }

    public void setStatus(String status) {
      this.status = status;
    }

    public OffsetDateTime getEntryTime() {
      return entryTime;
    }

    public void setEntryTime(OffsetDateTime entryTime) {
      this.entryTime = entryTime;
    }

    public OffsetDateTime getParkedTime() {
      return parkedTime;
    }

    public void setParkedTime(OffsetDateTime parkedTime) {
      this.parkedTime = parkedTime;
    }
  }
}
