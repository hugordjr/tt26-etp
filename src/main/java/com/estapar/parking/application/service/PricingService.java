package com.estapar.parking.application.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

@Service
public class PricingService {

  public BigDecimal applyDynamicPrice(BigDecimal basePrice, double occupancyRate) {
    BigDecimal factor;
    if (occupancyRate < 0.25) {
      factor = BigDecimal.valueOf(0.9);
    } else if (occupancyRate <= 0.5) {
      factor = BigDecimal.ONE;
    } else if (occupancyRate <= 0.75) {
      factor = BigDecimal.valueOf(1.1);
    } else {
      factor = BigDecimal.valueOf(1.25);
    }
    return basePrice.multiply(factor).setScale(2, RoundingMode.HALF_UP);
  }
}
