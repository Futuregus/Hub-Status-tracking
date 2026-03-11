// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.xrp.XRPMotor;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.XboxController;

/**
 * The methods in this class are called automatically corresponding to each
 * mode, as described in
 * the TimedRobot documentation. If you change the name of this class or the
 * package after creating
 * this project, you must also update the Main.java file in the project.
 */
public class Robot extends TimedRobot {

  // Auto mode chooser setup
  private static final String DefaultAuto = "Default";
  private static final String driveForward = "Drive Forward";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();
  private final Timer aTimer = new Timer(); // timer for autonomous

  // Drive setup
  private final XRPMotor leftDrive = new XRPMotor(0);
  private final XRPMotor rightDrive = new XRPMotor(1);
  private final DifferentialDrive Drive = new DifferentialDrive(leftDrive, rightDrive);

  // Controller setup
  private final XboxController driverController = new XboxController(0);

  // Tunable minimum speed for drive (0.0 to 1.0)
  private static final double DefaultMinSpeed = 0.5; // min speed is the speed that the robot can go if the right
                                                     // trigger is not held down

  // set the initial minimum speed to the default value
  private double driveMinSpeed = DefaultMinSpeed;

  hubStatus hubstatus = new hubStatus();
  private final Timer estimatedMatchTime = new Timer(); // timer to estimate match time for hub status

  /**
   * This function is run when the robot is first started up and should be used
   * for any
   * initialization code.
   */
  public Robot() {
    m_chooser.setDefaultOption("Default Auto", DefaultAuto);
    m_chooser.addOption("Drive Forward", driveForward);
    SmartDashboard.putData("Auto choices", m_chooser);
    SmartDashboard.putNumber("Minimum Speed", driveMinSpeed);
  }

  /**
   * This function is called every 20 ms, no matter the mode. Use this for items
   * like diagnostics
   * that you want ran during disabled, autonomous, teleoperated and test.
   *
   * <p>
   * This runs after the mode specific periodic functions, but before LiveWindow
   * and
   * SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
  }

  /**
   * This autonomous (along with the chooser code above) shows how to select
   * between different
   * autonomous modes using the dashboard. The sendable chooser code works with
   * the Java
   * SmartDashboard. If you prefer the LabVIEW Dashboard, remove all of the
   * chooser code and
   * uncomment the getString line to get the auto name from the text box below the
   * Gyro
   *
   * <p>
   * You can add additional auto modes by adding additional comparisons to the
   * switch structure
   * below with additional strings. If using the SendableChooser make sure to add
   * them to the
   * chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    m_autoSelected = m_chooser.getSelected();
    // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);

    aTimer.start();
    aTimer.reset();

  }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {
    switch (m_autoSelected) {
      case driveForward:
        // Put custom auto code here
        if (aTimer.get() < 2) {
          Drive.tankDrive(0.6, -0.6);
        } else if (aTimer.get() < 5) {
          Drive.tankDrive(1.0, -1.0);
        } else {

          Drive.tankDrive(0, 0);
        }

        break;

      case DefaultAuto:
      default:
        // leftDrive.set(0.6);
        // rightDrive.set(0.6);
        if (aTimer.get() < 2) {
          Drive.tankDrive(0.6, -0.6);
        } else if (aTimer.get() < 5) {
          Drive.tankDrive(1.0, 1.0);
        } else if (aTimer.get() < 6) {
          Drive.tankDrive(1.0, -1.0);
        } else {

          Drive.tankDrive(0, 0);
        }

        break;
    }
  }

  /** This function is called once when teleop is enabled. */
  @Override
  public void teleopInit() {
    estimatedMatchTime.reset();
    estimatedMatchTime.start();
    SmartDashboard.putNumber("Estimated Match Time", estimatedMatchTime.get());
  }

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {

    // hub status tracking
    boolean isHubActive = hubstatus.isHubActive(estimatedMatchTime.get());
    hubstatus.startCountdown(isHubActive, estimatedMatchTime.get());
    SmartDashboard.putBoolean("Is Hub Active?", isHubActive);
    SmartDashboard.putNumber("Hub Countdown", hubstatus.hubStatusCountdown);
    SmartDashboard.putNumber("Estimated Match Time", estimatedMatchTime.get());
    SmartDashboard.putString("Hub Shift", hubstatus.getShift(estimatedMatchTime.get()));
    
    if (estimatedMatchTime.get() >= 140) {
      estimatedMatchTime.stop(); // stop the timer at the end of the match.
    }

    // allow bumpers to tune the min speed in steps of 0.1
    if (driverController.getLeftBumperButtonPressed()) {
      driveMinSpeed = Math.max(0.0, driveMinSpeed - 0.1);
      SmartDashboard.putNumber("Minimum Speed", driveMinSpeed);
    }
    if (driverController.getRightBumperButtonPressed()) {
      driveMinSpeed = Math.min(1.0, driveMinSpeed + 0.1);
      SmartDashboard.putNumber("Minimum Speed", driveMinSpeed);
    }

    // control speed via right trigger
    double triggerSpeed = Math.max(driverController.getRightTriggerAxis(), driveMinSpeed); //
    double forward = -driverController.getLeftY() * triggerSpeed; // scale forward by trigger speed
    double rotation = driverController.getRightX() * triggerSpeed; // scale rotation by trigger speed
    SmartDashboard.putNumber("Forward Speed", forward);
    SmartDashboard.putNumber("Rotation Speed", rotation);

    // deadband to prevent stick drift
    if (Math.abs(forward) < 0.02)
      forward = 0;
    if (Math.abs(rotation) < 0.02)
      rotation = 0;

    // arcade drive with forward and rotation
    Drive.arcadeDrive(rotation, -forward); // may need to swap forward and rotation depending on how the motors are
                                           // wired

  }

  /** This function is called once when the robot is disabled. */
  @Override
  public void disabledInit() {
  }

  /** This function is called periodically when disabled. */
  @Override
  public void disabledPeriodic() {
  }

  /** This function is called once when test mode is enabled. */
  @Override
  public void testInit() {
  }

  /** This function is called periodically during test mode. */
  @Override
  public void testPeriodic() {
  }
}
