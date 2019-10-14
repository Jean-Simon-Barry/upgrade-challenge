package com.upgrade.islandrsvsrv.repository;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MONTHS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import com.upgrade.islandrsvsrv.domain.DateInterval;
import com.upgrade.islandrsvsrv.domain.Reservation;
import com.upgrade.islandrsvsrv.domain.api.ReservationModification;
import com.upgrade.islandrsvsrv.domain.api.ReservationRequest;
import java.time.LocalDate;
import java.util.List;
import org.junit.Before;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.PostgreSQLContainer;

@RunWith(SpringRunner.class)
@ContextConfiguration(initializers = {ReservationDAOTestIT.Initializer.class})
@SpringBootTest
public class ReservationDAOTestIT {

  private static final LocalDate NOW = LocalDate.now();
  private static final LocalDate START_DATE_WINDOW = NOW;
  private static final LocalDate END_DATE_WINDOW = NOW.plus(10, DAYS);
  @ClassRule
  public static PostgreSQLContainer postgres = new PostgreSQLContainer<>("postgres:latest")
      .withDatabaseName("island_resort_test")
      .withUsername("upgrade")
      .withPassword("upgrade");
  @Rule
  public ExpectedException expectedEx = ExpectedException.none();
  private ReservationDAO reservationDAO;
  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Before
  public void setUp() {
    reservationDAO = new ReservationDAO(jdbcTemplate);
  }

  @Test
  public void testReturnsAvailabilityPeriods() {

    //given
    LocalDate reservationStart = START_DATE_WINDOW.plus(3, DAYS);
    LocalDate reservationEnd = reservationStart.plus(3, DAYS);
    ReservationRequest expectedReservation = ReservationRequest.builder()
        .userEmail("emailhere")
        .userName("fullnamehere")
        .start(reservationStart)
        .end(reservationEnd)
        .build();
    reservationDAO.insertReservation(expectedReservation);

    // when
    List<DateInterval> reservationsDates = reservationDAO
        .getReservationDates(START_DATE_WINDOW, END_DATE_WINDOW);

    // then
    DateInterval expected = new DateInterval(reservationStart, reservationEnd);

    assertThat(reservationsDates).containsExactly(expected);
  }

  @Test
  public void testInsertReservationReturnsId() {
    //given
    ReservationRequest reservation = ReservationRequest.builder()
        .userEmail("email")
        .userName("userName")
        .start(LocalDate.now().plus(3, MONTHS))
        .end(LocalDate.now().plus(4, MONTHS))
        .build();
    // when
    Long reservationId = reservationDAO.insertReservation(reservation);

    // then
    assertTrue(reservationId > 1);
  }

  @Test
  public void testInsertReservationThrowsExceptionIfItOverlapsWithExistingReservations() {
    //given
    DateInterval dateInterval = new DateInterval(LocalDate.now().plus(6, MONTHS),
        LocalDate.now().plus(7, MONTHS));
    ReservationRequest reservation = ReservationRequest.builder()
        .userEmail("email")
        .userName("userName")
        .start(LocalDate.now().plus(6, MONTHS))
        .end(LocalDate.now().plus(7, MONTHS))
        .build();
    expectedEx.expect(DataIntegrityViolationException.class);
    expectedEx.expectMessage(
        "PreparedStatementCallback; ERROR: conflicting key value violates exclusion " +
            "constraint \"camping_reservation_reservation_dates_excl\"\n" +
            "  Detail: Key (reservation_dates)=(" + dateInterval + ") conflicts with " +
            "existing key (reservation_dates)=(" + dateInterval + ").; " +
            "nested exception is org.postgresql.util.PSQLException: ERROR: " +
            "conflicting key value violates exclusion constraint " +
            "\"camping_reservation_reservation_dates_excl\"\n" +
            "  Detail: Key (reservation_dates)=(" + dateInterval + ") " +
            "conflicts with existing key (reservation_dates)=(" + dateInterval + ").");

    // when
    reservationDAO.insertReservation(reservation);
    reservationDAO.insertReservation(reservation);

    //then
    //exception is asserted above

  }

  @Test
  public void testUpdateReservation() {
    //given
    ReservationRequest reservation = ReservationRequest.builder()
        .userEmail("email")
        .userName("userName")
        .start(LocalDate.now().plus(8, MONTHS))
        .end(LocalDate.now().plus(9, MONTHS))
        .build();
    Long reservationId = reservationDAO.insertReservation(reservation);

    //when
    LocalDate expectedStart = LocalDate.now().plus(666, DAYS);
    LocalDate expectedEnd = LocalDate.now().plus(667, DAYS);
    reservationDAO.updateReservation(reservationId, ReservationModification.builder()
        .start(expectedStart)
        .end(expectedEnd)
        .build());

    //then
    Reservation actualReservation = reservationDAO.getReservation(reservationId);

    assertThat(actualReservation.getDateInterval().getStart()).isEqualTo(expectedStart);
    assertThat(actualReservation.getDateInterval().getEnd()).isEqualTo(expectedEnd);
  }

  @Test
  public void testDeleteReservation() {
    //given
    ReservationRequest reservation = ReservationRequest.builder()
        .userEmail("email")
        .userName("userName")
        .start(LocalDate.now().plus(10, MONTHS))
        .end(LocalDate.now().plus(11, MONTHS))
        .build();
    Long reservationId = reservationDAO.insertReservation(reservation);

    //when
    expectedEx.expect(EmptyResultDataAccessException.class);
    expectedEx.expectMessage("Incorrect result size: expected 1, actual 0");
    reservationDAO.deleteReservation(reservationId);

    //then
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
