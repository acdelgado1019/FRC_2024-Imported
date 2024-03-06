package frc.robot.commands.Autonomous.Autonomous_Actions;

import frc.robot.Constants.IntakeConstants;
import frc.robot.commands.Autonomous.Subsystem_Commands.AutoIntakeElbowSet;
import frc.robot.commands.Autonomous.Subsystem_Commands.AutoIntakeWristSet;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

public class AutoIntakeStowToGround extends SequentialCommandGroup{

    public AutoIntakeStowToGround() {
        //Sets intake at stow, moves elbow to ground, drops intake to ground
        addCommands(
            new AutoIntakeElbowSet(IntakeConstants.kElbowGround, 10),
            new AutoIntakeWristSet(IntakeConstants.kWristGround,5)
        );
    }
}  