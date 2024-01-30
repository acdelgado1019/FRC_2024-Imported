package frc.robot.commands.Autonomous.Autonomous_Actions;

import frc.robot.Constants.IntakeConstants;
import frc.robot.commands.Autonomous.Subsystem_Commands.AutoIntakeElbowSet;
import frc.robot.commands.Autonomous.Subsystem_Commands.AutoIntakeWristSet;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

public class AutoIntakeGroundToAmp extends SequentialCommandGroup{

    /** Creates a new Drivetrain Snap-to-angle Command. */
    public AutoIntakeGroundToAmp() {
        //sets intake to stow, lifts elbow to stow
        addCommands(
            new ParallelCommandGroup(
                new AutoIntakeWristSet(IntakeConstants.kWristAmp,5),
                new AutoIntakeElbowSet(IntakeConstants.kElbowAmp, 10)
            )
        );
    }
}