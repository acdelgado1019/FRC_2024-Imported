package frc.robot.commands;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.Robot;
import frc.robot.ControlConfigs.PlayerConfigs;

public class ClimberTeleopCommand extends Command{
    
    public ClimberTeleopCommand(){
        addRequirements(Robot.climbers);
    }
    
    @Override
    public void execute (){
    if (PlayerConfigs.climberdown) {
            Robot.climbers.setclimbers(8);
        } else if (PlayerConfigs.climberup) {
            Robot.climbers.setclimbers(-8);
        } else {
            Robot.climbers.setclimbers(0);
        }
        Robot.climbers.errorCheck();

    SmartDashboard.putNumber("climberposition", Robot.climbers.getClimberEncoder());
    } 
}
