package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.Robot;

public class LEDTeleopCommand extends Command{
    
    public LEDTeleopCommand(){
        addRequirements(Robot.ledSystem);
    }
    
    @Override
    public void execute (){
        
    } 
}
