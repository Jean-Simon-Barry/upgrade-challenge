package com.upgrade.islandrsvsrv.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class DateInterval {

	private LocalDate start;
	private LocalDate end;
}
