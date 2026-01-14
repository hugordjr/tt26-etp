package com.estapar.parking.api.dto.response;

import java.time.OffsetDateTime;

public class HealthResponse {
  private String status;
  private OffsetDateTime timestamp;
  private ComponentStatus application;
  private ComponentStatus database;
  private ComponentStatus simulator;

  public HealthResponse() {}

  public HealthResponse(
      String status,
      OffsetDateTime timestamp,
      ComponentStatus application,
      ComponentStatus database,
      ComponentStatus simulator) {
    this.status = status;
    this.timestamp = timestamp;
    this.application = application;
    this.database = database;
    this.simulator = simulator;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public OffsetDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(OffsetDateTime timestamp) {
    this.timestamp = timestamp;
  }

  public ComponentStatus getApplication() {
    return application;
  }

  public void setApplication(ComponentStatus application) {
    this.application = application;
  }

  public ComponentStatus getDatabase() {
    return database;
  }

  public void setDatabase(ComponentStatus database) {
    this.database = database;
  }

  public ComponentStatus getSimulator() {
    return simulator;
  }

  public void setSimulator(ComponentStatus simulator) {
    this.simulator = simulator;
  }

  public static class ComponentStatus {
    private String status;
    private String message;

    public ComponentStatus() {}

    public ComponentStatus(String status, String message) {
      this.status = status;
      this.message = message;
    }

    public String getStatus() {
      return status;
    }

    public void setStatus(String status) {
      this.status = status;
    }

    public String getMessage() {
      return message;
    }

    public void setMessage(String message) {
      this.message = message;
    }
  }
}
