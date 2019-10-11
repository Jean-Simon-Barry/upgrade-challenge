package com.upgrade.islandrsvsrv.controller;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class CampsiteControllerTest {

    private CampsiteController campsiteController;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    @Before
    public void setup() {
        campsiteController = new CampsiteController();
    }

    @Test
    public void testReturnsAvailabilitiesForCampsite() {
        // given
        LocalDate requestStartDate = LocalDate.parse("2019-01-01", dateFormatter);
        LocalDate requestEndDate = LocalDate.parse("2019-01-03", dateFormatter);

		LocalDate availabilityOne = LocalDate.parse("2019-01-01", dateFormatter);
		LocalDate availabilityTwo = LocalDate.parse("2019-01-02", dateFormatter);
		LocalDate availabilityThree = LocalDate.parse("2019-01-03", dateFormatter);

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
    }
}
