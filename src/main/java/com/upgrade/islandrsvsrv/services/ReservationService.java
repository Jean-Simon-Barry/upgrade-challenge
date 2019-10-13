package com.upgrade.islandrsvsrv.services;

import com.upgrade.islandrsvsrv.domain.Reservation;
import com.upgrade.islandrsvsrv.domain.api.ReservationModification;
import com.upgrade.islandrsvsrv.domain.api.ReservationRequest;
import com.upgrade.islandrsvsrv.repository.ReservationDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class ReservationService {

	private final ReservationDAO reservationDAO;

	public Flux<LocalDate> getAvailabilities(LocalDate dateStart, LocalDate dateEnd) {
		return reservationDAO.getAvailabilities(dateStart, dateEnd)
				.flatMap(dateInterval -> Flux.fromIterable(dateInterval.getStart()
																   .datesUntil(dateInterval.getEnd().plus(1, DAYS))
																   .collect(toList())
						 ));
	}

	public Long insertReservation(ReservationRequest reservationRequest) throws DataIntegrityViolationException {
		return reservationDAO.insertReservation(reservationRequest);
	}

	public void updateReservation(ReservationModification modification) {
		reservationDAO.updateReservation(modification);
	}

	public void deleteReservation(long reservationId) {
		reservationDAO.deleteReservation(reservationId);
	}
}
