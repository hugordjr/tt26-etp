package com.estapar.parking.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.estapar.parking.api.dto.response.RevenueResponse;
import com.estapar.parking.domain.entity.Sector;
import com.estapar.parking.infrastructure.repository.RevenueRepository;
import com.estapar.parking.infrastructure.repository.SectorRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RevenueServiceTest {

  @Mock private RevenueRepository revenueRepository;
  @Mock private SectorRepository sectorRepository;

  @InjectMocks private RevenueService revenueService;

  @Test
  void deveSomarReceitaPorFiltro() {
    Sector sector = new Sector();
    sector.setCode("A");
    sector.setMaxCapacity(100);
    sector.setBasePrice(BigDecimal.TEN);

    when(sectorRepository.findByCode("A")).thenReturn(Optional.of(sector));
    when(revenueRepository.sumByFilters(null, LocalDate.of(2025, 1, 1)))
        .thenReturn(new BigDecimal("30.00"));
    when(revenueRepository.findFirstByOrderByTimestampDesc())
        .thenReturn(Optional.ofNullable(null));

    RevenueResponse response =
        revenueService.queryRevenue(LocalDate.of(2025, 1, 1), "A");

    assertThat(response.getAmount()).isEqualByComparingTo("30.00");
    assertThat(response.getCurrency()).isEqualTo("BRL");
  }
}
