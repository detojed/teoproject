package com.teoproject.pomodoro;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

/**
 * Entry point for the Pomodoro study application. Provides a simple console interface for
 * configuring study goals, running timed sessions, and reviewing productivity statistics.
 */
public class PomodoroApp {

    private final Scanner scanner = new Scanner(System.in);
    private final PomodoroConfiguration configuration = new PomodoroConfiguration();
    private final SessionLogger sessionLogger = new SessionLogger();
    private final StatisticsCalculator statisticsCalculator = new StatisticsCalculator();

    public static void main(String[] args) {
        PomodoroApp app = new PomodoroApp();
        app.run();
    }

    private void run() {
        System.out.println("Pomodoro Timer with Goal and Statistics Tracking");
        System.out.println("==================================================");
        boolean running = true;
        while (running) {
            showMainMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> configureStudyGoal();
                case "2" -> configurePomodoroSettings();
                case "3" -> startPomodoroSession();
                case "4" -> displayWeeklyStatistics();
                case "5" -> {
                    running = false;
                    System.out.println("Good luck with your studies!");
                }
                default -> System.out.println("Please enter a valid option (1-5).");
            }
        }
    }

    private void showMainMenu() {
        System.out.println();
        System.out.println("Main Menu");
        System.out.println("---------");
        System.out.println("1. Set study goal");
        System.out.println("2. Configure Pomodoro timings");
        System.out.println("3. Start study session");
        System.out.println("4. View weekly statistics");
        System.out.println("5. Exit");
        System.out.print("Select an option: ");
    }

    private void configureStudyGoal() {
        System.out.println();
        System.out.println("Set Study Goal");
        System.out.println("---------------");
        System.out.print("Enter your goal description (e.g. 'Two hours of maths revision'): ");
        String goal = scanner.nextLine().trim();
        while (goal.isBlank()) {
            System.out.print("The goal description cannot be empty. Please enter a description: ");
            goal = scanner.nextLine().trim();
        }
        configuration.setGoalDescription(goal);

        int minutes = readNonNegativeInt("Enter the number of minutes you aim to study this session (0 to skip): ", true);
        configuration.setGoalTargetMinutes(minutes);
        System.out.println("Goal saved: " + goal + (minutes > 0 ? " (" + minutes + " minutes)" : ""));
    }

    private void configurePomodoroSettings() {
        System.out.println();
        System.out.println("Configure Pomodoro Timings");
        System.out.println("---------------------------");
        System.out.println("Press Enter to keep the current value.");

        int work = readOptionalInt(
                "Work interval length in minutes [current: " + configuration.getWorkDurationMinutes() + "]: ",
                configuration.getWorkDurationMinutes(), 1);
        configuration.setWorkDurationMinutes(work);

        int rest = readOptionalInt(
                "Break interval length in minutes [current: " + configuration.getBreakDurationMinutes() + "]: ",
                configuration.getBreakDurationMinutes(), 0);
        configuration.setBreakDurationMinutes(rest);

        int intervals = readOptionalInt(
                "Number of focus intervals per session [current: " + configuration.getIntervalsPerSession() + "]: ",
                configuration.getIntervalsPerSession(), 1);
        configuration.setIntervalsPerSession(intervals);

        System.out.println("Updated Pomodoro settings saved.");
    }

    private void startPomodoroSession() {
        if (configuration.getGoalDescription().isBlank()) {
            System.out.println("Please set a study goal before starting a session (option 1).");
            return;
        }

        System.out.println();
        System.out.println("Starting Pomodoro session for goal: " + configuration.getGoalDescription());
        if (configuration.getGoalTargetMinutes() > 0) {
            System.out.println("Target study minutes for this session: " + configuration.getGoalTargetMinutes());
        }
        System.out.println("Each focus interval lasts " + configuration.getWorkDurationMinutes() + " minutes with "
                + configuration.getBreakDurationMinutes() + " minute breaks.");
        System.out.println("Total intervals this session: " + configuration.getIntervalsPerSession());

        PomodoroTimer timer = new PomodoroTimer(
                configuration.getWorkDurationMinutes(),
                configuration.getBreakDurationMinutes(),
                configuration.getIntervalsPerSession());
        Thread timerThread = new Thread(timer, "Pomodoro-Timer");
        timerThread.start();

        boolean sessionStoppedByUser = handleSessionCommands(timer);
        try {
            timerThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Timer thread was interrupted.");
        }

        PomodoroTimer.TimerStatus status = timer.getStatus();
        long focusMinutes = Math.round(timer.getTotalFocusSeconds() / 60.0);
        if (timer.getTotalFocusSeconds() > 0) {
            try {
                sessionLogger.appendEntry(configuration.getGoalDescription(), focusMinutes, timer.getCompletedIntervals());
                System.out.println("Session details saved to log file at " + sessionLogger.getLogPath().toAbsolutePath());
            } catch (IOException e) {
                System.err.println("Failed to write session log: " + e.getMessage());
            }
        }

        if (timer.isCompleted()) {
            System.out.println("Congratulations! You completed all " + timer.getIntervalsPerSession() + " intervals.");
        } else if (sessionStoppedByUser) {
            System.out.println("Session stopped early. Completed intervals: " + timer.getCompletedIntervals());
        } else {
            System.out.println("Session ended unexpectedly.");
        }
    }

    private boolean handleSessionCommands(PomodoroTimer timer) {
        System.out.println();
        System.out.println("Session running. Enter commands as needed:");
        System.out.println(" - pause: pause the current interval");
        System.out.println(" - resume: resume a paused interval");
        System.out.println(" - status: show the time remaining and progress");
        System.out.println(" - reset: stop the session early");
        System.out.println(" - exit: stop the session early and return to menu");
        boolean stoppedByUser = false;

        while (timer.isRunning()) {
            System.out.print("Command> ");
            String input;
            try {
                input = scanner.nextLine().trim().toLowerCase();
            } catch (Exception ex) {
                timer.requestStop();
                break;
            }
            switch (input) {
                case "pause" -> {
                    timer.pause();
                    System.out.println("Timer paused.");
                }
                case "resume" -> {
                    timer.resume();
                    System.out.println("Timer resumed.");
                }
                case "status" -> printStatus(timer);
                case "reset", "exit", "stop" -> {
                    timer.requestStop();
                    stoppedByUser = true;
                }
                case "" -> printStatus(timer);
                default -> System.out.println("Unknown command. Try pause, resume, status, or reset.");
            }
        }

        return stoppedByUser;
    }

    private void printStatus(PomodoroTimer timer) {
        PomodoroTimer.TimerStatus status = timer.getStatus();
        String phase = switch (status.getPhase()) {
            case WORK -> "Focus";
            case BREAK -> "Break";
            case COMPLETE -> "Complete";
            case IDLE -> "Idle";
        };
        long minutes = status.getRemaining().toMinutes();
        long seconds = status.getRemaining().minusMinutes(minutes).getSeconds();
        System.out.printf("Phase: %s | Time remaining: %02d:%02d | Intervals: %d/%d%s%n",
                phase,
                minutes,
                seconds,
                status.getCompletedIntervals(),
                status.getIntervalsPerSession(),
                status.isPaused() ? " (paused)" : "");
    }

    private void displayWeeklyStatistics() {
        System.out.println();
        System.out.println("Weekly Statistics");
        System.out.println("-----------------");
        try {
            List<SessionLogEntry> entries = sessionLogger.readAllEntries();
            StatisticsCalculator.WeeklyStatistics stats = statisticsCalculator.calculateWeeklyStatistics(entries, LocalDate.now());
            System.out.printf("Sessions completed this week: %d%n", stats.getSessionsCompleted());
            System.out.printf("Total focus time this week: %.2f hours (%d minutes)%n", stats.getTotalHours(), stats.getTotalMinutes());
            System.out.printf("Intervals completed this week: %d%n", stats.getIntervalsCompleted());
            if (configuration.getGoalTargetMinutes() > 0) {
                double progress = stats.getTotalMinutes() / (double) configuration.getGoalTargetMinutes() * 100;
                System.out.printf("Progress toward current goal: %.1f%%%n", progress);
            }
        } catch (IOException e) {
            System.err.println("Could not read session log: " + e.getMessage());
        }
    }

    private int readNonNegativeInt(String prompt, boolean allowZero) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isBlank()) {
                if (allowZero) {
                    return 0;
                }
                System.out.println("Value is required. Please enter a number.");
                continue;
            }
            try {
                int value = Integer.parseInt(input);
                if (value < 0 || (!allowZero && value == 0)) {
                    System.out.println("Please enter a positive number.");
                } else {
                    return value;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            }
        }
    }

    private int readOptionalInt(String prompt, int currentValue, int minimum) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isBlank()) {
                return currentValue;
            }
            try {
                int value = Integer.parseInt(input);
                if (value < minimum) {
                    System.out.println("Please enter a number greater than or equal to " + minimum + ".");
                } else {
                    return value;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            }
        }
    }
}
