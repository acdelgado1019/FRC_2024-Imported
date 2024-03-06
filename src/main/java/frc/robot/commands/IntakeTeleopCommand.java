package frc.robot.commands;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.Constants.IntakeConstants;
import frc.robot.Robot;
import frc.robot.ControlConfigs.PlayerConfigs;

public class IntakeTeleopCommand extends Command{
    
    public IntakeTeleopCommand() {
        addRequirements(Robot.intake);
    }
    
    @Override
    public void execute () {
        if (PlayerConfigs.ground && Robot.intake.intakeState != 3) {
            Robot.intake.intakeState = 1;
        } else if (PlayerConfigs.amp && Robot.intake.intakeState != 3) {
            Robot.intake.intakeState = 2;
        } else if (!Robot.shooter.shootingMode && Robot.intake.intakeState == 0 && (PlayerConfigs.climberLDown || PlayerConfigs.climberLUp || PlayerConfigs.climberRUp || PlayerConfigs.climberRUp)) {
            //Only climber control can initiate trap position, no manual triggering allowed, must start from stow position
            //To activate trap, disable shooter and run climber down
            Robot.intake.intakeState = 3;
        } else if (PlayerConfigs.fcEnable) {
            //When fine control is enabled, grab current positions to hold
            Robot.intake.intakeState = 4;
            Robot.intake.fcControlElbow = false;
            Robot.intake.fcControlWrist = false;
            Robot.intake.elbowSetPoint = Robot.intake.getElbowEncoder();
            Robot.intake.wristSetPoint = Robot.intake.getWristEncoder();
        } else if (PlayerConfigs.stow) {
            Robot.intake.intakeState = 0;
        }

        //Ground State
        if (Robot.intake.intakeState == 1) {
            if (Robot.intake.getElbowEncoder() < IntakeConstants.kElbowGround + 5) {
                SmartDashboard.putString("Wrist Status", "Ground - Ground Position");
                Robot.intake.wristSetPoint = IntakeConstants.kWristGround;
            } else {
                SmartDashboard.putString("Wrist Status", "Ground - Stowed");
                Robot.intake.wristSetPoint = IntakeConstants.kWristStowed;
            }

            SmartDashboard.putString("Elbow Status", "Ground - Ground Position");
            Robot.intake.elbowSetPoint = IntakeConstants.kElbowGround;
        //Amp State
        } else if (Robot.intake.intakeState == 2) {
            if (Robot.intake.getElbowEncoder() > IntakeConstants.kElbowAmp + 5) {
                SmartDashboard.putString("Wrist Status", "Amp - Stowed");
                Robot.intake.wristSetPoint = IntakeConstants.kWristStowed;
            // } else if (Robot.intake.getElbowEncoder() < IntakeConstants.kElbowDownConstraint) { <-Add this in if we are breaking rules badly
            //     SmartDashboard.putString("Wrist Status", "Amp - Constrain");
            //     Robot.intake.wristSetPoint = IntakeConstants.kWristGround - Robot.intake.getElbowEncoder();
            } else {
                SmartDashboard.putString("Wrist Status", "Amp - Amp Position");
                Robot.intake.wristSetPoint = IntakeConstants.kWristAmp;
            }

            SmartDashboard.putString("Elbow Status", "Amp - Amp Position");
            Robot.intake.elbowSetPoint = IntakeConstants.kElbowAmp;
        //Trap State
        } else if (Robot.intake.intakeState == 3) {
            if (PlayerConfigs.armScoringMechanism) {
                SmartDashboard.putString("Elbow Status", "Trap - Scoring Position");
                Robot.intake.elbowSetPoint = IntakeConstants.kElbowTrapScoring;
            } else {
                SmartDashboard.putString("Elbow Status", "Trap - Pressure");
                Robot.intake.elbowSetPoint = IntakeConstants.kElbowTrapPressure;
            }
            
            if (PlayerConfigs.armScoringMechanism) {
                SmartDashboard.putString("Wrist Status", "Trap - Scoring Position");
                Robot.intake.wristSetPoint = IntakeConstants.kWristTrap;
            } else {
                SmartDashboard.putString("Wrist Status", "Trap - Stowed");
                Robot.intake.wristSetPoint = IntakeConstants.kWristStowed;
            }
        //Fine Control
        } else if (Robot.intake.intakeState == 4) {
            if (Math.abs(PlayerConfigs.fcElbow) > 0.25) {
                SmartDashboard.putString("Elbow Status", "Fine Control, Moving");
                Robot.intake.fcControlElbow = true;
                Robot.intake.elbowSetPoint = Robot.intake.getElbowEncoder() + IntakeConstants.kElbowManualOffset * PlayerConfigs.fcElbow; 
            } else if (Robot.intake.fcControlElbow) {
                SmartDashboard.putString("Elbow Status", "Fine Control, Holding");
                Robot.intake.fcControlElbow = false;
                Robot.intake.elbowSetPoint = Robot.intake.getElbowEncoder();
            }

            if (Math.abs(PlayerConfigs.fcWrist) > 0.25) {
                SmartDashboard.putString("Wrist Status", "Fine Control, Moving");
                Robot.intake.fcControlWrist = true;
                Robot.intake.wristSetPoint = Robot.intake.getWristEncoder() + IntakeConstants.kWristManualOffset * PlayerConfigs.fcWrist;
            } else if (Robot.intake.fcControlWrist) {
                SmartDashboard.putString("Wrist Status", "Fine Control, Holding");
                Robot.intake.fcControlWrist = false;
                Robot.intake.wristSetPoint = Robot.intake.getWristEncoder();
            }
            
        //Stow and Shoot
        } else {
            if (PlayerConfigs.armScoringMechanism) {
                SmartDashboard.putString("Wrist Status", "Stow - Shooting Position");
                Robot.intake.wristSetPoint = IntakeConstants.kWristShooting;
            } else {
                SmartDashboard.putString("Wrist Status", "Stow - Stow Position");
                Robot.intake.wristSetPoint = IntakeConstants.kWristStowed;
            }
            
            if (Robot.intake.getWristEncoder() > IntakeConstants.kWristShooting - 10) {
                SmartDashboard.putString("Elbow Status", "Stow - Stow Position");
                Robot.intake.elbowSetPoint = IntakeConstants.kElbowStowed;
            }
        }
        
        //Once setpoints have been chosen, pass to controllers
        Robot.intake.setElbowPosition(Robot.intake.elbowSetPoint);
        Robot.intake.setWristPosition(Robot.intake.wristSetPoint);

        //Reject Piece if button is pressed, regardless of intake state
        if (PlayerConfigs.reject) {
            SmartDashboard.putString("Intake Status", "Rejecting");
            Robot.intake.setIntakeVoltage(-12);
        //If intake is on the ground or firing, run at full speed
        } else if (Robot.intake.intakeState == 1 || PlayerConfigs.fire) {
            SmartDashboard.putString("Intake Status", "Intaking");
            Robot.intake.setIntakeVoltage(12);
        //Maintain idle spin for positive hold (think cones sliding out of the intake last year)
        } else {
            SmartDashboard.putString("Intake Status", "Holding");
            Robot.intake.setIntakeVoltage(1);
        }

        SmartDashboard.putNumber("Wrist Setpoint: ", Robot.intake.wristSetPoint);
        SmartDashboard.putNumber("Elbow Setpoint: ", Robot.intake.elbowSetPoint);
        SmartDashboard.putNumber("Wrist Position: ", Robot.intake.getWristEncoder());
        SmartDashboard.putNumber("Elbow Position: ", Robot.intake.getElbowEncoder());
        SmartDashboard.putNumber("Intake State: ",Robot.intake.intakeState);
        SmartDashboard.putBoolean("Piece Acquired: ", Robot.intake.getIntakeCurrent() > 20);
        SmartDashboard.putNumber("Intake Current: ", Robot.intake.getIntakeCurrent());

        Robot.intake.intakeLog();
    }
}
