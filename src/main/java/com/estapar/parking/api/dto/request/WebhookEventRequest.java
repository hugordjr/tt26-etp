package com.estapar.parking.api.dto.request;

import com.estapar.parking.domain.enums.EventType;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

@ConditionalValidation
public class WebhookEventRequest {

  @NotBlank private String licensePlate;

  @NotNull private EventType eventType;

  @JsonSerialize(using = OffsetDateTimeSerializer.class)
  @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
  private OffsetDateTime entryTime;

  @JsonSerialize(using = OffsetDateTimeSerializer.class)
  @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
  private OffsetDateTime exitTime;

  private Double lat;
  private Double lng;

  public String getLicensePlate() {
    return licensePlate;
  }

  public void setLicensePlate(String licensePlate) {
    this.licensePlate = licensePlate;
  }

  public EventType getEventType() {
    return eventType;
  }

  public void setEventType(EventType eventType) {
    this.eventType = eventType;
  }

  public OffsetDateTime getEntryTime() {
    return entryTime;
  }

  public void setEntryTime(OffsetDateTime entryTime) {
    this.entryTime = entryTime;
  }

  public OffsetDateTime getExitTime() {
    return exitTime;
  }

  public void setExitTime(OffsetDateTime exitTime) {
    this.exitTime = exitTime;
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
