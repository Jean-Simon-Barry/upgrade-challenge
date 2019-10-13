package com.upgrade.islandrsvsrv.controllers;

import com.upgrade.islandrsvsrv.services.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

import static com.upgrade.islandrsvsrv.validation.ReservationDateValidation.validateDates;
import static java.time.LocalDate.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MONTHS;

@RestController
@RequestMapping("/campsite")
@RequiredArgsConstructor
public class CampsiteController {

	private static final int DEFAULT_AVAILABILITY_WINDOW_MONTHS = 1;

	private final ReservationService reservationService;

	@GetMapping("/availabilities")
	public Flux<LocalDate> getAvailabilities(
			@RequestParam(value = "startDate", required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam(value = "endDate", required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

		if (startDate == null || endDate == null) {
			LocalDate tomorrow = now().plus(1, DAYS);
			startDate = tomorrow;
			endDate = tomorrow.plus(DEFAULT_AVAILABILITY_WINDOW_MONTHS, MONTHS);
		}
		validateDates(startDate, endDate);

		return reservationService.getAvailabilities(startDate, endDate);
	}

}
