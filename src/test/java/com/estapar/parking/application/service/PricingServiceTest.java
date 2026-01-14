package com.estapar.parking.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class PricingServiceTest {

  private final PricingService pricingService = new PricingService();

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
  void deveAplicarAumentoParaOcupacaoAlta() {
    var price = pricingService.applyDynamicPrice(BigDecimal.TEN, 0.8);
    assertThat(price).isEqualByComparingTo("12.50");
  }
}
