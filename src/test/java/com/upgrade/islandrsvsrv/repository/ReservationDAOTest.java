package com.upgrade.islandrsvsrv.repository;

import com.upgrade.islandrsvsrv.domain.DateInterval;
import com.upgrade.islandrsvsrv.domain.Reservation;
import com.upgrade.islandrsvsrv.domain.api.ReservationRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.KeyHolder;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
		DateInterval expected = new DateInterval(start, end.minus(1, DAYS));
		when(jdbcTemplate.query(any(String.class), any(RowMapper.class), any(), any())).thenReturn(List.of(Optional.of(expected)));

		// when
		Flux<DateInterval> availabilities = reservationDAO.getAvailabilities(start, end);

		// then
		StepVerifier.create(availabilities).expectNext(expected).verifyComplete();

		verify(jdbcTemplate).query(any(String.class), any(RowMapper.class), any(), any());
	}
}
