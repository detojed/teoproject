package com.teoproject.pomodoro;

/**
 * Listener for receiving updates from a {@link PomodoroTimer} as it progresses.
 */
public interface PomodoroTimerListener {

    /**
     * Called whenever the timer has new status information (e.g. every second or phase change).
     *
     * @param status snapshot of the timer state
     */
    void onStatusUpdate(PomodoroTimer.TimerStatus status);

    /**
     * Called when the timer session finishes, either by completing all intervals or being stopped early.
     *
     * @param completed {@code true} if every interval was finished, {@code false} otherwise
     */
    void onSessionFinished(boolean completed);
}
