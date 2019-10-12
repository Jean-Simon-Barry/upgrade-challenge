package com.upgrade.islandrsvsrv.domain;

import lombok.Builder;
import lombok.Data;

import java.time.Period;

@Data
@Builder
public class Reservation {

	private Long id;
	private Period reservationDates;
	private CampingUser campingUser;
}
