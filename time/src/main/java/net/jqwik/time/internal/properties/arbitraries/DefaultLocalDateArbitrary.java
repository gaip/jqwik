package net.jqwik.time.internal.properties.arbitraries;

import java.time.*;
import java.util.*;
import java.util.stream.*;

import org.apiguardian.api.*;

import net.jqwik.api.*;
import net.jqwik.api.arbitraries.*;
import net.jqwik.time.api.arbitraries.*;

import static java.time.temporal.ChronoUnit.*;
import static org.apiguardian.api.API.Status.*;

@API(status = INTERNAL)
public class DefaultLocalDateArbitrary extends ArbitraryDecorator<LocalDate> implements LocalDateArbitrary {

	private static final LocalDate DEFAULT_MIN_DATE = LocalDate.of(1900, 1, 1);
	private static final LocalDate DEFAULT_MAX_DATE = LocalDate.of(2500, 12, 31);

	private LocalDate dateMin = null;
	private LocalDate dateMax = null;

	private Set<Month> allowedMonths = new HashSet<>(Arrays.asList(Month.values()));
	private Set<DayOfWeek> allowedDayOfWeeks = new HashSet<>(Arrays.asList(DayOfWeek.values()));

	private int dayOfMonthMin = 1;
	private int dayOfMonthMax = 31;

	private boolean withLeapYears = true;

	@Override
	protected Arbitrary<LocalDate> arbitrary() {

		LocalDate effectiveMin = effectiveMinDate();
		LocalDate effectiveMax = effectiveMaxDate();

		long days = DAYS.between(effectiveMin, effectiveMax);

		Arbitrary<Long> day =
				Arbitraries.longs()
						   .between(0, days)
						   .withDistribution(RandomDistribution.uniform())
						   .edgeCases(edgeCases -> {
							   edgeCases.includeOnly(0L, days);
							   Optional<Long> optionalLeapDay = firstLeapDayAfter(effectiveMin, days);
							   optionalLeapDay.ifPresent(edgeCases::add);
						   });

		Arbitrary<LocalDate> localDates = day.map(effectiveMin::plusDays);

		if (allowedMonths.size() < 12) {
			localDates = localDates.filter(date -> allowedMonths.contains(date.getMonth()));
		}

		if (allowedDayOfWeeks.size() < 7) {
			localDates = localDates.filter(date -> allowedDayOfWeeks.contains(date.getDayOfWeek()));
		}

		if (dayOfMonthMax - dayOfMonthMin != 30) {
			localDates = localDates.filter(date -> date.getDayOfMonth() >= dayOfMonthMin && date.getDayOfMonth() <= dayOfMonthMax);
		}

		if (!withLeapYears) {
			localDates = localDates.filter(date -> !new GregorianCalendar().isLeapYear(date.getYear()));
		}

		return localDates;

	}

	private LocalDate effectiveMaxDate() {
		LocalDate effective = dateMax == null ? DEFAULT_MAX_DATE : dateMax;
		int latestMonth = latestAllowedMonth();
		if (latestMonth < effective.getMonth().getValue()) {
			return effective.withMonth(latestMonth);
		}
		if (dayOfMonthMax < effective.getDayOfMonth()) {
			return effective.withDayOfMonth(dayOfMonthMax);
		}

		return effective;
	}

	private int latestAllowedMonth() {
		return allowedMonths.stream()
							.sorted((m1, m2) -> -Integer.compare(m1.getValue(), m2.getValue()))
							.map(Month::getValue)
							.findFirst().orElse(12);
	}

	private LocalDate effectiveMinDate() {
		LocalDate effective = dateMin == null ? DEFAULT_MIN_DATE : dateMin;
		int earliestMonth = earliestAllowedMonth();
		if (earliestMonth > effective.getMonth().getValue()) {
			return effective.withMonth(earliestMonth);
		}
		if (dayOfMonthMin > effective.getDayOfMonth()) {
			return effective.withDayOfMonth(dayOfMonthMin);
		}
		return effective;
	}

	private int earliestAllowedMonth() {
		return allowedMonths.stream()
							.sorted(Comparator.comparing(Month::getValue))
							.map(Month::getValue)
							.findFirst().orElse(1);
	}

	private Optional<Long> firstLeapDayAfter(LocalDate date, long maxOffset) {
		long offset = nextLeapDayOffset(date, 0);
		if (offset > maxOffset) {
			return Optional.empty();
		} else {
			return Optional.of(offset);
		}
	}

	private long nextLeapDayOffset(LocalDate date, long base) {
		if (date.isLeapYear()) {
			if (date.getMonth().compareTo(Month.FEBRUARY) <= 0) {
				LocalDate leapDaySameYear = date.withMonth(2).withDayOfMonth(29);
				long offset = DAYS.between(date, leapDaySameYear);
				return base + offset;
			}
		}
		int nextYear = date.getYear() + 1;
		if (nextYear > Year.MAX_VALUE) {
			return Long.MAX_VALUE;
		}
		LocalDate nextJan1 = LocalDate.of(nextYear, 1, 1);

		return nextLeapDayOffset(nextJan1, base + DAYS.between(date, nextJan1));
	}

	@Override
	public LocalDateArbitrary atTheEarliest(LocalDate min) {
		if (min.getYear() <= 0) {
			throw new IllegalArgumentException("Minimum year in a date must be > 0");
		}
		if ((dateMax != null) && min.isAfter(dateMax)) {
			throw new IllegalArgumentException("Minimum date must not be after maximum date");
		}

		DefaultLocalDateArbitrary clone = typedClone();
		clone.dateMin = min;
		return clone;
	}

	@Override
	public LocalDateArbitrary atTheLatest(LocalDate max) {
		if (max.getYear() <= 0) {
			throw new IllegalArgumentException("Maximum year in a date must be > 0");
		}
		if ((dateMin != null) && max.isBefore(dateMin)) {
			throw new IllegalArgumentException("Maximum date must not be before minimum date");
		}

		DefaultLocalDateArbitrary clone = typedClone();
		clone.dateMax = max;
		return clone;
	}

	@Override
	public LocalDateArbitrary yearBetween(Year min, Year max) {
		if (!min.isBefore(max)) {
			Year remember = min;
			min = max;
			max = remember;
		}

		LocalDate minDate = LocalDate.of(min.getValue(), 1, 1);
		LocalDate maxDate = LocalDate.of(max.getValue(), 12, 31);
		return between(minDate, maxDate);
	}

	@Override
	public LocalDateArbitrary monthBetween(Month min, Month max) {
		if (min.compareTo(max) > 0) {
			throw new IllegalArgumentException("Minimum month cannot be after maximum month");
		}

		DefaultLocalDateArbitrary clone = typedClone();
		clone.allowedMonths = Arrays.stream(Month.values())
									.filter(m -> m.compareTo(min) >= 0 && m.compareTo(max) <= 0)
									.collect(Collectors.toSet());
		return clone;
	}

	@Override
	public LocalDateArbitrary onlyMonths(Month... months) {
		DefaultLocalDateArbitrary clone = typedClone();
		clone.allowedMonths = new HashSet<>(Arrays.asList(months));
		return clone;
	}

	@Override
	public LocalDateArbitrary dayOfMonthBetween(int min, int max) {
		if (min > max) {
			int remember = min;
			min = max;
			max = remember;
		}
		DefaultLocalDateArbitrary clone = typedClone();
		clone.dayOfMonthMin = Math.max(1, min);
		clone.dayOfMonthMax = Math.min(31, max);
		return clone;
	}

	@Override
	public LocalDateArbitrary onlyDaysOfWeek(DayOfWeek... daysOfWeek) {
		DefaultLocalDateArbitrary clone = typedClone();
		clone.allowedDayOfWeeks = new HashSet<>(Arrays.asList(daysOfWeek));
		return clone;
	}

	@Override
	public LocalDateArbitrary leapYears(boolean withLeapYears) {
		DefaultLocalDateArbitrary clone = typedClone();
		clone.withLeapYears = withLeapYears;
		return clone;
	}

}
