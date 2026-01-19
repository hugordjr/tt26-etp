package com.estapar.parking.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class PricingServiceTest {

  private PricingService pricingService;

  @BeforeEach
  void setup() {
    pricingService = new PricingService();
    ReflectionTestUtils.setField(pricingService, "occupancyRateThresholdLow", 0.25);
    ReflectionTestUtils.setField(pricingService, "occupancyRateThresholdMedium", 0.5);
    ReflectionTestUtils.setField(pricingService, "occupancyRateThresholdHigh", 0.75);
    ReflectionTestUtils.setField(pricingService, "priceFactorLow", new BigDecimal("0.9"));
    ReflectionTestUtils.setField(pricingService, "priceFactorNormal", new BigDecimal("1.0"));
    ReflectionTestUtils.setField(pricingService, "priceFactorMedium", new BigDecimal("1.1"));
    ReflectionTestUtils.setField(pricingService, "priceFactorHigh", new BigDecimal("1.25"));
    ReflectionTestUtils.setField(pricingService, "decimalScale", 2);
  }

  @Test
  void deveAplicarDescontoParaOcupacaoBaixa() {
    var price = pricingService.applyDynamicPrice(BigDecimal.TEN, 0.2);
    assertThat(price).isEqualByComparingTo("9.00");
  }

  @Test
  void deveManterPrecoParaOcupacaoMedia() {
    var price = pricingService.applyDynamicPrice(BigDecimal.TEN, 0.5);
    assertThat(price).isEqualByComparingTo("10.00");
  }

  @Test
  void deveAplicarAumentoParaOcupacaoMediaAlta() {
    var price = pricingService.applyDynamicPrice(BigDecimal.TEN, 0.6);
    assertThat(price).isEqualByComparingTo("11.00");
  }

  @Test
  void deveAplicarAumentoParaOcupacaoMediaAltaNoLimite() {
    var price = pricingService.applyDynamicPrice(BigDecimal.TEN, 0.75);
    assertThat(price).isEqualByComparingTo("11.00");
  }

  @Test
  void deveAplicarAumentoParaOcupacaoAlta() {
    var price = pricingService.applyDynamicPrice(BigDecimal.TEN, 0.8);
    assertThat(price).isEqualByComparingTo("12.50");
  }

  @Test
  void deveAplicarAumentoParaOcupacaoTotal() {
    var price = pricingService.applyDynamicPrice(BigDecimal.TEN, 1.0);
    assertThat(price).isEqualByComparingTo("12.50");
  }
}
