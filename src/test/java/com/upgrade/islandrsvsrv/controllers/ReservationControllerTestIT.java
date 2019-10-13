package com.upgrade.islandrsvsrv.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.upgrade.islandrsvsrv.domain.Reservation;
import com.upgrade.islandrsvsrv.domain.api.ReservationModification;
import com.upgrade.islandrsvsrv.domain.api.ReservationRequest;
import com.upgrade.islandrsvsrv.repository.ReservationDAO;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.LocalDate;

import static java.time.LocalDate.now;
import static java.time.temporal.ChronoUnit.DAYS;

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
	private ReservationDAO reservationDAO;

	@Rule
	public ExpectedException expectedEx = ExpectedException.none();

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
						startDate + "and " +
						endDate + "." +
						"Please try another time slot.");

	}

	@Test
	public void modifiesReservation() {
		//given
		ReservationRequest build = ReservationRequest.builder()
				.start(now().plus(10, DAYS))
				.end(now().plus(15, DAYS))
				.userEmail("email")
				.userName("username")
				.build();
		Long reservationId = reservationDAO.insertReservation(build);

		LocalDate expectedStart = now().plus(19, DAYS);
		LocalDate expectedEnd = now().plus(21, DAYS);
		ReservationModification modification = ReservationModification.builder()
				.start(expectedStart)
				.end(expectedEnd)
				.reservationId(reservationId)
				.build();

		//when
		webTestClient.put()
				.uri("/reservation")
				.accept(MediaType.APPLICATION_JSON_UTF8)
				.body(Mono.just(modification), ReservationModification.class)
				.exchange()
				.expectStatus().isOk();

		//then
		Reservation actual = reservationDAO.getReservation(reservationId);
		assertThat(actual.getDateInterval().getStart()).isEqualTo(expectedStart);
		assertThat(actual.getDateInterval().getEnd()).isEqualTo(expectedEnd.minus(1, DAYS));
	}

	@Test
	public void testDeletesReservation() {
		//given
		ReservationRequest build = ReservationRequest.builder()
				.start(now().plus(23, DAYS))
				.end(now().plus(27, DAYS))
				.userEmail("email")
				.userName("username")
				.build();
		Long reservationId = reservationDAO.insertReservation(build);

		//when
		webTestClient.delete()
				.uri("/reservation/{id}", reservationId)
				.accept(MediaType.APPLICATION_JSON_UTF8)
				.exchange()
				.expectStatus().isOk();

		//then
		expectedEx.expect(EmptyResultDataAccessException.class);
		expectedEx.expectMessage("Incorrect result size: expected 1, actual 0");
		reservationDAO.getReservation(reservationId);
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
