package com.estapar.parking.domain.entity;

import com.estapar.parking.domain.enums.VehicleStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "vehicles")
public class Vehicle {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 15)
  private String licensePlate;

  @ManyToOne(optional = false)
  @JoinColumn(name = "sector_id")
  private Sector sector;

  @ManyToOne(optional = false)
  @JoinColumn(name = "spot_id")
  private Spot spot;

  @Column(nullable = false)
  private OffsetDateTime entryTime;

  private OffsetDateTime parkedTime;

  private OffsetDateTime exitTime;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal adjustedPrice;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private VehicleStatus status = VehicleStatus.ENTRY;

  public Long getId() {
    return id;
  }

  public String getLicensePlate() {
    return licensePlate;
  }

  public void setLicensePlate(String licensePlate) {
    this.licensePlate = licensePlate;
  }

  public Sector getSector() {
    return sector;
  }

  public void setSector(Sector sector) {
    this.sector = sector;
  }

  public Spot getSpot() {
    return spot;
  }

  public void setSpot(Spot spot) {
    this.spot = spot;
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

  public OffsetDateTime getExitTime() {
    return exitTime;
  }

  public void setExitTime(OffsetDateTime exitTime) {
    this.exitTime = exitTime;
  }

  public BigDecimal getAdjustedPrice() {
    return adjustedPrice;
  }

  public void setAdjustedPrice(BigDecimal adjustedPrice) {
    this.adjustedPrice = adjustedPrice;
  }

  public VehicleStatus getStatus() {
    return status;
  }

  public void setStatus(VehicleStatus status) {
    this.status = status;
  }
}
