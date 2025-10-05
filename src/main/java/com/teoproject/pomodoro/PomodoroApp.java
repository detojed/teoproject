package com.teoproject.pomodoro;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Graphical Pomodoro application that allows goal setting, timer control, configuration updates,
 * and statistics review without leaving the desktop interface.
 */
public class PomodoroApp extends JFrame {

    private final PomodoroConfiguration configuration = new PomodoroConfiguration();
    private final SessionLogger sessionLogger = new SessionLogger();
    private final StatisticsCalculator statisticsCalculator = new StatisticsCalculator();

    private PomodoroTimer currentTimer;

    private JTextField goalDescriptionField;
    private JSpinner goalMinutesSpinner;
    private JLabel timerDisplayLabel;
    private JLabel phaseLabel;
    private JLabel intervalsLabel;
    private JLabel statusLabel;
    private JProgressBar phaseProgressBar;
    private JButton startButton;
    private JButton pauseButton;
    private JButton resumeButton;
    private JButton resetButton;

    private JSpinner workDurationSpinner;
    private JSpinner breakDurationSpinner;
    private JSpinner intervalsSpinner;

    private JLabel totalMinutesLabel;
    private JLabel totalHoursLabel;
    private JLabel sessionsCompletedLabel;
    private JLabel intervalsCompletedLabel;
    private JLabel logLocationLabel;
    private DefaultTableModel logTableModel;

    private PomodoroTimer.Phase lastKnownPhase = PomodoroTimer.Phase.IDLE;
    private long activeWorkSeconds;
    private long activeBreakSeconds;

    private final PomodoroTimerListener swingTimerListener = new PomodoroTimerListener() {
        @Override
        public void onStatusUpdate(PomodoroTimer.TimerStatus status) {
            SwingUtilities.invokeLater(() -> updateTimerStatus(status));
        }

        @Override
        public void onSessionFinished(boolean completed) {
            SwingUtilities.invokeLater(() -> handleTimerFinished(completed));
        }
    };

    public PomodoroApp() {
        super("Pomodoro Timer with Goals & Statistics");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        buildUi();
        refreshStatistics();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Use default look and feel if the system one is unavailable.
            }
            new PomodoroApp().setVisible(true);
        });
    }

    private void buildUi() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Timer", buildTimerPanel());
        tabs.addTab("Settings", buildSettingsPanel());
        tabs.addTab("Statistics", buildStatisticsPanel());
        add(tabs, BorderLayout.CENTER);
    }

    private JComponent buildTimerPanel() {
        JPanel panel = new JPanel(new BorderLayout(24, 24));
        panel.setBorder(new EmptyBorder(24, 24, 24, 24));

        JPanel goalPanel = new JPanel(new GridBagLayout());
        goalPanel.setBorder(BorderFactory.createTitledBorder("Study Goal"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        goalPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        goalDescriptionField = new JTextField(configuration.getGoalDescription());
        goalPanel.add(goalDescriptionField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        goalPanel.add(new JLabel("Target minutes:"), gbc);
        gbc.gridx = 1;
        goalMinutesSpinner = new JSpinner(new SpinnerNumberModel(configuration.getGoalTargetMinutes(), 0, 600, 5));
        goalPanel.add(goalMinutesSpinner, gbc);

        panel.add(goalPanel, BorderLayout.NORTH);

        JPanel statusPanel = new JPanel(new GridBagLayout());
        statusPanel.setBorder(BorderFactory.createTitledBorder("Session Status"));
        GridBagConstraints statusGbc = new GridBagConstraints();
        statusGbc.insets = new Insets(6, 6, 6, 6);
        statusGbc.gridx = 0;
        statusGbc.gridy = 0;
        statusGbc.anchor = GridBagConstraints.CENTER;
        statusGbc.gridwidth = 2;

        timerDisplayLabel = new JLabel("00:00");
        timerDisplayLabel.setFont(timerDisplayLabel.getFont().deriveFont(Font.BOLD, 48f));
        statusPanel.add(timerDisplayLabel, statusGbc);

        statusGbc.gridy = 1;
        phaseLabel = new JLabel("Phase: Idle");
        phaseLabel.setFont(phaseLabel.getFont().deriveFont(Font.PLAIN, 18f));
        statusPanel.add(phaseLabel, statusGbc);

        statusGbc.gridy = 2;
        intervalsLabel = new JLabel("Intervals: 0 / 0");
        statusPanel.add(intervalsLabel, statusGbc);

        statusGbc.gridy = 3;
        phaseProgressBar = new JProgressBar(0, 100);
        phaseProgressBar.setStringPainted(true);
        phaseProgressBar.setPreferredSize(new Dimension(400, 30));
        phaseProgressBar.setString("0%");
        statusPanel.add(phaseProgressBar, statusGbc);

        statusGbc.gridy = 4;
        statusLabel = new JLabel("Ready to begin your next focus session.");
        statusPanel.add(statusLabel, statusGbc);

        panel.add(statusPanel, BorderLayout.CENTER);

        JPanel controlsPanel = new JPanel();
        controlsPanel.setBorder(new EmptyBorder(8, 0, 0, 0));

        startButton = new JButton("Start Session");
        startButton.addActionListener(e -> startSession());
        controlsPanel.add(startButton);

        pauseButton = new JButton("Pause");
        pauseButton.addActionListener(e -> pauseTimer());
        controlsPanel.add(pauseButton);

        resumeButton = new JButton("Resume");
        resumeButton.addActionListener(e -> resumeTimer());
        controlsPanel.add(resumeButton);

        resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> resetTimer());
        controlsPanel.add(resetButton);

        panel.add(controlsPanel, BorderLayout.SOUTH);
        updateControlState();
        return panel;
    }

    private JComponent buildSettingsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(32, 32, 32, 32));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        panel.add(new JLabel("Work interval (minutes):"), gbc);
        gbc.gridx = 1;
        workDurationSpinner = new JSpinner(new SpinnerNumberModel(configuration.getWorkDurationMinutes(), 1, 180, 1));
        panel.add(workDurationSpinner, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Break interval (minutes):"), gbc);
        gbc.gridx = 1;
        breakDurationSpinner = new JSpinner(new SpinnerNumberModel(configuration.getBreakDurationMinutes(), 0, 120, 1));
        panel.add(breakDurationSpinner, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Intervals per session:"), gbc);
        gbc.gridx = 1;
        intervalsSpinner = new JSpinner(new SpinnerNumberModel(configuration.getIntervalsPerSession(), 1, 12, 1));
        panel.add(intervalsSpinner, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        JButton applyButton = new JButton("Save Settings");
        applyButton.addActionListener(e -> applySettings());
        panel.add(applyButton, gbc);

        gbc.gridy = 4;
        JLabel hintLabel = new JLabel("Adjust timings to suit each study block. Changes apply to new sessions.");
        hintLabel.setForeground(Color.DARK_GRAY);
        panel.add(hintLabel, gbc);

        return panel;
    }

    private JComponent buildStatisticsPanel() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setBorder(new EmptyBorder(24, 24, 24, 24));

        JPanel summaryPanel = new JPanel(new GridBagLayout());
        summaryPanel.setBorder(BorderFactory.createTitledBorder("This Week"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        summaryPanel.add(new JLabel("Total minutes:"), gbc);
        gbc.gridx = 1;
        totalMinutesLabel = new JLabel("0");
        summaryPanel.add(totalMinutesLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        summaryPanel.add(new JLabel("Total hours:"), gbc);
        gbc.gridx = 1;
        totalHoursLabel = new JLabel("0.0");
        summaryPanel.add(totalHoursLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        summaryPanel.add(new JLabel("Sessions completed:"), gbc);
        gbc.gridx = 1;
        sessionsCompletedLabel = new JLabel("0");
        summaryPanel.add(sessionsCompletedLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        summaryPanel.add(new JLabel("Intervals completed:"), gbc);
        gbc.gridx = 1;
        intervalsCompletedLabel = new JLabel("0");
        summaryPanel.add(intervalsCompletedLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        JButton refreshButton = new JButton("Refresh Statistics");
        refreshButton.addActionListener(e -> refreshStatistics());
        summaryPanel.add(refreshButton, gbc);

        panel.add(summaryPanel, BorderLayout.NORTH);

        logTableModel = new DefaultTableModel(new Object[] {"Date", "Goal", "Focus (min)", "Intervals"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable logTable = new JTable(logTableModel);
        logTable.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
        logTable.getColumnModel().getColumn(0).setPreferredWidth(160);
        logTable.getColumnModel().getColumn(1).setPreferredWidth(260);

        panel.add(new JScrollPane(logTable), BorderLayout.CENTER);

        logLocationLabel = new JLabel();
        panel.add(logLocationLabel, BorderLayout.SOUTH);
        return panel;
    }

    private void startSession() {
        if (currentTimer != null && currentTimer.isRunning()) {
            JOptionPane.showMessageDialog(this, "A session is already running.", "Timer Active", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String goalDescription = goalDescriptionField.getText().trim();
        if (goalDescription.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a study goal before starting.", "Goal Required", JOptionPane.WARNING_MESSAGE);
            goalDescriptionField.requestFocusInWindow();
            return;
        }

        configuration.setGoalDescription(goalDescription);
        configuration.setGoalTargetMinutes((Integer) goalMinutesSpinner.getValue());

        int workMinutes = (Integer) workDurationSpinner.getValue();
        int breakMinutes = (Integer) breakDurationSpinner.getValue();
        int intervals = (Integer) intervalsSpinner.getValue();

        try {
            configuration.setWorkDurationMinutes(workMinutes);
            configuration.setBreakDurationMinutes(breakMinutes);
            configuration.setIntervalsPerSession(intervals);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid Settings", JOptionPane.ERROR_MESSAGE);
            return;
        }

        currentTimer = new PomodoroTimer(workMinutes, breakMinutes, intervals);
        currentTimer.addListener(swingTimerListener);
        activeWorkSeconds = workMinutes * 60L;
        activeBreakSeconds = breakMinutes * 60L;
        lastKnownPhase = PomodoroTimer.Phase.IDLE;
        intervalsLabel.setText(String.format("Intervals: %d / %d", 0, intervals));
        phaseProgressBar.setValue(0);
        phaseProgressBar.setString("0%");

        Thread timerThread = new Thread(currentTimer, "Pomodoro-Timer");
        timerThread.start();
        updateControlState();
        statusLabel.setText("Focus time underway. Stay on task!");
    }

    private void pauseTimer() {
        if (currentTimer != null) {
            currentTimer.pause();
            updateControlState();
            statusLabel.setText("Timer paused. Resume when ready.");
        }
    }

    private void resumeTimer() {
        if (currentTimer != null) {
            currentTimer.resume();
            updateControlState();
            statusLabel.setText("Back to focusing!");
        }
    }

    private void resetTimer() {
        if (currentTimer != null) {
            currentTimer.requestStop();
        }
    }

    private void applySettings() {
        try {
            configuration.setWorkDurationMinutes((Integer) workDurationSpinner.getValue());
            configuration.setBreakDurationMinutes((Integer) breakDurationSpinner.getValue());
            configuration.setIntervalsPerSession((Integer) intervalsSpinner.getValue());
            JOptionPane.showMessageDialog(this, "Settings saved. They will apply to new sessions.", "Settings Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid Settings", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTimerStatus(PomodoroTimer.TimerStatus status) {
        PomodoroTimer.Phase phase = status.getPhase();
        if (phase != lastKnownPhase) {
            lastKnownPhase = phase;
            switch (phase) {
                case WORK -> statusLabel.setText("Deep focus interval in progress.");
                case BREAK -> statusLabel.setText(activeBreakSeconds > 0 ? "Break time. Stretch and reset." : "Moving to the next interval.");
                case COMPLETE -> statusLabel.setText("Session complete.");
                case IDLE -> statusLabel.setText("Ready to begin your next focus session.");
            }
        }

        String phaseName = switch (phase) {
            case WORK -> "Focus";
            case BREAK -> "Break";
            case COMPLETE -> "Complete";
            case IDLE -> "Idle";
        };
        phaseLabel.setText("Phase: " + phaseName + (status.isPaused() ? " (paused)" : ""));

        long totalSeconds = Math.max(0, status.getRemaining().getSeconds());
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        timerDisplayLabel.setText(String.format("%02d:%02d", minutes, seconds));

        intervalsLabel.setText(String.format("Intervals: %d / %d", status.getCompletedIntervals(), status.getIntervalsPerSession()));

        long phaseLengthSeconds = switch (phase) {
            case WORK -> activeWorkSeconds;
            case BREAK -> activeBreakSeconds;
            default -> 0L;
        };

        if (phaseLengthSeconds > 0) {
            long elapsed = Math.max(0, phaseLengthSeconds - totalSeconds);
            int progress = (int) Math.min(100, Math.round((elapsed / (double) phaseLengthSeconds) * 100));
            phaseProgressBar.setValue(progress);
            phaseProgressBar.setString(progress + "%");
        } else {
            phaseProgressBar.setValue(0);
            phaseProgressBar.setString("0%");
        }

        updateControlState();
    }

    private void handleTimerFinished(boolean completed) {
        updateControlState();

        if (currentTimer == null) {
            return;
        }

        long focusMinutes = Math.round(currentTimer.getTotalFocusSeconds() / 60.0);
        int completedIntervals = currentTimer.getCompletedIntervals();

        if (focusMinutes > 0 || completedIntervals > 0) {
            try {
                sessionLogger.appendEntry(configuration.getGoalDescription(), focusMinutes, completedIntervals);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Could not write to the session log: " + ex.getMessage(), "Logging Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        String message = completed
                ? String.format("Fantastic! You completed all %d intervals and logged %d minutes of focus.", completedIntervals, focusMinutes)
                : String.format("Session ended early. You logged %d minutes across %d intervals.", focusMinutes, completedIntervals);
        JOptionPane.showMessageDialog(this, message, "Session Summary", JOptionPane.INFORMATION_MESSAGE);

        refreshStatistics();
        currentTimer.removeListener(swingTimerListener);
        currentTimer = null;
        lastKnownPhase = PomodoroTimer.Phase.IDLE;
        statusLabel.setText("Ready to begin your next focus session.");
        timerDisplayLabel.setText("00:00");
        intervalsLabel.setText("Intervals: 0 / " + intervalsSpinner.getValue());
        phaseProgressBar.setValue(0);
        phaseProgressBar.setString("0%");
    }

    private void refreshStatistics() {
        try {
            List<SessionLogEntry> entries = sessionLogger.readAllEntries();
            StatisticsCalculator.WeeklyStatistics weeklyStats = statisticsCalculator.calculateWeeklyStatistics(entries, java.time.LocalDate.now());

            totalMinutesLabel.setText(Long.toString(weeklyStats.getTotalMinutes()));
            totalHoursLabel.setText(String.format("%.2f", weeklyStats.getTotalHours()));
            sessionsCompletedLabel.setText(Integer.toString(weeklyStats.getSessionsCompleted()));
            intervalsCompletedLabel.setText(Integer.toString(weeklyStats.getIntervalsCompleted()));

            populateLogTable(entries);
            logLocationLabel.setText("Log file: " + sessionLogger.getLogPath().toAbsolutePath());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Unable to read the session log: " + ex.getMessage(), "Log Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateLogTable(List<SessionLogEntry> entries) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        logTableModel.setRowCount(0);
        for (SessionLogEntry entry : entries) {
            logTableModel.addRow(new Object[] {
                    entry.getTimestamp().format(formatter),
                    entry.getGoalDescription(),
                    entry.getFocusMinutes(),
                    entry.getIntervalsCompleted()
            });
        }
    }

    private void updateControlState() {
        boolean running = currentTimer != null && currentTimer.isRunning();
        boolean paused = currentTimer != null && currentTimer.isPaused();

        startButton.setEnabled(!running);
        pauseButton.setEnabled(running && !paused);
        resumeButton.setEnabled(running && paused);
        resetButton.setEnabled(running);
    }
}
