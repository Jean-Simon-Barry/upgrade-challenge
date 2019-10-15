package com.upgrade.islandrsvsrv.repository;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import com.upgrade.islandrsvsrv.domain.DateInterval;
import com.upgrade.islandrsvsrv.domain.Reservation;
import com.upgrade.islandrsvsrv.domain.api.ReservationModification;
import com.upgrade.islandrsvsrv.domain.api.ReservationRequest;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;


@RequiredArgsConstructor
@Service
public class ReservationDAO {

  private static final String EMPTY_SEARCH_RESULT_STRING = "empty";

  private static final String GET_AVAILABILITIES_QUERY =
      "SELECT reservation_dates from camping_reservation where " +
          "reservation_dates && daterange(?, ?, '[]') ORDER BY reservation_dates";

  private static final String INSERT_RESERVATION = "INSERT INTO camping_reservation(user_name, " +
      "user_email, reservation_dates) VALUES (?, ?, daterange(?, ?));";

  private static final String UPDATE_RESERVATION =
      "UPDATE camping_reservation SET reservation_dates = daterange(?, ?)" +
          " WHERE id = ?;";

  private static final String GET_RESERVATION = "SELECT user_name, user_email, reservation_dates" +
      " FROM camping_reservation WHERE id = ?";

  private static final String DELETE_RESERVATION = "DELETE FROM camping_reservation where id = ?";


  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
      .ofPattern("yyyy-MM-dd");

  private final JdbcTemplate jdbc;

  public List<DateInterval> getReservationDates(LocalDate start, LocalDate end) {
    return jdbc.query(GET_AVAILABILITIES_QUERY,
        (rs, num) -> reservtionDatesFromResultSet(rs),
        Date.valueOf(start),
        Date.valueOf(end))
        .stream()
        .flatMap(Optional::stream)
        .collect(toList());
  }

  public Long insertReservation(ReservationRequest reservation)
      throws DataIntegrityViolationException {
    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbc.update(connection -> {
          PreparedStatement ps = connection.prepareStatement(INSERT_RESERVATION, new String[]{"id"});
          ps.setString(1, reservation.getUserName());
          ps.setString(2, reservation.getUserEmail());
          ps.setDate(3, Date.valueOf(reservation.getStart()));
          ps.setDate(4, Date.valueOf(reservation.getEnd()));
          return ps;
        },
        keyHolder);
    return requireNonNull(keyHolder.getKey()).longValue();
  }

  public void updateReservation(long reservationId, ReservationModification modification) {
    jdbc.update(connection -> {
      PreparedStatement ps = connection.prepareStatement(UPDATE_RESERVATION);
      ps.setDate(1, Date.valueOf(modification.getStart()));
      ps.setDate(2, Date.valueOf(modification.getEnd()));
      ps.setLong(3, reservationId);
      return ps;
    });
  }

  public void deleteReservation(long reservationId) {
    jdbc.update(connection -> {
      PreparedStatement ps = connection.prepareStatement(DELETE_RESERVATION);
      ps.setLong(1, reservationId);
      return ps;
    });
  }

  public Reservation getReservation(Long reservationId) {
    return jdbc.queryForObject(GET_RESERVATION,
        (rs, num) -> mapReservationFromResultSet(rs),
        reservationId);
  }

  private Reservation mapReservationFromResultSet(ResultSet rs) throws SQLException {
    return Reservation.builder()
        .userName(rs.getString(1))
        .userEmail(rs.getString(2))
        .dateInterval(dateIntervalFromDateRange(rs.getString(3)))
        .build();
  }

  private Optional<DateInterval> reservtionDatesFromResultSet(ResultSet rs) throws SQLException {
    String dateRangeString = rs.getString(1);
    return parsePeriodFromDateRangeString(dateRangeString);
  }

  /**
   * It seems like there's no support for jdbc to resultset.getDateRange(column), so I am forced to
   * parse the range as a string. It's no big deal I guess but adds some complexity.
   *
   * @param dateRange the daterange is of this format : '[yyyy-MM-dd,yyyy-MM-dd)' since we are
   *                  dealing with exclusive end dates. Alternatively, it is possible that there are
   *                  _no_ available dates for the requested dates so the dateRange input will be
   *                  'empty'.
   * @return a Period parsed from daterange passed as a string
   */
  private Optional<DateInterval> parsePeriodFromDateRangeString(String dateRange) {
    if (dateRange.equals(EMPTY_SEARCH_RESULT_STRING)) {
      return Optional.empty();
    }
    return Optional.of(dateIntervalFromDateRange(dateRange));
  }

  private DateInterval dateIntervalFromDateRange(String dateRange) {
    String[] stringDates = dateRange
        .replace("[", "")
        .replace(")", "")
        .split(",");

    LocalDate start = LocalDate.parse(stringDates[0], DATE_TIME_FORMATTER);
    //remove 1 day from the end since we have exclusive end dates
    LocalDate end = LocalDate.parse(stringDates[1], DATE_TIME_FORMATTER);
    return new DateInterval(start, end);
  }
}
