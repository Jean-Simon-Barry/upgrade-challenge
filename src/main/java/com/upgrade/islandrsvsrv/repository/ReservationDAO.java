package com.upgrade.islandrsvsrv.repository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import reactor.core.publisher.Flux;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;

@RequiredArgsConstructor
public class ReservationDAO {

	private static final String GET_AVAILABILITIES_QUERY = "SELECT get_available_periods('[?,?)'::daterange)";

	private final JdbcTemplate jdbc;

	public Flux<Period> getAvailabilities(LocalDate start, LocalDate end) {
		return Flux.fromIterable(jdbc.query(GET_AVAILABILITIES_QUERY,
													(rs, num) -> availabilitiesFromQuerySet(rs),
													Date.valueOf(start),
											Date.valueOf(start)));
	}

	private static Period availabilitiesFromQuerySet(ResultSet rs) throws SQLException {
		return Period.between(rs.getDate(0).toLocalDate(), rs.getDate(1).toLocalDate());
	}
}
