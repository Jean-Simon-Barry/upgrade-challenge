package com.upgrade.islandrsvsrv.controllers;


import com.upgrade.islandrsvsrv.services.ReservationService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import static java.time.LocalDate.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CampsiteControllerTest {

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	@Rule
	public ExpectedException expectedEx = ExpectedException.none();

	@Mock
	private ReservationService reservationService;

    private CampsiteController campsiteController;

    @Before
    public void setUp() {
		campsiteController = new CampsiteController(reservationService);
    }

    @Test
    public void testReturnsAvailabilitiesForCampsite() {
        // given
        LocalDate requestStartDate = now();
        LocalDate requestEndDate = now().plus(1, DAYS);

		LocalDate availabilityOne = LocalDate.parse("2019-01-01", dateFormatter);
		LocalDate availabilityTwo = LocalDate.parse("2019-01-02", dateFormatter);
		LocalDate availabilityThree = LocalDate.parse("2019-01-03", dateFormatter);
		when(reservationService.getAvailabilities(requestStartDate, requestEndDate))
				.thenReturn(Flux.just(availabilityOne, availabilityTwo, availabilityThree));


		// when
        Flux<LocalDate> actualAvailabilities = campsiteController.getAvailabilities(requestStartDate, requestEndDate);

        // then
		StepVerifier.create(actualAvailabilities)
				.recordWith(ArrayList::new)
				.expectNextCount(3)
				.consumeRecordedWith(availabilities -> assertThat(availabilities, hasItems(availabilityOne,
																						   availabilityTwo,
																						   availabilityThree)))
				.verifyComplete();

		verify(reservationService).getAvailabilities(requestStartDate, requestEndDate);
    }

	@Test
	public void testReturnsAvailabilitiesForCampsiteUsingDefaultValueIfParamsAreNull() {
		// given
		LocalDate availabilityOne = LocalDate.parse("2019-01-01", dateFormatter);
		LocalDate availabilityTwo = LocalDate.parse("2019-01-02", dateFormatter);
		LocalDate availabilityThree = LocalDate.parse("2019-01-03", dateFormatter);
		when(reservationService.getAvailabilities(any(), any()))
				.thenReturn(Flux.just(availabilityOne, availabilityTwo, availabilityThree));


		// when
		Flux<LocalDate> actualAvailabilities = campsiteController.getAvailabilities(null, null);

		// then
		StepVerifier.create(actualAvailabilities)
				.recordWith(ArrayList::new)
				.expectNextCount(3)
				.consumeRecordedWith(availabilities -> assertThat(availabilities, hasItems(availabilityOne,
																						   availabilityTwo,
																						   availabilityThree)))
				.verifyComplete();

		verify(reservationService).getAvailabilities(any(), any());
	}

	@Test
	public void testThrowsExceptionWhenEndDateIsBeforeStartDate() {
		// given
		LocalDate startDate = LocalDate.parse("2019-01-02", dateFormatter);
		LocalDate endDate = LocalDate.parse("2019-01-01", dateFormatter);
		expectedEx.expect(ResponseStatusException.class);
		expectedEx.expectMessage("The end date cannot be before the start date.");

		// when
		campsiteController.getAvailabilities(startDate, endDate);

		verify(reservationService, never()).getAvailabilities(any(), any());
	}

	@Test
	public void testThrowsExceptionWhenEndDateIsBeforeNow() {
		// given
		LocalDate startDate = now().minus(2, DAYS);
		LocalDate endDate = now().minus(1, DAYS);
		expectedEx.expect(ResponseStatusException.class);
		expectedEx.expectMessage("The end date cannot be in the past.");

		// when
		campsiteController.getAvailabilities(startDate, endDate);

		verify(reservationService, never()).getAvailabilities(any(), any());
	}

}
