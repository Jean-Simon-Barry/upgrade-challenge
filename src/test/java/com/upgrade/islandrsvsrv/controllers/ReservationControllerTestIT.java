package com.upgrade.islandrsvsrv.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.upgrade.islandrsvsrv.domain.api.ReservationRequest;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static java.time.LocalDate.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MONTHS;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@ContextConfiguration(initializers = {ReservationControllerTestIT.Initializer.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ReservationControllerTestIT {

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private WebTestClient webTestClient;

	@Autowired
	private ReservationController reservationController;

	@ClassRule
	public static PostgreSQLContainer postgres = new PostgreSQLContainer<>("postgres:latest")
			.withDatabaseName("island_resort_test")
			.withUsername("upgrade")
			.withPassword("upgrade");

	@Test
	public void testInsertReservation() throws Exception {
		//given
		LocalDate startDate = now().plus(1, DAYS);
		LocalDate endDate = now().plus(4, DAYS);
		ReservationRequest reservation = ReservationRequest.builder()
				.userEmail("email")
				.userName("userName")
				.start(startDate)
				.end(endDate)
				.build();

		//when
		webTestClient.post()
				.uri("/reservation")
				.accept(MediaType.APPLICATION_JSON_UTF8)
				.body(Mono.just(reservation), ReservationRequest.class)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
				.expectBody()
				.consumeWith(response -> {
					Long reservationId = null;
					try {
						reservationId = mapper.readValue(response.getResponseBody(), new TypeReference<Long>() {
						});
					} catch (IOException e) {
						//noop
					}

					//then
					assertThat(reservationId).isEqualTo(1L);
				});

	}

	@Test
	public void testInsertReservationReturnsErrorIfOverlapping() throws Exception {
		//given
		LocalDate startDate = now().plus(10, DAYS);
		LocalDate endDate = now().plus(13, DAYS);
		ReservationRequest reservation = ReservationRequest.builder()
				.userEmail("email")
				.userName("userName")
				.start(startDate)
				.end(endDate)
				.build();

		//when
		webTestClient.post()
				.uri("/reservation")
				.accept(MediaType.APPLICATION_JSON_UTF8)
				.body(Mono.just(reservation), ReservationRequest.class)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
				.expectBody();

		webTestClient.post()
				.uri("/reservation")
				.accept(MediaType.APPLICATION_JSON_UTF8)
				.body(Mono.just(reservation), ReservationRequest.class)
				.exchange()
				.expectStatus().is4xxClientError()
				.expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
				.expectBody()
				.jsonPath("$.message", "" +
						"Sorry it looks like the island is booked somewhere between " +
						startDate  + "and " +
						endDate + "." +
						"Please try another time slot.");

	}

	static class Initializer
			implements ApplicationContextInitializer<ConfigurableApplicationContext> {
		public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
			TestPropertyValues.of(
					"spring.datasource.url=" + postgres.getJdbcUrl(),
					"spring.datasource.username=" + postgres.getUsername(),
					"spring.datasource.password=" + postgres.getPassword()
			).applyTo(configurableApplicationContext.getEnvironment());
		}
	}
}