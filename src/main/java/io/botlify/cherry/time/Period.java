package io.botlify.cherry.time;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ToString
public final class Period {

    private final LocalDate startDate;

    private final LocalDate endDate;

    /*
     $      Constructor
     */

    /**
     * This constructor is used to create a period from two dates.
     * This constructor convert the two dates into a LocalDate object.
     * @param startDate The start date of the period.
     * @param endDate The end date of the period.
     */
    public Period(@NotNull final Instant startDate,
                  @NotNull final Instant endDate) {
        this.startDate = startDate.atZone(ZoneId.of("UTC")).toLocalDate();
        this.endDate = endDate.atZone(ZoneId.of("UTC")).toLocalDate();
        postConstructor();
    }

    public Period(@NotNull final Instant startDate,
                  @NotNull final Instant endDate,
                  @NotNull final String timeZone) {
        this.startDate = startDate.atZone(ZoneId.of(timeZone)).toLocalDate();
        this.endDate = endDate.atZone(ZoneId.of(timeZone)).toLocalDate();
        postConstructor();
    }

    /**
     * This constructor is used to create a period from two dates.
     * @param startDate The start date of the period.
     * @param endDate The end date of the period.
     */
    public Period(@NotNull LocalDate startDate, @NotNull LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
        postConstructor();
    }

    /**
     * This constructor is used to create a period from two dates.
     * @param startDate The start date of the period in YYYY-MM-DD format.
     * @param endDate The start date of the period in YYYY-MM-DD format.
     */
    public Period(@NotNull String startDate, @NotNull String endDate) {
        this.startDate = LocalDate.parse(startDate);
        this.endDate = LocalDate.parse(endDate);
        postConstructor();
    }

    /**
     * This post constructor is used to check if the period is valid.
     * The start date must be before the end date.
     */
    private void postConstructor() {
        if (startDate.isAfter(endDate))
            throw (new IllegalArgumentException("The start date is after the end date."));
    }

    /*
     $      Public function
     */

    /**
     * Check if the date is inside the given period.
     * @param dateLocal The date to check.
     * @return True if the date is inside the period.
     */
    public boolean isInside(@NotNull final LocalDate dateLocal) {
        return (isAfterOrEquals(startDate, dateLocal) && isBeforeOrEquals(endDate, dateLocal));
    }

    public boolean isInside(@NotNull final Instant dateInstant) {
        return (isInside(dateInstant.atZone(ZoneId.of("UTC")).toLocalDate()));
    }

    /**
     * Check if the period is inside the given period.
     * @param period The period to check.
     * @return True if the period is inside the period.
     */
    public boolean isInside(@NotNull final Period period) {
        return (isAfterOrEquals(startDate, period.getStartDate()) && isBeforeOrEquals(endDate, period.getEndDate()));
    }

    /**
     * Check if the period is overlapping the given period.
     * @param period The period to check.
     * @return True if the period is overlapping the period.
     */
    public boolean isOverlap(@NotNull Period period) {
        if (startDate.isBefore(period.startDate) && endDate.isBefore(period.startDate))
            return (false);
        return !startDate.isAfter(period.endDate) || !endDate.isAfter(period.endDate);
    }

    /*
     $      Private method
     */

    /**
     * Check if the dateLocalRef is before or equal to dateLocalB.
     * @param dateLocalRef The reference date for comparison.
     * @param dateLocalToCmp The second date to compare.
     * @return True if the dateLocalRef is before or equal to dateLocalB.
     */
    private boolean isBeforeOrEquals(@NotNull LocalDate dateLocalRef, @NotNull LocalDate dateLocalToCmp) {
        return (dateLocalRef.isAfter(dateLocalToCmp) || dateLocalRef.equals(dateLocalToCmp));
    }

    /**
     * Check if the dateLocalA is after or equal the dateLocalB.
     * @param dateLocalRef The reference date for comparison.
     * @param dateLocalToCmp The second date to compare.
     * @return True if the dateLocalA is after or equals the dateLocalB.
     */
    private boolean isAfterOrEquals(@NotNull LocalDate dateLocalRef, @NotNull LocalDate dateLocalToCmp) {
        return (dateLocalRef.isBefore(dateLocalToCmp) || dateLocalRef.equals(dateLocalToCmp));
    }

    private boolean isOverlapLeft(@NotNull Period period) {
        if (!isBeforeOrEquals(startDate, period.getStartDate()))
            return (false);
        if (!isBeforeOrEquals(endDate, period.getEndDate()))
            return (false);
        return isAfterOrEquals(startDate, period.getEndDate());
    }

    private boolean isOverlapRight(@NotNull Period period) {
        if (!isAfterOrEquals(startDate, period.getStartDate()))
            return (false);
        if (!isAfterOrEquals(endDate, period.getEndDate()))
            return (false);
        return isBeforeOrEquals(endDate, period.getStartDate());
    }

    private boolean isOverlapRightAndLeft(@NotNull Period period) {
        return isBeforeOrEquals(startDate, period.getStartDate()) && isAfterOrEquals(endDate, period.getEndDate());
    }

    /**
     * Check if the period is in month year.
     * @param monthYear The month year to check.
     * @return True if the period is in month year.
     */
    public boolean isIn(@NotNull final MonthYear monthYear) {
        List<MonthYear> monthYears = toMonthYears();
        return (monthYears.contains(monthYear));
    }

    /**
     * Check if the period is in week year.
     * @param weekOfYear The week of year to check.
     * @return True if the period is in week of year.
     */
    public boolean isIn(@NotNull final WeekOfYear weekOfYear) {
        List<WeekOfYear> weekOfYears = toWeekYears();
        return (weekOfYears.contains(weekOfYear));
    }

    public @NotNull List<MonthYear> toMonthYears() {
        MonthYear tmp = new MonthYear(getStartDateAsInstant());
        MonthYear end = new MonthYear(getEndDateAsInstant());
        if (tmp.equals(end))
            return (Collections.singletonList(tmp));
        List<MonthYear> monthYears = new ArrayList<>();
        while (tmp.isBefore(end)) {
            monthYears.add(tmp);
            tmp = tmp.nextMonthYear();
        }
        monthYears.add(end);
        return (monthYears);
    }

    public @NotNull List<WeekOfYear> toWeekYears() {
        WeekOfYear tmp = new WeekOfYear(getStartDateAsInstant());
        WeekOfYear end = new WeekOfYear(getEndDateAsInstant());
        if (tmp.equals(end))
            return (Collections.singletonList(tmp));
        List<WeekOfYear> weekYears = new ArrayList<>();
        while (tmp.isBefore(end)) {
            weekYears.add(tmp);
            tmp = tmp.nextWeek();
        }
        weekYears.add(end);
        return (weekYears);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Period period = (Period) o;

        if (!startDate.equals(period.startDate)) return false;
        return endDate.equals(period.endDate);
    }

    @Override
    public int hashCode() {
        int result = startDate.hashCode();
        result = 31 * result + endDate.hashCode();
        return result;
    }

    /*
     $      Getters and setters
     */

    /**
     * Get the start date of the period.
     * @return The start date of the period.
     */
    public LocalDate getStartDate() {
        return (startDate);
    }

    /**
     * Get the end date of the period.
     * @return The end date of the period.
     */
    public LocalDate getEndDate() {
        return (endDate);
    }

    public Instant getStartDateAsInstant() {
        return (startDate.atStartOfDay().toInstant(ZoneOffset.UTC));
    }

    public Instant getEndDateAsInstant() {
        return (endDate.atStartOfDay().toInstant(ZoneOffset.UTC));
    }

}
