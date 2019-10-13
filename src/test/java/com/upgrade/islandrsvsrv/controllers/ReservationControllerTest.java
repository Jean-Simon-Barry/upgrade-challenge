package com.upgrade.islandrsvsrv.controllers;

import com.upgrade.islandrsvsrv.domain.api.ReservationRequest;
import com.upgrade.islandrsvsrv.services.ReservationService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReservationControllerTest {

	@Mock
	private ReservationService reservationService;

	@Rule
	public ExpectedException expectedEx = ExpectedException.none();

	private ReservationController reservationController;


	@Before
	public void setUp() throws Exception {
		reservationController = new ReservationController(reservationService);
	}

	@Test
	public void testInsertReservation() {
		//given
		when(reservationService.insertReservation(any())).thenReturn(1L);
		ReservationRequest reservation = ReservationRequest.builder()
				.userEmail("email")
				.userName("userName")
				.start(LocalDate.now().plus(1, DAYS))
				.end(LocalDate.now().plus(3, DAYS))
				.build();

		//when
		Long actual = reservationController.newReservation(reservation);

		assertThat(actual).isEqualTo(1L);
	}

	@Test
	public void testInsertReservationThrowsStatusExceptionOnIntegrityException() {
		//given
		when(reservationService.insertReservation(any())).thenThrow(new DataIntegrityViolationException(""));
		LocalDate start = LocalDate.now().plus(1, DAYS);
		LocalDate end = LocalDate.now().plus(3, DAYS);
		ReservationRequest reservation = ReservationRequest.builder()
				.userEmail("email")
				.userName("userName")
				.start(start)
				.end(end)
				.build();
		expectedEx.expect(ResponseStatusException.class);
		expectedEx.expectMessage("Sorry it looks like the island is booked somewhere between " +
										 start +
										 " and " +
										 end + "." +
										 " Please try another time slot.");

		//when
		reservationController.newReservation(reservation);

		//then
		//exception is asserted above

	}
}
