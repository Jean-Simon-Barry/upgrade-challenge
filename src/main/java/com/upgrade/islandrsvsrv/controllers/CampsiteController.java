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

import static java.time.LocalDate.now;
import static java.time.temporal.ChronoUnit.DAYS;

@RestController
@RequestMapping("/campsite")
@RequiredArgsConstructor
public class CampsiteController {

	private static final int DEFAULT_AVAILABILITY_WINDOW_DAYS = 30;

	private final ReservationService reservationService;

	@GetMapping("/availabilities")
	public Flux<LocalDate> getAvailabilities(
			@RequestParam(value = "startDate", required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam(value = "startDate", required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

		if (startDate == null || endDate == null) {
			startDate = now();
			endDate = now().plus(DEFAULT_AVAILABILITY_WINDOW_DAYS, DAYS);
		}
		validateDates(startDate, endDate);

		return reservationService.getAvailabilities(startDate, endDate);
	}

	private void validateDates(LocalDate start, LocalDate end) throws ResponseStatusException {
		if (end.isBefore(start)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The end date cannot be before the start date.");
		} else if (end.isBefore(now())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The end date cannot be in the past.");
		}
	}

}
