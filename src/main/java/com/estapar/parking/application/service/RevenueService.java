package com.estapar.parking.application.service;

import com.estapar.parking.api.dto.response.RevenueResponse;
import com.estapar.parking.domain.entity.Revenue;
import com.estapar.parking.domain.entity.Sector;
import com.estapar.parking.domain.exception.BusinessException;
import com.estapar.parking.infrastructure.repository.RevenueRepository;
import com.estapar.parking.infrastructure.repository.SectorRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RevenueService {

  private static final Logger log = LoggerFactory.getLogger(RevenueService.class);

  private final RevenueRepository revenueRepository;
  private final SectorRepository sectorRepository;

  public RevenueService(RevenueRepository revenueRepository, SectorRepository sectorRepository) {
    this.revenueRepository = revenueRepository;
    this.sectorRepository = sectorRepository;
  }

  @Transactional
  public void registerRevenue(Sector sector, LocalDate date, BigDecimal amount) {
    if (amount == null) {
      amount = BigDecimal.ZERO;
    }
    Revenue revenue = new Revenue();
    revenue.setSector(sector);
    revenue.setDate(date);
    revenue.setAmount(amount);
    revenue.setCurrency("BRL");
    revenue.setTimestamp(OffsetDateTime.now());
    revenueRepository.save(revenue);
    log.info(
        "receita registrada setor {} data {} valor {}",
        sector.getCode(),
        date,
        amount);
  }

  @Transactional(readOnly = true)
  public RevenueResponse queryRevenue(LocalDate date, String sectorCode) {
    Long sectorId = null;
    if (sectorCode != null && !sectorCode.isBlank()) {
      Sector sector =
          sectorRepository
              .findByCode(sectorCode)
              .orElseThrow(() -> new BusinessException("setor nao encontrado"));
      sectorId = sector.getId();
    }

    BigDecimal amount = revenueRepository.sumByFilters(sectorId, date);
    OffsetDateTime timestamp =
        revenueRepository
            .findFirstByOrderByTimestampDesc()
            .map(Revenue::getTimestamp)
            .orElse(OffsetDateTime.now());

    return new RevenueResponse(amount, "BRL", timestamp);
  }
}
