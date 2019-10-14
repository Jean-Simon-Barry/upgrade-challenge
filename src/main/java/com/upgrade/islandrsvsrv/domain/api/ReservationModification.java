package com.upgrade.islandrsvsrv.domain.api;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReservationModification {

  private LocalDate start;
  private LocalDate end;
}
