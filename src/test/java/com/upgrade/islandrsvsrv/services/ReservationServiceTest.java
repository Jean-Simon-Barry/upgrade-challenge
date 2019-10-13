package com.upgrade.islandrsvsrv.services;

import com.upgrade.islandrsvsrv.domain.DateInterval;
import com.upgrade.islandrsvsrv.domain.api.ReservationRequest;
import com.upgrade.islandrsvsrv.repository.ReservationDAO;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.dao.DataIntegrityViolationException;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.Optional;

import static java.time.LocalDate.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReservationServiceTest {

	@Mock
	private ReservationDAO reservationDAO;
	private ReservationService reservationService;

	@Rule
	public ExpectedException expectedEx = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		reservationService = new ReservationService(reservationDAO);
	}

	@Test
	public void getAvailabilitiesSearchesInclusiveEndDate() {
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
				.expectNext(start.plus(10, DAYS))
				.expectComplete()
				.verify();

	}

	@Test
	public void testInsertReservation() {

		//given
		LocalDate start = now();
		LocalDate end = now().plus(10, DAYS);
		ReservationRequest reservation = ReservationRequest.builder()
				.userEmail("email")
				.userName("userName")
				.start(start)
				.end(end)
				.build();
		when(reservationDAO.insertReservation(reservation)).thenReturn(1L);

		//when
		Long reservationId = reservationService.insertReservation(reservation);

		//then
		assertThat(reservationId).isEqualTo(1L);

	}

	@Test
	public void testInsertReservationBubblesUpException() {

		//given
		LocalDate start = now();
		LocalDate end = now().plus(10, DAYS);
		ReservationRequest reservation = ReservationRequest.builder()
				.userEmail("email")
				.userName("userName")
				.start(start)
				.end(end)
				.build();
		when(reservationDAO.insertReservation(reservation)).thenThrow(new DataIntegrityViolationException("error"));

		expectedEx.expect(DataIntegrityViolationException.class);
		expectedEx.expectMessage("error");

		//when
		reservationService.insertReservation(reservation);

	}
}
