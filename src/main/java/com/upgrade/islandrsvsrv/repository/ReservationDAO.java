package com.upgrade.islandrsvsrv.repository;

import com.upgrade.islandrsvsrv.domain.DateInterval;
import com.upgrade.islandrsvsrv.domain.Reservation;
import com.upgrade.islandrsvsrv.domain.api.ReservationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;


@RequiredArgsConstructor
@Service
public class ReservationDAO {

	private static final String EMPTY_SEARCH_RESULT_STRING = "empty";
	private static final String GET_AVAILABILITIES_QUERY = "SELECT get_available_periods(daterange(?, ?, '[]'))";
	private static final String INSERT_RESERVATION = "INSERT INTO camping_reservation(user_name, " +
			"user_email, reservation_dates) VALUES (?, ?, daterange(?, ?));";
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	private final JdbcTemplate jdbc;

	public Flux<DateInterval> getAvailabilities(LocalDate start, LocalDate end) {
		List<DateInterval> availabilities = jdbc.query(GET_AVAILABILITIES_QUERY,
													   (rs, num) -> availabilitiesFromQuerySet(rs),
													   Date.valueOf(start),
													   Date.valueOf(end))
				.stream()
				.flatMap(Optional::stream)
				.collect(toList());
		return Flux.fromIterable(availabilities);
	}

	public Long insertReservation(ReservationRequest reservation) throws DataIntegrityViolationException {
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

	private Optional<DateInterval> availabilitiesFromQuerySet(ResultSet rs) throws SQLException {
		String dateRangeString = rs.getString(1);
		return parsePeriodFromDateRangeString(dateRangeString);
	}

	/**
	 * It seems like there's no support for jdbc to resultset.getDateRange(column), so I am forced to parse the range as
	 * a string. It's no big deal I guess but adds some complexity.
	 * @param dateRange the daterange is of this format : '[yyyy-MM-dd,yyyy-MM-dd)' since we are dealing with exclusive
	 *                  end dates. Alternatively, it is possible that there are _no_ available dates for the requested
	 *                  dates so the dateRange input will be 'empty'.
	 * @return a Period parsed from daterange passed as a string
	 */
	private Optional<DateInterval> parsePeriodFromDateRangeString(String dateRange) {
		if (dateRange.equals(EMPTY_SEARCH_RESULT_STRING)) {
			return Optional.empty();
		}
		String[] stringDates = dateRange
				.replace("[", "")
				.replace(")", "")
				.split(",");

		LocalDate start = LocalDate.parse(stringDates[0], DATE_TIME_FORMATTER);
		//remove 1 day from the end since we have exclusive end dates
		LocalDate end = LocalDate.parse(stringDates[1], DATE_TIME_FORMATTER).minus(1, DAYS);
		return Optional.of(new DateInterval(start, end));
	}

}
