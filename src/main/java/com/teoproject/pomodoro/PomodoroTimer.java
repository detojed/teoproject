package com.teoproject.pomodoro;

import java.time.Duration;

/**
 * Controls the execution of a Pomodoro session by managing alternating work and break periods.
 * The timer runs on its own thread so the user interface can issue commands such as pause, resume,
 * and reset while a session is active.
 */
public class PomodoroTimer implements Runnable {
    public enum Phase {
        WORK,
        BREAK,
        COMPLETE,
        IDLE
    }

    private final Object lock = new Object();
    private final int workDurationMinutes;
    private final int breakDurationMinutes;
    private final int intervalsPerSession;

    private volatile boolean running;
    private volatile boolean paused;
    private volatile boolean stopRequested;
    private volatile boolean completed;

    private Phase currentPhase = Phase.IDLE;
    private int remainingSeconds;
    private int completedIntervals;
    private long totalFocusSeconds;

    public PomodoroTimer(int workDurationMinutes, int breakDurationMinutes, int intervalsPerSession) {
        this.workDurationMinutes = workDurationMinutes;
        this.breakDurationMinutes = breakDurationMinutes;
        this.intervalsPerSession = intervalsPerSession;
    }

    @Override
    public void run() {
        running = true;
        stopRequested = false;
        completed = false;
        completedIntervals = 0;
        totalFocusSeconds = 0;

        try {
            for (int i = 0; i < intervalsPerSession && !stopRequested; i++) {
                if (!runPhase(workDurationMinutes * 60, Phase.WORK)) {
                    finish(false);
                    return;
                }

                if (stopRequested) {
                    finish(false);
                    return;
                }

                if (i < intervalsPerSession - 1) {
                    if (!runPhase(breakDurationMinutes * 60, Phase.BREAK)) {
                        finish(false);
                        return;
                    }
                }
            }
        } finally {
            if (!stopRequested && completedIntervals == intervalsPerSession) {
                finish(true);
            } else if (stopRequested) {
                finish(false);
            }
        }
    }

    private boolean runPhase(int totalSeconds, Phase phase) {
        synchronized (lock) {
            currentPhase = phase;
            remainingSeconds = totalSeconds;
        }

        while (remainingSeconds > 0) {
            synchronized (lock) {
                if (stopRequested) {
                    return false;
                }
                while (paused && !stopRequested) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                }
                if (stopRequested) {
                    return false;
                }
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }

            synchronized (lock) {
                remainingSeconds--;
                if (phase == Phase.WORK) {
                    totalFocusSeconds++;
                }
            }
        }

        if (phase == Phase.WORK) {
            completedIntervals++;
        }

        return true;
    }

    private void finish(boolean wasCompleted) {
        synchronized (lock) {
            running = false;
            paused = false;
            completed = wasCompleted;
            currentPhase = Phase.COMPLETE;
            remainingSeconds = 0;
            lock.notifyAll();
        }
    }

    public void pause() {
        synchronized (lock) {
            if (running && !paused) {
                paused = true;
            }
        }
    }

    public void resume() {
        synchronized (lock) {
            if (running && paused) {
                paused = false;
                lock.notifyAll();
            }
        }
    }

    public void requestStop() {
        synchronized (lock) {
            stopRequested = true;
            paused = false;
            lock.notifyAll();
        }
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isPaused() {
        return paused;
    }

    public boolean isCompleted() {
        return completed;
    }

    public TimerStatus getStatus() {
        synchronized (lock) {
            Duration remaining = Duration.ofSeconds(Math.max(0, remainingSeconds));
            return new TimerStatus(currentPhase, remaining, completedIntervals, intervalsPerSession, paused);
        }
    }

    public int getCompletedIntervals() {
        return completedIntervals;
    }

    public long getTotalFocusSeconds() {
        return totalFocusSeconds;
    }

    public int getIntervalsPerSession() {
        return intervalsPerSession;
    }

    public enum PhaseStatus {
        ACTIVE,
        PAUSED,
        COMPLETE
    }

    public static class TimerStatus {
        private final Phase phase;
        private final Duration remaining;
        private final int completedIntervals;
        private final int intervalsPerSession;
        private final boolean paused;

        public TimerStatus(Phase phase, Duration remaining, int completedIntervals, int intervalsPerSession, boolean paused) {
            this.phase = phase;
            this.remaining = remaining;
            this.completedIntervals = completedIntervals;
            this.intervalsPerSession = intervalsPerSession;
            this.paused = paused;
        }

        public Phase getPhase() {
            return phase;
        }

        public Duration getRemaining() {
            return remaining;
        }

        public int getCompletedIntervals() {
            return completedIntervals;
        }

        public int getIntervalsPerSession() {
            return intervalsPerSession;
        }

        public boolean isPaused() {
            return paused;
        }
    }
}
