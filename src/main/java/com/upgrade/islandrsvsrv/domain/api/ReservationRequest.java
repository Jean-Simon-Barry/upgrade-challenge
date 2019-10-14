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
public class ReservationRequest {

  private LocalDate start;
  private LocalDate end;
  private String userName;
  private String userEmail;

}
