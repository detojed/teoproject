package com.teoproject.pomodoro;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles persistence of session data to a CSV log file and provides utilities for reading the file.
 */
public class SessionLogger {
    private static final String DEFAULT_LOG_PATH = "session_log.csv";

    private final Path logPath;

    public SessionLogger() {
        this(DEFAULT_LOG_PATH);
    }

    public SessionLogger(String path) {
        this.logPath = Paths.get(path);
    }

    public synchronized void appendEntry(String goalDescription, long focusMinutes, int intervalsCompleted) throws IOException {
        ensureFileExists();
        SessionLogEntry entry = new SessionLogEntry(LocalDateTime.now(), goalDescription, focusMinutes, intervalsCompleted);
        try (BufferedWriter writer = Files.newBufferedWriter(logPath, StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.APPEND)) {
            writer.write(entry.toCsvRow());
            writer.newLine();
        }
    }

    public synchronized List<SessionLogEntry> readAllEntries() throws IOException {
        ensureFileExists();
        List<SessionLogEntry> entries = new ArrayList<>();
        for (String line : Files.readAllLines(logPath, StandardCharsets.UTF_8)) {
            if (!line.isBlank()) {
                try {
                    entries.add(SessionLogEntry.fromCsvRow(line));
                } catch (IllegalArgumentException ex) {
                    System.err.println("Skipping invalid log line: " + line);
                }
            }
        }
        return entries;
    }

    private void ensureFileExists() throws IOException {
        if (!Files.exists(logPath)) {
            Files.createFile(logPath);
        }
    }

    public Path getLogPath() {
        return logPath;
    }
}
