package com.upgrade.islandrsvsrv.controllers;


import com.upgrade.islandrsvsrv.services.ReservationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CampsiteControllerTest {

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	@Mock
	private ReservationService reservationService;

	@InjectMocks
    private CampsiteController campsiteController;

    @Before
    public void setUp() {
		MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testReturnsAvailabilitiesForCampsite() {
        // given
        LocalDate requestStartDate = LocalDate.parse("2019-01-01", dateFormatter);
        LocalDate requestEndDate = LocalDate.parse("2019-01-03", dateFormatter);

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
}
