package com.upgrade.islandrsvsrv.controllers;

import com.upgrade.islandrsvsrv.domain.api.ReservationModification;
import com.upgrade.islandrsvsrv.domain.api.ReservationRequest;
import com.upgrade.islandrsvsrv.services.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.Period;

import static com.upgrade.islandrsvsrv.validation.ReservationDateValidation.validateDates;

@RestController
@RequestMapping("/reservation")
@RequiredArgsConstructor
public class ReservationController {

	private final ReservationService reservationService;

	@PostMapping
	public Long newReservation(@RequestBody ReservationRequest reservationRequest) {

		validateDates(reservationRequest.getStart(), reservationRequest.getEnd());
		validateRequestDoesNotExceedThreeDays(reservationRequest.getStart(), reservationRequest.getEnd());
		try {
			return reservationService.insertReservation(reservationRequest);
		} catch (DataIntegrityViolationException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sorry it looks like the island is booked " +
					"somewhere between " + reservationRequest.getStart() + " and " + reservationRequest.getEnd() +
					". Please try another time slot.");

		}
	}

	@PutMapping
	public void modifyReservation(@RequestBody ReservationModification modification) {
		validateDates(modification.getStart(), modification.getEnd());
		validateRequestDoesNotExceedThreeDays(modification.getStart(), modification.getEnd());
		try {
			reservationService.updateReservation(modification);
		} catch (DataIntegrityViolationException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sorry it looks like the island is booked " +
					"somewhere between " + modification.getStart() + " and " + modification.getEnd() +
					". Please try another time slot.");

		}
	}

	@DeleteMapping("/{id}")
	public void deleteReservation(@PathVariable("id") Long reservationId) {
		reservationService.deleteReservation(reservationId);
	}

	private void validateRequestDoesNotExceedThreeDays(LocalDate start, LocalDate end) {
		if (Period.between(start, end).getDays() > 3) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reservation can only be for 3 days at a time.");
		}
	}
}
