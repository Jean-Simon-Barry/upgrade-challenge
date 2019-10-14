package com.upgrade.islandrsvsrv.services;

import com.upgrade.islandrsvsrv.domain.DateInterval;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.time.LocalDate.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;


public class AvailabilityServiceTest {

	private AvailabilityService availabilityService;

	@Before
	public void setUp() throws Exception {
		availabilityService = new AvailabilityService();
	}

	@Test
	public void getAvailableDatesWhenNoReservations() {

		//given
		LocalDate now = now();
		LocalDate tenDaysFromNow = now.plus(10, DAYS);

		//when
		List<LocalDate> availableDates = availabilityService.findAvailabilities(new DateInterval(now, tenDaysFromNow),
																				emptyList());

		//then
		List<LocalDate> expectedDates = now.datesUntil(tenDaysFromNow).collect(toList());
		assertThat(availableDates).containsExactlyElementsOf(expectedDates);
	}

	@Test
	public void getAvailableDatesWhenReservationsAtStartOfRequestedInterval() {

		//given
		LocalDate now = now();
		LocalDate tenDaysFromNow = now.plus(10, DAYS);

		DateInterval reservation = new DateInterval(now, now.plus(2, DAYS));

		//when
		List<LocalDate> availableDates = availabilityService.findAvailabilities(new DateInterval(now, tenDaysFromNow),
																				List.of(reservation));

		//then
		List<LocalDate> expectedDates = now.plus(2, DAYS).datesUntil(tenDaysFromNow).collect(toList());
		assertThat(availableDates).containsExactlyElementsOf(expectedDates);

	}

	@Test
	public void getAvailableDatesWhenReservationsInTheMiddleOfRequestedInterval() {

		//given
		LocalDate now = now();
		LocalDate tenDaysFromNow = now.plus(10, DAYS);

		LocalDate reservationStart = now.plus(2, DAYS);
		LocalDate reservationEnd = now.plus(4, DAYS);
		DateInterval reservation = new DateInterval(reservationStart, reservationEnd);

		//when
		List<LocalDate> availableDates = availabilityService.findAvailabilities(new DateInterval(now, tenDaysFromNow),
																				List.of(reservation));

		//then
		List<LocalDate> firstChunk = now.datesUntil(reservationStart).collect(toList());
		List<LocalDate> secondChunk = reservationEnd.datesUntil(tenDaysFromNow).collect(toList());
		List<LocalDate> expected = new ArrayList<>(firstChunk);
		expected.addAll(secondChunk);
		assertThat(availableDates).containsExactlyElementsOf(expected);

	}

	@Test
	public void getAvailableDatesWhenMultipleReservationsInTheMiddleOfRequestedInterval() {

		//given
		LocalDate now = now();
		LocalDate tenDaysFromNow = now.plus(10, DAYS);

		LocalDate firstReservationStart = now.plus(2, DAYS);
		LocalDate firstReservationEnd = now.plus(4, DAYS);
		LocalDate secondReservationStart = now.plus(4, DAYS);
		LocalDate secondReservationEnd = now.plus(6, DAYS);
		DateInterval firstReservation = new DateInterval(firstReservationStart, firstReservationEnd);
		DateInterval secondReservation = new DateInterval(secondReservationStart, secondReservationEnd);

		//when
		List<LocalDate> availableDates = availabilityService.findAvailabilities(new DateInterval(now, tenDaysFromNow),
																				List.of(firstReservation, secondReservation));

		//then
		List<LocalDate> firstChunk = now.datesUntil(firstReservationStart).collect(toList());
		List<LocalDate> secondChunk = firstReservationEnd.datesUntil(secondReservationStart).collect(toList());
		List<LocalDate> thirdChunk = secondReservationEnd.datesUntil(tenDaysFromNow).collect(toList());
		List<LocalDate> expected = new ArrayList<>(firstChunk);
		expected.addAll(secondChunk);
		expected.addAll(thirdChunk);
		assertThat(availableDates).containsExactlyElementsOf(expected);

	}

	@Test
	public void getAvailabilitiesWhenReservationAtVeryEndOfRequestedInterval() {

		//given
		LocalDate now = now();
		LocalDate tenDaysFromNow = now.plus(10, DAYS);

		LocalDate reservationStart = now.plus(8, DAYS);
		DateInterval reservationDates = new DateInterval(reservationStart, tenDaysFromNow);

		//when
		List<LocalDate> availableDates = availabilityService.findAvailabilities(new DateInterval(now, tenDaysFromNow),
																				List.of(reservationDates));

		//then
		List<LocalDate> availabilityChunk = now.datesUntil(reservationStart).collect(toList());
		assertThat(availableDates).containsExactlyElementsOf(availabilityChunk);
	}

	@Test
	public void testFindsAvailabilitiesWhenReservationStartsBeforeRequestedInterval() {

		//given
		LocalDate now = now();
		LocalDate tenDaysFromNow = now.plus(10, DAYS);

		LocalDate reservationStart = now.minus(1, DAYS);
		LocalDate reservationEnd = now.plus(3, DAYS);
		DateInterval reservationDates = new DateInterval(reservationStart, reservationEnd);

		//when
		List<LocalDate> availableDates = availabilityService.findAvailabilities(new DateInterval(now, tenDaysFromNow),
																				List.of(reservationDates));

		//then
		List<LocalDate> availabilityChunk = reservationEnd.datesUntil(tenDaysFromNow).collect(toList());
		assertThat(availableDates).containsExactlyElementsOf(availabilityChunk);

	}

	@Test
	public void testFindsAvailabilitiesWhenReservationStartsEndsAfterInterval() {

		//given
		LocalDate now = now();
		LocalDate tenDaysFromNow = now.plus(10, DAYS);

		LocalDate reservationStart = now.plus(8, DAYS);
		LocalDate reservationEnd = tenDaysFromNow.plus(1, DAYS);
		DateInterval reservationDates = new DateInterval(reservationStart, reservationEnd);

		//when
		List<LocalDate> availableDates = availabilityService.findAvailabilities(new DateInterval(now, tenDaysFromNow),
																				List.of(reservationDates));

		//then
		List<LocalDate> availabilityChunk = now.datesUntil(reservationStart).collect(toList());
		assertThat(availableDates).containsExactlyElementsOf(availabilityChunk);

	}

	@Test
	public void testWhenReservationOverlapsCompletelyAvailability() {

		//given
		LocalDate now = now();
		LocalDate tenDaysFromNow = now.plus(10, DAYS);

		LocalDate reservationStart = now.minus(1, DAYS);
		LocalDate reservationEnd = tenDaysFromNow.plus(1, DAYS);
		DateInterval reservationDates = new DateInterval(reservationStart, reservationEnd);

		//when
		List<LocalDate> availableDates = availabilityService.findAvailabilities(new DateInterval(now, tenDaysFromNow),
																				List.of(reservationDates));

		//then
		assertThat(availableDates).isEmpty();

	}

	@Test
	public void testReturnsAvailabilityWhenReservationIsOutsideRequestedInterval() {

		//given
		LocalDate now = now();
		LocalDate tenDaysFromNow = now.plus(10, DAYS);

		LocalDate reservationStart = now.plus(20, DAYS);
		LocalDate reservationEnd = tenDaysFromNow.plus(23, DAYS);
		DateInterval reservationDates = new DateInterval(reservationStart, reservationEnd);

		//when
		List<LocalDate> availableDates = availabilityService.findAvailabilities(new DateInterval(now, tenDaysFromNow),
																				List.of(reservationDates));

		//then
		List<LocalDate> expected = now.datesUntil(tenDaysFromNow).collect(toList());
		assertThat(availableDates).containsExactlyElementsOf(expected);

	}
}
