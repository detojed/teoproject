package com.teoproject.pomodoro;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;

/**
 * Provides utility methods for calculating study statistics from stored sessions.
 */
public class StatisticsCalculator {

    public WeeklyStatistics calculateWeeklyStatistics(List<SessionLogEntry> entries, LocalDate referenceDate) {
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int targetWeek = referenceDate.get(weekFields.weekOfWeekBasedYear());
        int targetYear = referenceDate.get(weekFields.weekBasedYear());

        long totalMinutes = 0;
        int sessions = 0;
        int intervals = 0;

        for (SessionLogEntry entry : entries) {
            LocalDate entryDate = entry.getTimestamp().toLocalDate();
            int entryWeek = entryDate.get(weekFields.weekOfWeekBasedYear());
            int entryYear = entryDate.get(weekFields.weekBasedYear());
            if (entryWeek == targetWeek && entryYear == targetYear) {
                totalMinutes += entry.getFocusMinutes();
                sessions++;
                intervals += entry.getIntervalsCompleted();
            }
        }

        return new WeeklyStatistics(totalMinutes, sessions, intervals);
    }

    public static class WeeklyStatistics {
        private final long totalMinutes;
        private final int sessionsCompleted;
        private final int intervalsCompleted;

        public WeeklyStatistics(long totalMinutes, int sessionsCompleted, int intervalsCompleted) {
            this.totalMinutes = totalMinutes;
            this.sessionsCompleted = sessionsCompleted;
            this.intervalsCompleted = intervalsCompleted;
        }

        public long getTotalMinutes() {
            return totalMinutes;
        }

        public double getTotalHours() {
            return totalMinutes / 60.0;
        }

        public int getSessionsCompleted() {
            return sessionsCompleted;
        }

        public int getIntervalsCompleted() {
            return intervalsCompleted;
        }
    }
}
