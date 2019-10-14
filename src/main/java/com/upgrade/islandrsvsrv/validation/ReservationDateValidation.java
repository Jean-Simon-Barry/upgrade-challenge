package com.upgrade.islandrsvsrv.validation;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import static java.time.LocalDate.now;

public class ReservationDateValidation {

	public static void validateDates(LocalDate start, LocalDate end) throws ResponseStatusException {
		if (end.isBefore(start)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The end date cannot be before the start date.");
		} else if (end.isBefore(now())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The end date cannot be in the past.");
		} else if (!start.isAfter(now())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The start date must be in the future.");
		} else if (start.equals(end)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The start and end date must differ by " +
					"at least 1 day.");
		}
	}
}
