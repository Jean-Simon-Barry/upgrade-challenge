package com.upgrade.islandrsvsrv.services;

import com.upgrade.islandrsvsrv.domain.DateInterval;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class AvailabilityService {

	public List<LocalDate> findAvailabilities(DateInterval requestedInterval, List<DateInterval> sortedOccupiedIntervals) {

		LocalDate currentStart = requestedInterval.getStart();
		List<LocalDate> availableDates = new ArrayList<>();

		List<DateInterval> filteredReservations = sortedOccupiedIntervals.stream()
				.filter(reservation -> reservation.getStart().isBefore(requestedInterval.getEnd()) &&
						requestedInterval.getStart().isBefore(reservation.getEnd()))
				.collect(toList());

		if (filteredReservations.isEmpty()) {
			return requestedInterval.getStart()
					.datesUntil(requestedInterval.getEnd())
					.collect(toList());
		}

		for (DateInterval reservationDate : filteredReservations) {
			availableDates.addAll(currentStart
										  .datesUntil(maximum(requestedInterval.getStart(), reservationDate.getStart()))
										  .collect(toList()));
			currentStart = minimum(requestedInterval.getEnd(), reservationDate.getEnd());
		}

		//get the last stretch of availabilities between the last reservation end and the requested interval end
		availableDates.addAll(currentStart.datesUntil(requestedInterval.getEnd()).collect(toList()));

		return availableDates;
	}

	private LocalDate minimum(LocalDate date1, LocalDate date2) {
		return date1.isBefore(date2) ? date1 : date2;
	}

	private LocalDate maximum(LocalDate date1, LocalDate date2) {
		return date1.isAfter(date2) ? date1 : date2;
	}
}
