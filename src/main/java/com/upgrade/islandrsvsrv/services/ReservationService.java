package com.upgrade.islandrsvsrv.services;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

@Service
public class ReservationService {

	public Flux<LocalDate> getAvailabilities(LocalDate dateStart, LocalDate dateEnd) {
		return Flux.empty();
	}
}
