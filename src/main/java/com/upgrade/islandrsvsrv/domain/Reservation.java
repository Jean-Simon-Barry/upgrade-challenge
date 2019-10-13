package com.upgrade.islandrsvsrv.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Reservation {

	private Long id;
	private DateInterval dateInterval;
	private String userName;
	private String userEmail;
}
