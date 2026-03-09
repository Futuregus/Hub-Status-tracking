// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.Timer;


/**
 * The methods in this class are called automatically corresponding to each mode, as described in
 * the TimedRobot documentation. If you change the name of this class or the package after creating
 * this project, you must also update the Main.java file in the project.
 */
public class Robot extends TimedRobot {



  // Drive setup
  private final WPI_TalonSRX leftMaster = new WPI_TalonSRX(1);
  private final WPI_TalonSRX leftFollower = new WPI_TalonSRX(2);
  private final WPI_TalonSRX rightMaster = new WPI_TalonSRX(3);
  private final WPI_TalonSRX rightFollower = new WPI_TalonSRX(4);
  private final DifferentialDrive mooseDrive = new DifferentialDrive(leftMaster, rightMaster);

  // Controller setup
  private final XboxController driverController = new XboxController(0);

  // Tunable minimum speed for drive (0.0 to 1.0) (1.0 makes the robot go vroooooooooom)
  private static final double DefaultMinSpeed = 0.5; // min speed is the speed that the robot can go if the right trigger is not held down
  private double driveMinSpeed = DefaultMinSpeed; // set the initial minimum speed to the default value

  // Set Up hub status tracking
  hubStatus hubstatus = new hubStatus();
  private final Timer estimatedMatchTime = new Timer(); // timer to estimate match time for hub status
  
  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  public Robot() {

    SmartDashboard.putNumber("Minimum Speed", driveMinSpeed);

    // set up followers
    leftFollower.follow(leftMaster);
    rightFollower.follow(rightMaster);

    // invert right side
    rightMaster.setInverted(true);
    rightFollower.setInverted(true);
  }

  /**
   * This function is called every 20 ms, no matter the mode. Use this for items like diagnostics
   * that you want ran during disabled, autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before LiveWindow and
   * SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {}

  @Override
  public void autonomousInit() {}

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {}

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
    hubstatus.updateCountdown();
    SmartDashboard.putBoolean("Is Hub Active?", isHubActive);
    SmartDashboard.putNumber("Hub Countdown", hubstatus.hubStatusCountdown);
    SmartDashboard.putNumber("Estimated Match Time", estimatedMatchTime.get());
    SmartDashboard.putString("Hub Shift", hubstatus.getShift(estimatedMatchTime.get()));
    if (estimatedMatchTime.get() >= 140) {
      estimatedMatchTime.stop(); // stop the timer at the end of the match.
    }

  // read trigger as max speed (0.0 to 1.0) joystick is a percentage of max speed
  if (driverController.getLeftBumperButtonPressed()) {
    driveMinSpeed = Math.max(0.0, driveMinSpeed - 0.1); // decrease minimum speed
    SmartDashboard.putNumber("Minimum Speed", driveMinSpeed);
  }
  if (driverController.getRightBumperButtonPressed()) {
    driveMinSpeed = Math.min(1.0, driveMinSpeed + 0.1); // increase minimum speed
    SmartDashboard.putNumber("Minimum Speed", driveMinSpeed);
  }
  double triggerSpeed = Math.max(driverController.getRightTriggerAxis(), driveMinSpeed); // use right trigger for speed control. (push the right trigger all the way down to make the robot go vroooooom)

  // read joystick inputs
  double forwardInput = -driverController.getLeftY(); // forward/backward on left stick
  double rotationInput = driverController.getRightX(); // rotation on right stick
  
  // apply minimum speed and scaling
  double forward = forwardInput * triggerSpeed; // scale forward by trigger speed
  double rotation = rotationInput * triggerSpeed; // scale rotation by trigger speed
  SmartDashboard.putNumber("Forward Speed", forward);
  SmartDashboard.putNumber("Rotation Speed", rotation);

    // deadband to prevent stick drift
    if (Math.abs(forward) < 0.02) forward = 0; // left stick Y-axis deadband
    if (Math.abs(rotation) < 0.02) rotation = 0; // right stick X-axis deadband

    // arcade drive
    mooseDrive.arcadeDrive(forward, -rotation); // may need to swap forward and rotation depending on how the motors are wired

  }

  /** This function is called once when the robot is disabled. */
  @Override
  public void disabledInit() {}

  /** This function is called periodically when disabled. */
  @Override
  public void disabledPeriodic() {}

  /** This function is called once when test mode is enabled. */
  @Override
  public void testInit() {}

  /** This function is called periodically during test mode. */
  @Override
  public void testPeriodic() {}
}