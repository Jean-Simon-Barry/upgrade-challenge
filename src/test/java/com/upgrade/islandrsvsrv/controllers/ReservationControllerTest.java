package com.upgrade.islandrsvsrv.controllers;

import com.upgrade.islandrsvsrv.domain.api.ReservationModification;
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
import java.time.format.DateTimeFormatter;

import static java.time.LocalDate.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ReservationControllerTest {

	private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");


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

	@Test
	public void testThrowsExceptionWhenEndDateIsBeforeStartDate() {
		// given
		LocalDate startDate = LocalDate.parse("2019-01-02", dateFormatter);
		LocalDate endDate = LocalDate.parse("2019-01-01", dateFormatter);

		ReservationRequest reservation = ReservationRequest.builder()
				.userEmail("email")
				.userName("userName")
				.start(startDate)
				.end(endDate)
				.build();

		expectedEx.expect(ResponseStatusException.class);
		expectedEx.expectMessage("The end date cannot be before the start date.");

		// when
		reservationController.newReservation(reservation);

		verify(reservationService, never()).insertReservation(any());
	}

	@Test
	public void testThrowsExceptionWhenEndDateIsBeforeNow() {
		// given
		LocalDate startDate = now().minus(2, DAYS);
		LocalDate endDate = now().minus(1, DAYS);
		ReservationRequest reservation = ReservationRequest.builder()
				.userEmail("email")
				.userName("userName")
				.start(startDate)
				.end(endDate)
				.build();
		expectedEx.expect(ResponseStatusException.class);
		expectedEx.expectMessage("The end date cannot be in the past.");

		// when
		reservationController.newReservation(reservation);

		verify(reservationService, never()).insertReservation(any());
	}

	@Test
	public void testThrowsExceptionWhenStartDateIsNotInFuture() {
		// given
		LocalDate startDate = now().minus(2, DAYS);
		LocalDate endDate = now().plus(1, DAYS);
		ReservationRequest reservation = ReservationRequest.builder()
				.userEmail("email")
				.userName("userName")
				.start(startDate)
				.end(endDate)
				.build();
		expectedEx.expect(ResponseStatusException.class);
		expectedEx.expectMessage("The start date must be in the future.");

		// when
		reservationController.newReservation(reservation);

		verify(reservationService, never()).insertReservation(any());
	}

	@Test
	public void testThrowsExceptionIfExceedThreeDays() {
		// given
		LocalDate startDate = now().plus(1, DAYS);
		LocalDate endDate = now().plus(5, DAYS);
		ReservationRequest reservation = ReservationRequest.builder()
				.userEmail("email")
				.userName("userName")
				.start(startDate)
				.end(endDate)
				.build();
		expectedEx.expect(ResponseStatusException.class);
		expectedEx.expectMessage("Reservation can only be for 3 days at a time.");

		// when
		reservationController.newReservation(reservation);

		verify(reservationService, never()).insertReservation(any());
	}

	@Test
	public void testUpdateThrowsExceptionIfExceedThreeDays() {
		// given
		LocalDate startDate = now().plus(1, DAYS);
		LocalDate endDate = now().plus(5, DAYS);
		ReservationModification reservation = ReservationModification.builder()
				.start(startDate)
				.end(endDate)
				.build();
		expectedEx.expect(ResponseStatusException.class);
		expectedEx.expectMessage("Reservation can only be for 3 days at a time.");

		// when
		reservationController.modifyReservation( reservation, 1L);

		verify(reservationService, never()).insertReservation(any());
	}

	@Test
	public void testUpdateThrowsExceptionWhenEndDateIsBeforeStartDate() {
		// given
		LocalDate startDate = LocalDate.parse("2019-01-02", dateFormatter);
		LocalDate endDate = LocalDate.parse("2019-01-01", dateFormatter);

		ReservationModification reservation = ReservationModification.builder()
				.start(startDate)
				.end(endDate)
				.build();

		expectedEx.expect(ResponseStatusException.class);
		expectedEx.expectMessage("The end date cannot be before the start date.");

		// when
		reservationController.modifyReservation(reservation, 1L);

		verify(reservationService, never()).insertReservation(any());
	}

	@Test
	public void testUpdateThrowsExceptionWhenEndDateIsBeforeNow() {
		// given
		LocalDate startDate = now().minus(2, DAYS);
		LocalDate endDate = now().minus(1, DAYS);
		ReservationModification reservation = ReservationModification.builder()
				.start(startDate)
				.end(endDate)
				.build();
		expectedEx.expect(ResponseStatusException.class);
		expectedEx.expectMessage("The end date cannot be in the past.");

		// when
		reservationController.modifyReservation(reservation, 1L);

		verify(reservationService, never()).insertReservation(any());
	}

	@Test
	public void testUpdateThrowsExceptionWhenStartDateIsNotInFuture() {
		// given
		LocalDate startDate = now().minus(2, DAYS);
		LocalDate endDate = now().plus(1, DAYS);
		ReservationModification reservation = ReservationModification.builder()
				.start(startDate)
				.end(endDate)
				.build();
		expectedEx.expect(ResponseStatusException.class);
		expectedEx.expectMessage("The start date must be in the future.");

		// when
		reservationController.modifyReservation(reservation, 1L);

		verify(reservationService, never()).insertReservation(any());
	}

	@Test
	public void testStartAndEndMustDifferByAtLeastOneDay() {
		// given
		LocalDate startDate = now().plus(1, DAYS);
		LocalDate endDate = startDate;
		ReservationModification reservation = ReservationModification.builder()
				.start(startDate)
				.end(endDate)
				.build();
		expectedEx.expect(ResponseStatusException.class);
		expectedEx.expectMessage("The start and end date must differ by at least 1 day.");

		// when
		reservationController.modifyReservation(reservation, 1L);

		verify(reservationService, never()).insertReservation(any());
	}
}
