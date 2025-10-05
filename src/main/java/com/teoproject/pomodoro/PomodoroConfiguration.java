package com.teoproject.pomodoro;

/**
 * Holds configurable values for a Pomodoro study session.
 */
public class PomodoroConfiguration {
    private String goalDescription = "";
    private int workDurationMinutes = 25;
    private int breakDurationMinutes = 5;
    private int intervalsPerSession = 4;
    private int goalTargetMinutes = 0;

    public String getGoalDescription() {
        return goalDescription;
    }

    public void setGoalDescription(String goalDescription) {
        this.goalDescription = goalDescription;
    }

    public int getWorkDurationMinutes() {
        return workDurationMinutes;
    }

    public void setWorkDurationMinutes(int workDurationMinutes) {
        if (workDurationMinutes <= 0) {
            throw new IllegalArgumentException("Work duration must be positive.");
        }
        this.workDurationMinutes = workDurationMinutes;
    }

    public int getBreakDurationMinutes() {
        return breakDurationMinutes;
    }

    public void setBreakDurationMinutes(int breakDurationMinutes) {
        if (breakDurationMinutes < 0) {
            throw new IllegalArgumentException("Break duration cannot be negative.");
        }
        this.breakDurationMinutes = breakDurationMinutes;
    }

    public int getIntervalsPerSession() {
        return intervalsPerSession;
    }

    public void setIntervalsPerSession(int intervalsPerSession) {
        if (intervalsPerSession <= 0) {
            throw new IllegalArgumentException("Intervals per session must be positive.");
        }
        this.intervalsPerSession = intervalsPerSession;
    }

    public int getGoalTargetMinutes() {
        return goalTargetMinutes;
    }

    public void setGoalTargetMinutes(int goalTargetMinutes) {
        if (goalTargetMinutes < 0) {
            throw new IllegalArgumentException("Goal target minutes cannot be negative.");
        }
        this.goalTargetMinutes = goalTargetMinutes;
    }
}
