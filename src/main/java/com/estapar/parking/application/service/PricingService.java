package com.estapar.parking.application.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PricingService {

  @Value("${pricing.occupancy-rate.threshold-low}")
  private double occupancyRateThresholdLow;

  @Value("${pricing.occupancy-rate.threshold-medium}")
  private double occupancyRateThresholdMedium;

  @Value("${pricing.occupancy-rate.threshold-high}")
  private double occupancyRateThresholdHigh;

  @Value("${pricing.price-factor.low}")
  private BigDecimal priceFactorLow;

  @Value("${pricing.price-factor.normal}")
  private BigDecimal priceFactorNormal;

  @Value("${pricing.price-factor.medium}")
  private BigDecimal priceFactorMedium;

  @Value("${pricing.price-factor.high}")
  private BigDecimal priceFactorHigh;

  @Value("${pricing.decimal-scale}")
  private int decimalScale;

  public BigDecimal applyDynamicPrice(BigDecimal basePrice, double occupancyRate) {
    BigDecimal factor;
    if (occupancyRate == 0.0 || occupancyRate >= occupancyRateThresholdLow) {
      if (occupancyRate <= occupancyRateThresholdMedium) {
        factor = priceFactorNormal;
      } else if (occupancyRate <= occupancyRateThresholdHigh) {
        factor = priceFactorMedium;
      } else {
        factor = priceFactorHigh;
      }
    } else {
      factor = priceFactorLow;
    }
    return basePrice.multiply(factor).setScale(decimalScale, RoundingMode.HALF_UP);
  }
}
