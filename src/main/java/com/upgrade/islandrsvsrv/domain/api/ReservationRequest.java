package com.upgrade.islandrsvsrv.domain.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


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
