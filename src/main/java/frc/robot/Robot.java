// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import java.util.Optional;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.Mechanism2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.ControlConfigs.PlayerConfigs;
import frc.robot.ControlConfigs.Drivers.Controller;
import frc.robot.commands.DrivetrainTeleopCommand;
import frc.robot.commands.LEDTeleopCommand;
import frc.robot.commands.Autonomous.AutoRoutines;
import frc.robot.commands.Autonomous.Autonomous_Actions.AutoIntakeGroundToStow;
import frc.robot.commands.Autonomous.Autonomous_Actions.AutoIntakeStowToGround;
import frc.robot.subsystems.Drivetrain;
import frc.robot.subsystems.Intake;
import frc.robot.subsystems.Shooter;
import frc.robot.subsystems.Climbers;
import frc.robot.subsystems.Limelight;
import frc.robot.subsystems.LEDs;
import frc.robot.util.Logger;

/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {  
  //Modes and people
  public PlayerConfigs driver;
  public PlayerConfigs coDriver;
  public static boolean skipNonPath;
  private AutoRoutines autoMode;

  public static PlayerConfigs controller = new Controller();

  public static final Mechanism2d m_Mech2d = new Mechanism2d(60, 40);
  
  //Subsystem Declarations
  public static final Drivetrain drivetrain = new Drivetrain();
  public static final Intake intake = new Intake();
  public static final Shooter shooter = new Shooter();
  public static final Climbers climbers = new Climbers();  
  public static final Limelight limelight = new Limelight();
  public static final LEDs ledSystem = new LEDs();

  //Controllers
  public static final XboxController controller0 = new XboxController(Constants.IOConstants.DRIVER_CONTROLLER_0);
  public static final XboxController controller1 = new XboxController(Constants.IOConstants.DRIVER_CONTROLLER_1);

  //Test Timer & Field Info
  Timer timer = new Timer();
  public static Optional<Alliance> teamColor;
  public final static Field2d m_field = new Field2d();

  /*
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  @Override
  public void robotInit() {
    Logger.info("SYSTEM","Robot Started");
    SmartDashboard.putData("Motion Sim", m_Mech2d);
    SmartDashboard.putBoolean("Skip Non-Path Commands", false);
    SmartDashboard.putData(m_field);
    autoMode = new AutoRoutines();
  }

  /**
   * This function is called every robot packet, no matter the mode. Use this for items like
   * diagnostics that you want ran during disabled, autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before LiveWindow and
   * SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
    SmartDashboard.putNumber("Match Time",Timer.getMatchTime());
    SmartDashboard.putNumber("Wrist Setpoint: ", Robot.intake.wristSetPoint);
    SmartDashboard.putNumber("Elbow Setpoint: ", Robot.intake.elbowSetPoint);
    SmartDashboard.putNumber("Wrist Position: ", Robot.intake.m_wristEncoder.getDistance()/(2*Math.PI)*360 - Robot.intake.m_elbowEncoder.getDistance()/(2*Math.PI)*360 + 62);
    SmartDashboard.putNumber("Elbow Position: ", Robot.intake.m_elbowEncoder.getDistance()/(2*Math.PI)*360 + 28);
    SmartDashboard.putNumber("Intake State: ",Robot.intake.intakeState);
    SmartDashboard.putBoolean("Piece Acquired: ", Robot.intake.pieceAcquired);
    SmartDashboard.putNumber("Intake Speed", Robot.intake.m_intakeEncoder.getRate());
    SmartDashboard.putBoolean("Intake Running", Robot.intake.running);
    SmartDashboard.putNumber("Shooter Speed", Robot.shooter.m_flywheelEncoder.getRate());
    SmartDashboard.putBoolean("Shooter Mode", Robot.shooter.shootingMode);

    SmartDashboard.putData(CommandScheduler.getInstance());
  }

  @Override
  public void autonomousInit() {
    Logger.info("SYSTEM","Autonomous Program Started");
    CommandScheduler.getInstance().cancelAll();
    teamColor = DriverStation.getAlliance();
    autoMode.getAutonomousCommand().schedule();
    skipNonPath = SmartDashboard.getBoolean("Skip Non-Path Commands", false);
  }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {
    CommandScheduler.getInstance().run();
    ledSystem.rainbow();
    Robot.intake.reachElbowSetpoint(Robot.intake.elbowSetPoint);
    Robot.intake.reachWristSetpoint(Robot.intake.wristSetPoint + Robot.intake.elbowSetPoint);
    Robot.limelight.reachSetpoint(Robot.limelight.limelightSetpoint);
    Robot.shooter.reachSetpoint(Robot.shooter.setpoint);
  }

  /** This function is called once when teleop is enabled. */
  @Override
  public void teleopInit() {
    Logger.info("SYSTEM","Teleop Started");
    CommandScheduler.getInstance().cancelAll();
    Robot.drivetrain.setDefaultCommand(new DrivetrainTeleopCommand());
    Robot.ledSystem.setDefaultCommand(new LEDTeleopCommand());
  }

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {
    CommandScheduler.getInstance().run();
    controller.getDriverConfig();
    controller.getCoDriverConfig();

    Robot.intake.teleopCommand();
    Robot.climbers.teleopCommand();
    Robot.shooter.teleopCommand();
    Robot.limelight.teleopCommand();
  }

  /** This function is called once when the robot is disabled. */
  @Override
  public void disabledInit() {
    Logger.info("SYSTEM", "Robot Disabled");
    CommandScheduler.getInstance().cancelAll();
  }

  /** This function is called periodically when disabled. */
  @Override
  public void disabledPeriodic() {
    teamColor = DriverStation.getAlliance();
    ledSystem.rainbow();
  }

  /** This function is called once when test mode is enabled. */
  @Override
  public void testInit() {
    new AutoIntakeStowToGround().schedule();
    new AutoIntakeGroundToStow().schedule();
  }

  /** This function is called periodically during test mode. */
  @Override
  public void testPeriodic() {
  }

  /** This function is called once when the robot is first started up. */
  @Override
  public void simulationInit() {}

  /** This function is called periodically whilst in simulation. */
  @Override
  public void simulationPeriodic() {
    intake.simulationPeriodic();
    climbers.simulationPeriodic();
    shooter.simulationPeriodic();
    limelight.simulationPeriodic();
  }
}
