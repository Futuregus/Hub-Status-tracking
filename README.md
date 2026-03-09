# Hub Status Tracker

This is a simple class for FRC REBUILT (2026) that tracks whether the HUB is active for your alliance and shows a countdown timer for when the hub status changes.

**What it does:**
- Tells you if your HUB is active right now (true/false)
- Counts down time until the next change
- Special countdowns: 35s if you lost auto 10s if you won auto, and 30s for endgame
- Uses a local timer (more reliable than DriverStation.getMatchTime())

See `Robot.java` for a example of how to use it.
