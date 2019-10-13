package com.upgrade.islandrsvsrv.repository;

import com.upgrade.islandrsvsrv.domain.DateInterval;
import com.upgrade.islandrsvsrv.domain.Reservation;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.PostgreSQLContainer;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MONTHS;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@ContextConfiguration(initializers = {ReservationDAOTestIT.Initializer.class})
@SpringBootTest
public class ReservationDAOTestIT {

	private ReservationDAO reservationDAO;
	private static final LocalDate NOW = LocalDate.now();
	private static final LocalDate START_DATE_WINDOW = NOW;
	private static final LocalDate END_DATE_WINDOW = NOW.plus(10, DAYS);

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@ClassRule
	public static PostgreSQLContainer postgres = new PostgreSQLContainer<>("postgres:latest")
			.withDatabaseName("island_resort_test")
			.withUsername("upgrade")
			.withPassword("upgrade");

	@Before
	public void setUp() {
		reservationDAO = new ReservationDAO(jdbcTemplate);
	}

	@Test
	public void testReturnsAvailabilityPeriods() {

		//given
		LocalDate reservationStart = START_DATE_WINDOW.plus(3, DAYS);
		LocalDate reservationEnd = reservationStart.plus(3, DAYS);
		Reservation reservation = Reservation.builder()
				.userEmail("emailhere")
				.userName("fullnamehere")
				.dateInterval(new DateInterval(reservationStart, reservationEnd))
				.build();
		reservationDAO.insertReservation(reservation);

		// when
		Flux<DateInterval> availabilities = reservationDAO.getAvailabilities(START_DATE_WINDOW, END_DATE_WINDOW);

		// then
		DateInterval expectedAvailableInterval1 = new DateInterval(START_DATE_WINDOW,
																  reservationStart.minus(1, DAYS));
		DateInterval expectedAvailableInterval2 = new DateInterval(reservationEnd, END_DATE_WINDOW);

		StepVerifier.create(availabilities)
				.recordWith(ArrayList::new)
				.expectNextCount(2)
				.consumeRecordedWith(periods -> assertThat(periods, hasItems(expectedAvailableInterval1,
																			 expectedAvailableInterval2)))
				.verifyComplete();
	}

	@Test
	public void testInsertReservationReturnsId() {
		//given
		Reservation reservation = Reservation.builder()
				.userEmail("email")
				.userName("userName")
				.dateInterval(new DateInterval(LocalDate.now().plus(3, MONTHS),
											   LocalDate.now().plus(4, MONTHS)))
				.build();
		// when
		Optional<Long> reservationId = reservationDAO.insertReservation(reservation);

		// then
		assertTrue(reservationId.isPresent());
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
