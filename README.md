# Pomodoro Timer with Goal and Statistics Tracking

A cross-platform Java desktop application built to help students such as Gleb stay focused during
study sessions. The tool implements the Pomodoro technique, stores progress across sessions, and
shows weekly productivity statistics without requiring an internet connection. A distraction-free
Swing interface keeps every control—goal setting, timer management, configuration, and statistics—
within a single window.

## Features

- Configure a custom study goal before each session, including a description and optional target
  duration in minutes directly from the timer tab.
- Adjustable Pomodoro timings: choose the length of focus intervals, break durations, and the number
  of intervals per session from the dedicated settings tab.
- Real-time session controls (start, pause, resume, reset) that keep the timer running while you
  interact with the graphical interface.
- Automatic logging of every session (date, focus minutes, intervals completed) to a local CSV file
  so progress is preserved between runs.
- Weekly statistics that summarise total study time, sessions completed, and Pomodoro intervals
  achieved, plus a log viewer, to provide feedback on study habits.

All functionality works entirely offline so there are no distractions from internet-connected apps.

## Getting Started

1. Ensure you have Java 17 or later installed.
2. Build the Maven project (this creates `target/pomodoro-app-1.0.0.jar`):

   ```bash
   mvn clean package
   ```

3. Run the packaged application (the Swing interface will appear):

   ```bash
   java -jar target/pomodoro-app-1.0.0.jar
   ```

4. Use the tabs to set a study goal, adjust timings, start a session, and review weekly statistics
   and past logs—all without leaving the window.

Session logs are saved to `~/.pomodoro-tracker/session_log.csv`. The directory is created
automatically the first time you run the application so that logs persist across IDEs and different
launch locations.

## Running the project in NetBeans IDE

You can also work with the application inside NetBeans if you prefer an IDE workflow:

1. **Start NetBeans** and choose **File → Open Project…**
2. Navigate to the repository folder (the one containing `pom.xml`), select it, and click **Open Project**. NetBeans recognises Maven projects automatically.
3. Once the project loads, you can run it via **Run → Run Project** (default shortcut `F6`). NetBeans uses Maven behind the scenes to compile and execute `com.teoproject.pomodoro.PomodoroApp`.
4. Session logs are written to `~/.pomodoro-tracker/session_log.csv`, independent of the working directory NetBeans uses. Open the file from the IDE if you would like to inspect previous sessions.

These steps do not require any internet connectivity—the IDE runs the same offline Java application described above and caches Maven dependencies locally.

## Project Structure

```
pom.xml                          # Maven configuration recognised by NetBeans
src/main/java/com/teoproject/pomodoro/
├── PomodoroApp.java             # Swing interface and application entry point
├── PomodoroConfiguration.java   # Stores goal and timer settings
├── PomodoroTimer.java           # Manages Pomodoro timing logic and commands
├── PomodoroTimerListener.java   # Listener interface for timer updates
├── SessionLogEntry.java         # Represents persisted study sessions
├── SessionLogger.java           # Reads/writes session logs to disk
└── StatisticsCalculator.java    # Calculates weekly study statistics
```

Feel free to adapt the timings and workflow to suit different study routines or expand the interface
with additional productivity insights.
