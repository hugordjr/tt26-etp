package com.estapar.parking.api.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class RevenueResponse {
  private BigDecimal amount;
  private String currency;
  private OffsetDateTime timestamp;

  public RevenueResponse(BigDecimal amount, String currency, OffsetDateTime timestamp) {
    this.amount = amount;
    this.currency = currency;
    this.timestamp = timestamp;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public OffsetDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(OffsetDateTime timestamp) {
    this.timestamp = timestamp;
  }
}
