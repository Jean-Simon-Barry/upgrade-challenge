package com.upgrade.islandrsvsrv.controllers;

import com.upgrade.islandrsvsrv.domain.api.ReservationRequest;
import com.upgrade.islandrsvsrv.services.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/reservation")
@RequiredArgsConstructor
public class ReservationController {

	private final ReservationService reservationService;

	@PostMapping("/")
	public Long newReservation(@RequestBody ReservationRequest reservationRequest) {
		try {
			return reservationService.insertReservation(reservationRequest);
		} catch (DataIntegrityViolationException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sorry it looks like the island is booked " +
					"somewhere between " + reservationRequest.getStart() + " and " + reservationRequest.getEnd() +
					". Please try another time slot.");

		}
	}
}
