package io.botlify.cherry.time;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.time.Instant;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * This class store a month and a year in UTC timezone.
 */
public final class MonthYear implements Comparable<MonthYear>, Serializable {

    @NotNull
    private final Month month;

    private final Year year;

    public MonthYear() {
        this(Instant.now());
    }

    public MonthYear(@NotNull final Instant instant) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(instant.toEpochMilli());
        this.month = Month.of(calendar.get(Calendar.MONTH) + 1);
        this.year = Year.of(calendar.get(Calendar.YEAR));
    }

    public MonthYear(@NotNull final Month month,
                     @NotNull final Year year) {
        this.month = month;
        this.year = year;
    }

    public MonthYear(int month, int year) {
        this.month = Month.of(month);
        this.year = Year.of(year);
    }

    public MonthYear(@NotNull final MonthYear monthYear) {
        this.month = Month.of(monthYear.getMonth().getValue());
        this.year = Year.of(monthYear.getYear().getValue());
    }

    public @NotNull Month getMonth() {
        return month;
    }

    public @NotNull Year getYear() {
        return year;
    }

    /**
     * Return the next month year, if you are in December,
     * it will return January of the next year.
     * @return The next month year.
     */
    public @NotNull MonthYear nextMonthYear() {
        Month month = this.month.plus(1);
        Year year = this.year;
        if (month == Month.JANUARY) year = this.year.plusYears(1);
        return new MonthYear(month, year);
    }

    public @NotNull MonthYear previousMonthYear() {
        Month month = this.month.minus(1);
        Year year = this.year;
        if (month == Month.DECEMBER) year = this.year.minusYears(1);
        return new MonthYear(month, year);
    }

    /**
     * Return the first day of month.
     * The time is set to 00:00:00.
     * @return The first day of month.
     */
    public @NotNull Instant getFirstDateOfMonth() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(Calendar.YEAR, this.year.getValue());
        calendar.set(Calendar.MONTH, this.month.getValue() - 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return (calendar.toInstant());
    }

    public @NotNull Instant getLastDayOfMonth() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(Calendar.YEAR, this.year.getValue());
        calendar.set(Calendar.MONTH, this.month.getValue() - 1);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return (calendar.toInstant());
    }

    /*
     $      Public static methods
     */

    public static @NotNull List<MonthYear> betweenTwoInstant(@NotNull final Instant start, @NotNull final Instant end) {
        List<MonthYear> result = new ArrayList<>();
        MonthYear current = new MonthYear(start);
        while (current.getLastDayOfMonth().isBefore(end)) {
            result.add(current);
            current = current.nextMonthYear();
        }
        return (result);
    }

    public static @NotNull List<MonthYear> fromInstants(@NotNull final List<Instant> instants) {
        List<MonthYear> monthYears = new ArrayList<>();
        for (Instant instant : instants) {
            if (monthYears.contains(new MonthYear(instant))) continue;
            monthYears.add(new MonthYear(instant));
        }
        return (monthYears);
    }

    /*
     $      Comparaison
     */

    /**
     * Compare two month year.
     * @param monthYear The month year to compare.
     * @return True if the month year is the before
     * the month year given in parameter.
     */
    public boolean isBefore(@NotNull final MonthYear monthYear) {
        if (this.year.isBefore(monthYear.getYear())) return (true);
        if (this.year.isAfter(monthYear.getYear())) return (false);
        return (this.month.getValue() < monthYear.getMonth().getValue());
    }

    /**
     * Compare two month year.
     * @param monthYear The month year to compare.
     * @return True if the month year is the after
     * the month year given in parameter.
     */
    public boolean isAfter(@NotNull final MonthYear monthYear) {
        if (this.year.isAfter(monthYear.getYear())) return (true);
        if (this.year.isBefore(monthYear.getYear())) return (false);
        return (this.month.getValue() > monthYear.getMonth().getValue());
    }

    /*
     $      Override
     */

    @Override
    public int compareTo(@NotNull final MonthYear my) {
        if (this.getYear().equals(my.getYear()))
            return (this.getMonth().compareTo(my.getMonth()));
        return (this.getYear().compareTo(my.getYear()));
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) return (true);
        if (o == null || getClass() != o.getClass()) return false;
        MonthYear monthYear = (MonthYear)o;
        if (!month.equals(monthYear.month)) return (false);
        return (year.equals(monthYear.year));
    }

    @Override
    public int hashCode() {
        String hash = this.month.getValue() + "" + this.year.getValue();
        return (Integer.parseInt(hash));
    }

    @Override
    public @NotNull String toString() {
        return (getMonth().toString() + "-" + getYear().toString());
    }

}
