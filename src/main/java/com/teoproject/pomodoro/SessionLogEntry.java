package com.teoproject.pomodoro;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a single study session entry stored in the persistent log file.
 */
public class SessionLogEntry {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final LocalDateTime timestamp;
    private final String goalDescription;
    private final long focusMinutes;
    private final int intervalsCompleted;

    public SessionLogEntry(LocalDateTime timestamp, String goalDescription, long focusMinutes, int intervalsCompleted) {
        this.timestamp = timestamp;
        this.goalDescription = goalDescription;
        this.focusMinutes = focusMinutes;
        this.intervalsCompleted = intervalsCompleted;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getGoalDescription() {
        return goalDescription;
    }

    public long getFocusMinutes() {
        return focusMinutes;
    }

    public int getIntervalsCompleted() {
        return intervalsCompleted;
    }

    public String toCsvRow() {
        return String.join(",",
                FORMATTER.format(timestamp),
                escape(goalDescription),
                Long.toString(focusMinutes),
                Integer.toString(intervalsCompleted));
    }

    public static SessionLogEntry fromCsvRow(String row) {
        String[] parts = splitEscaped(row);
        if (parts.length < 4) {
            throw new IllegalArgumentException("Invalid log row: " + row);
        }
        LocalDateTime time = LocalDateTime.parse(parts[0], FORMATTER);
        String goal = unescape(parts[1]);
        long minutes = Long.parseLong(parts[2]);
        int intervals = Integer.parseInt(parts[3]);
        return new SessionLogEntry(time, goal, minutes, intervals);
    }

    private static String escape(String text) {
        return text.replace("\\", "\\\\").replace(",", "\\,");
    }

    private static String unescape(String text) {
        StringBuilder builder = new StringBuilder();
        boolean escaping = false;
        for (char ch : text.toCharArray()) {
            if (escaping) {
                builder.append(ch);
                escaping = false;
            } else if (ch == '\\') {
                escaping = true;
            } else {
                builder.append(ch);
            }
        }
        return builder.toString();
    }

    private static String[] splitEscaped(String row) {
        java.util.List<String> parts = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean escaping = false;
        for (char ch : row.toCharArray()) {
            if (escaping) {
                current.append(ch);
                escaping = false;
            } else if (ch == '\\') {
                escaping = true;
            } else if (ch == ',') {
                parts.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        parts.add(current.toString());
        return parts.toArray(new String[0]);
    }
}
