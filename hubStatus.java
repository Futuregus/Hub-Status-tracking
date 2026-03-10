package frc.robot;

import java.util.Optional;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;

/**
 * this class allows you to track the status of the hub 
 */
public class hubStatus {

    public double hubStatusCountdown = 0.0;
    private boolean wasHubActive = false;


    /**
     * Determines if the hub is currently active based on the current match time, alliance, and game data.
     * @param MatchTime Current match time in seconds can be obtained from a timer
     * @return true if the hub is active for the current alliance, false otherwise
     */
    public boolean isHubActive(double MatchTime) {
        Optional<Alliance> allianceOpt = DriverStation.getAlliance();

        // No alliance, no hub
        if (allianceOpt.isEmpty()) {
            return false;
        }

        Alliance alliance = allianceOpt.get();
        boolean isRedAlliance = alliance == Alliance.Red;
        boolean isBlueAlliance = alliance == Alliance.Blue;

        String gameData = DriverStation.getGameSpecificMessage();
        double matchTime = MatchTime;

        // If we have no game data, assume hub is active (likely early in teleop)
        if (gameData == null || gameData.isEmpty()) {
            return true;
        }

        // Only evaluate hub during teleop
        if (!DriverStation.isTeleopEnabled()) {
            return false;
        }

        //
        if (matchTime <= 0.02) {
            return false;
        }
        // If match is over everything should stop
        if (matchTime >= 140) {
            hubStatusCountdown = 0.0;
            wasHubActive = false;
            return false;
        }
        char gd = gameData.charAt(0);
        boolean blueActiveFirst = gd == 'R';
        boolean redActiveFirst = gd == 'B';

        if (matchTime > 110) { // End game
            // During endgame, hub is active only while the endgame countdown is running.
            return hubStatusCountdown > 0;
        }

        // Shift 4 (85s-110s) and Shift 2 (35s-60s) hub active for the opposite alliance
        // as the active-first alliance
        if (matchTime > 85 || (matchTime > 35 && matchTime <= 60)) {
            return (blueActiveFirst && isRedAlliance) || (redActiveFirst && isBlueAlliance);
        }

        // Shift 3 (60s-85s) and Shift 1 (10s-35s) hub active for the active-first
        // alliance
        if (matchTime > 60 || (matchTime > 10 && matchTime <= 35)) {
            return (blueActiveFirst && isBlueAlliance) || (redActiveFirst && isRedAlliance);
        }

        // Transition (0s-10s)
        return true; // hub active for both alliances during transition shift
    }

    /**
     * Should be called periodically to work properly
     * @param isHubActiveNow use the isHubActive method to determine this value before calling startCountdown
     * @param matchTime current match time in seconds can be obtained from a timer
     */ 
    public void startCountdown(boolean isHubActiveNow, double matchTime) {
        // If match is over ensure everything is stopped
        if (matchTime >= 140) {
            hubStatusCountdown = 0.0;
            wasHubActive = false;
            return;
        }

        // start endgame countdown if we just entered endgame and no countdown is
        // running
        if (matchTime > 110 && hubStatusCountdown <= 0) {
            hubStatusCountdown = 30.0;
            wasHubActive = true;
            return;
        }

        if (isHubActiveNow != wasHubActive) {
            // Determine countdown length based on match time and alliance/game data
            String gameData = DriverStation.getGameSpecificMessage();
            Optional<Alliance> allianceOpt = DriverStation.getAlliance();

            // Default countdown
            double countdown = 25.0;

            // Special cases
            if (matchTime > 110) {
                countdown = 30.0;
            } else if (matchTime <= 10) { // Transition shift (0s-10s)
                if (gameData != null && !gameData.isEmpty() && !allianceOpt.isEmpty()) {
                    char gd = gameData.charAt(0);
                    boolean blueActiveFirst = gd == 'R';
                    boolean redActiveFirst = gd == 'B';
                    Alliance alliance = allianceOpt.get();
                    boolean isRedAlliance = alliance == Alliance.Red;
                    boolean isBlueAlliance = alliance == Alliance.Blue;

                    boolean ourAllianceIsActiveFirst = (blueActiveFirst && isBlueAlliance)
                            || (redActiveFirst && isRedAlliance);

                    // If our alliance is active first longer countdown 35s, else 10s
                    countdown = ourAllianceIsActiveFirst ? 35.0 : 10.0;
                } else {
                    // If we don't have game/alliance info
                    countdown = 0.0; // default to no countdown since we can't determine which alliance is active
                                     // first
                }
            }

            hubStatusCountdown = countdown;
            wasHubActive = isHubActiveNow;
        }
    }


/**
 * Should be called periodically to update the hub status countdown timer.
 */
    public void updateCountdown() {
    
        if (hubStatusCountdown > 0) {
            hubStatusCountdown -= 0.020;
            if (hubStatusCountdown < 0) {
                hubStatusCountdown = 0;
            }
        }
    }

    /**
     * Returns a human-readable Label for the current match.
     * @param matchTime current match time in seconds can be obtained from a timer
     */
    public String getShift(double matchTime) {

        if (matchTime >= 140) {
            return "Match Over";
        }
        if (matchTime > 110) {
            return "Endgame";
        }
        if (matchTime > 85) {
            return "Shift 4";
        }
        if (matchTime > 60) {
            return "Shift 3";
        }
        if (matchTime > 35) {
            return "Shift 2";
        }
        if (matchTime > 10) {
            return "Shift 1";
        }
        if (matchTime > 0) {
            return "Transition";
        }
        return "Pre-match";
    }

}