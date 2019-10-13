package com.upgrade.islandrsvsrv.domain.api;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;


@Data
@Builder
public class ReservationRequest {

	private LocalDate start;
	private LocalDate end;
	private String userName;
	private String userEmail;

}
