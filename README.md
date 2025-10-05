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
2. Compile the project:

   ```bash
   mkdir -p out
   javac $(find src/main/java -name "*.java") -d out
   ```

3. Run the application (the Swing interface will appear):

   ```bash
   java -cp out com.teoproject.pomodoro.PomodoroApp
   ```

4. Use the tabs to set a study goal, adjust timings, start a session, and review weekly statistics
   and past logs—all without leaving the window.

Session logs are saved to `session_log.csv` in the project directory. You can back up or analyse this
file to review long-term trends.

## Running the project in NetBeans IDE

You can also work with the application inside NetBeans if you prefer an IDE workflow:

1. **Start NetBeans** and choose **File → New Project…**
2. Select **Java with Ant → Java Project with Existing Sources** (or the equivalent "with Existing Sources" option for your NetBeans version) and click **Next**.
3. Enter a project name such as `PomodoroTimer` and choose a location for the NetBeans project metadata.
4. In the **Source Package Folders** step, click **Add Folder…** and select the repository’s `src/main/java` directory. Click **Next**.
5. When prompted for the main class, set it to `com.teoproject.pomodoro.PomodoroApp`. Finish the wizard.
6. NetBeans will index the sources. Once complete, you can **Right-click the project → Run** (or press `Shift + F6`) to launch the graphical application. NetBeans automatically compiles the sources before running.
7. Session logs are still written to `session_log.csv` in the NetBeans project directory. You can open the file within NetBeans to review past sessions.

These steps do not require any internet connectivity—the IDE runs the same offline Java application described above.

## Project Structure

```
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
