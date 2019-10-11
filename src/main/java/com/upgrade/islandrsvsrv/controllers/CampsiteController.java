package com.upgrade.islandrsvsrv.controllers;

import com.upgrade.islandrsvsrv.services.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import static java.time.temporal.ChronoUnit.DAYS;

@RestController
@RequestMapping("/campsite")
public class CampsiteController {

	@Autowired
	private ReservationService reservationService;

	@GetMapping("/availabilities")
	public Flux<LocalDate> getAvailabilities(
			@RequestParam(value = "startDate", required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam(value = "startDate", required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

		if (startDate == null || endDate == null) {
			startDate = LocalDate.now();
			endDate = LocalDate.now().plus(30, DAYS);
		}

		return reservationService.getAvailabilities(startDate, endDate);
	}

}
