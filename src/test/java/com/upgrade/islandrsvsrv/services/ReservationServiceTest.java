package com.upgrade.islandrsvsrv.services;

import com.upgrade.islandrsvsrv.domain.DateInterval;
import com.upgrade.islandrsvsrv.repository.ReservationDAO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static java.time.LocalDate.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReservationServiceTest {

	@Mock
	private ReservationDAO reservationDAO;
	private ReservationService reservationService;

	@Before
	public void setUp() throws Exception {
		reservationService = new ReservationService(reservationDAO);
	}

	@Test
	public void getAvailabilities() {
		//given
		LocalDate start = now();
		LocalDate end = start.plus(10, DAYS);
		when(reservationDAO.getAvailabilities(start, end)).thenReturn(Flux.just(new DateInterval(start, end)));

		//when
		Flux<LocalDate> availabilities = reservationService.getAvailabilities(start, end);

		//then
		StepVerifier.create(availabilities)
				.expectNext(start)
				.expectNext(start.plus(1, DAYS))
				.expectNext(start.plus(2, DAYS))
				.expectNext(start.plus(3, DAYS))
				.expectNext(start.plus(4, DAYS))
				.expectNext(start.plus(5, DAYS))
				.expectNext(start.plus(6, DAYS))
				.expectNext(start.plus(7, DAYS))
				.expectNext(start.plus(8, DAYS))
				.expectNext(start.plus(9, DAYS))
				.expectComplete()
				.verify();

	}
}
