package io.botlify.cherry.time;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Date;
import java.time.Instant;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * This class store a week of year and a year.
 * It is aligned with the ISO-8601 standard and the first day of the week is Monday.
 * The first week of the year is the 1st day of the year to the last day of the week.
 */
public final class WeekOfYear implements Comparable<WeekOfYear> {

    @NotNull @Getter
    private final Calendar calendar;

    @NotNull @Getter
    private final Year year;

    @NotNull @Getter
    private final Integer week;

    public WeekOfYear() {
        this(Instant.now());
    }

    public WeekOfYear(@NotNull final Instant instant) {
        this.calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        this.calendar.setTime(Date.from(instant));
        fixCalendar();
        // set value
        this.year = Year.of(this.calendar.get(Calendar.YEAR));
        this.week = this.calendar.get(Calendar.WEEK_OF_YEAR);
    }

    public WeekOfYear(@NotNull final Calendar calendar) {
        this.calendar = calendar;
        fixCalendar();
        // set value
        this.year = Year.of(calendar.getWeekYear());
        this.week = calendar.get(Calendar.WEEK_OF_YEAR);
    }

    /**
     * Construct a week of year in UTC timezone.
     * @param week Week of year.
     * @param year Year.
     */
    public WeekOfYear(@NotNull final Integer week,
                      @NotNull final Year year) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(Calendar.YEAR, year.getValue());
        calendar.set(Calendar.WEEK_OF_YEAR, week);
        this.calendar = calendar;
        fixCalendar();
        // set value
        this.week = week;
        this.year = year;
    }

    public WeekOfYear(@NotNull final Integer week,
                      @NotNull final Integer year) {
        this(week, Year.of(year));
    }

    public WeekOfYear(@NotNull final WeekOfYear weekOfYear) {
        this.calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        this.calendar.setTime(Date.from(weekOfYear.getFirstDayOfWeek()));
        fixCalendar();
        // set value
        this.week = weekOfYear.getWeek();
        this.year = weekOfYear.getYear();
    }

    public @NotNull WeekOfYear addYear(final int year) {
        return (new WeekOfYear(this.week, this.year.plusYears(year)));
    }

    public @NotNull WeekOfYear addWeeks(final int weeks) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.setTime(Date.from(this.toInstant()));
        calendar.add(Calendar.WEEK_OF_YEAR, weeks);
        return new WeekOfYear(calendar);
    }

    public @NotNull Instant getFirstDayOfWeek() {
        return (this.toInstant());
    }

    public @NotNull Instant getLastDayOfWeek() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTime(Date.from(this.toInstant()));
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.add(Calendar.DAY_OF_YEAR, 6);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return (calendar.toInstant());
    }

    public @NotNull WeekOfYear nextWeek() {
        return (this.addWeeks(1));
    }

    /**
     * The list contain the actual week and the next <b>number</b>
     * weeks.
     * @param number Number of weeks.
     * @return The list of weeks.
     */
    public @NotNull List<WeekOfYear> nextWeeks(final int number) {
        List<WeekOfYear> weeks = new ArrayList<>();
        WeekOfYear week = this;
        weeks.add(week);
        for (int i = 0; i < number; i++) {
            week = week.nextWeek();
            weeks.add(week);
        }
        return (weeks);
    }

    public boolean isAfter(@NotNull final WeekOfYear weekOfYear) {
        return (this.compareTo(weekOfYear) > 0);
    }

    public boolean isBefore(@NotNull final WeekOfYear weekOfYear) {
        return (this.compareTo(weekOfYear) < 0);
    }

    /*
     $      Private methods
     */

    private void fixCalendar() {
        this.calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        this.calendar.set(Calendar.HOUR_OF_DAY, 0);
        this.calendar.set(Calendar.MINUTE, 0);
        this.calendar.set(Calendar.SECOND, 0);
        this.calendar.set(Calendar.MILLISECOND, 0);
    }

    /*
     $      Public static methods
     */

    public static @NotNull List<WeekOfYear> betweenTwoInstant(@NotNull final Instant start, @NotNull final Instant end) {
        List<WeekOfYear> weeks = new ArrayList<>();
        WeekOfYear week = new WeekOfYear(start);
        while (week.getLastDayOfWeek().isBefore(end)) {
            weeks.add(week);
            week = week.addWeeks(1);
        }
        return (weeks);
    }

    /**
     * Return the day just before next week of year and after the given date.
     * @param instant The date.
     * @return The day just before next month and after the given date.
     */
    public static @NotNull Instant getPreviousInstantBeforeNextWeekOfYear(@NotNull final Instant instant) {
        WeekOfYear monthYear = new WeekOfYear(instant);
        Instant nextDay = instant.plus(1, ChronoUnit.DAYS);
        WeekOfYear nextMonthYear = new WeekOfYear(nextDay);
        while (monthYear.equals(nextMonthYear)) {
            nextDay = nextDay.plus(1, ChronoUnit.DAYS);
            nextMonthYear = new WeekOfYear(nextDay);
        }
        Instant result = nextDay.minus(1, ChronoUnit.DAYS);
        result = result.plus(23, ChronoUnit.HOURS);
        result = result.plus(59, ChronoUnit.MINUTES);
        result = result.plus(59, ChronoUnit.SECONDS);
        return (result);
    }

    /*
     $      Getter
     */

    public @NotNull Instant toInstant() {
        return (this.calendar.toInstant());
    }

    public @NotNull Period toPeriod() {
        return new Period(getFirstDayOfWeek(), getLastDayOfWeek());
    }

    public @NotNull List<MonthYear> toMonthYears() {
        List<MonthYear> monthYears = new ArrayList<>();
        monthYears.add(new MonthYear(getFirstDayOfWeek()));
        MonthYear monthYearLastDay = new MonthYear(getLastDayOfWeek());
        if (!monthYears.contains(monthYearLastDay))
            monthYears.add(monthYearLastDay);
        return (monthYears);
    }

    /*
     $      Comparable
     */

    @Override
    public int compareTo(@NotNull WeekOfYear wy) {
        if (this.getYear().equals(wy.getYear()))
            return (Integer.compare(this.getWeek(), wy.getWeek()));
        return (this.getYear().compareTo(wy.getYear()));
    }

    /*
     $      Override object methods
     */

    @Override
    public int hashCode() {
        String sb = String.valueOf(week) + year.getValue();
        return (Integer.parseInt(sb));
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        if (obj == null)
            return (false);
        if (getClass() != obj.getClass())
            return (false);
        final WeekOfYear other = (WeekOfYear) obj;
        if (!Objects.equals(this.week, other.week))
            return (false);
        return Objects.equals(this.year, other.year);
    }

    @Override
    public String toString() {
        return "week " + this.week + " of " + this.year;
    }

}
