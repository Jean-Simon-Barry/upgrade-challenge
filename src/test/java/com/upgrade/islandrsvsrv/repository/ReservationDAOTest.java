package com.upgrade.islandrsvsrv.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import static java.time.LocalDate.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReservationDAOTest {

	@Mock
	private JdbcTemplate jdbcTemplate;

	private ReservationDAO reservationDAO;

	@Before
	public void setUp() throws Exception {
		reservationDAO = new ReservationDAO(jdbcTemplate);
	}

	@Test
	public void returnsAvailabilities() {
		// given
		LocalDate start = now();
		LocalDate end = now().plus(10, DAYS);
		Period expected = Period.between(start, end.minus(2, DAYS));
		when(jdbcTemplate.query(any(String.class), any(RowMapper.class), any(), any())).thenReturn(List.of(expected));

		// when
		Flux<Period> availabilities = reservationDAO.getAvailabilities(start, end);

		// then
		StepVerifier.create(availabilities).expectNext(expected).verifyComplete();

		verify(jdbcTemplate).query(any(String.class), any(RowMapper.class), any(), any());
	}
}
