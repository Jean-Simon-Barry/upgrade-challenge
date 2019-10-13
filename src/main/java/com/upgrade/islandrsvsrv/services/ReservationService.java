package com.upgrade.islandrsvsrv.services;

import com.upgrade.islandrsvsrv.domain.Reservation;
import com.upgrade.islandrsvsrv.domain.api.ReservationRequest;
import com.upgrade.islandrsvsrv.repository.ReservationDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class ReservationService {

	private final ReservationDAO reservationDAO;

	public Flux<LocalDate> getAvailabilities(LocalDate dateStart, LocalDate dateEnd) {
		return reservationDAO.getAvailabilities(dateStart, dateEnd)
				.flatMap(dateInterval -> Flux.fromIterable(dateInterval.getStart()
																   .datesUntil(dateInterval.getEnd())
																   .collect(toList())
						 ));
	}
}
