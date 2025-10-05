# Pomodoro Timer with Goal and Statistics Tracking

A cross-platform Java console application built to help students such as Gleb stay focused during
study sessions. The tool implements the Pomodoro technique, stores progress across sessions, and
shows weekly productivity statistics without requiring an internet connection.

## Features

- Configure a custom study goal before each session, including a description and optional target
  duration in minutes.
- Adjustable Pomodoro timings: choose the length of focus intervals, break durations, and the number
  of intervals per session.
- Real-time session controls (pause, resume, status, reset) that keep the timer running while you
  interact with the console.
- Automatic logging of every session (date, focus minutes, intervals completed) to a local CSV file
  so progress is preserved between runs.
- Weekly statistics that summarise total study time, sessions completed, and Pomodoro intervals
  achieved to provide feedback on study habits.

All functionality works entirely offline so there are no distractions from internet-connected apps.

## Getting Started

1. Ensure you have Java 17 or later installed.
2. Compile the project:

   ```bash
   mkdir -p out
   javac $(find src/main/java -name "*.java") -d out
   ```

3. Run the application:

   ```bash
   java -cp out com.teoproject.pomodoro.PomodoroApp
   ```

4. Follow the on-screen menu to set a study goal, adjust timings, start a session, and review weekly
   statistics.

Session logs are saved to `session_log.csv` in the project directory. You can back up or analyse this
file to review long-term trends.

## Project Structure

```
src/main/java/com/teoproject/pomodoro/
├── PomodoroApp.java           # Console interface and application entry point
├── PomodoroConfiguration.java # Stores goal and timer settings
├── PomodoroTimer.java         # Manages Pomodoro timing logic and commands
├── SessionLogEntry.java       # Represents persisted study sessions
├── SessionLogger.java         # Reads/writes session logs to disk
└── StatisticsCalculator.java  # Calculates weekly study statistics
```

Feel free to adapt the timings and workflow to suit different study routines or to add a graphical
interface in the future.
