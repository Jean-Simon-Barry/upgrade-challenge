package com.upgrade.islandrsvsrv.services;

import com.upgrade.islandrsvsrv.domain.DateInterval;
import com.upgrade.islandrsvsrv.domain.api.ReservationModification;
import com.upgrade.islandrsvsrv.domain.api.ReservationRequest;
import com.upgrade.islandrsvsrv.repository.ReservationDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ReservationService {

	private final ReservationDAO reservationDAO;

	private final AvailabilityService availabilityService;

	public Flux<LocalDate> getAvailabilities(LocalDate dateStart, LocalDate dateEnd) {
		return Flux.fromIterable(availabilityService.findAvailabilities(new DateInterval(dateStart, dateEnd),
																		reservationDAO.getReservationDates(dateStart, dateEnd)));
	}

	public Long insertReservation(ReservationRequest reservationRequest) throws DataIntegrityViolationException {
		return reservationDAO.insertReservation(reservationRequest);
	}

	public void updateReservation(long reservationId, ReservationModification modification) {
		reservationDAO.updateReservation(reservationId, modification);
	}

	public void deleteReservation(long reservationId) {
		reservationDAO.deleteReservation(reservationId);
	}
}
