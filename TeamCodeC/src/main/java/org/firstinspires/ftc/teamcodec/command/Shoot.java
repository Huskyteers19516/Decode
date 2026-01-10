package org.firstinspires.ftc.teamcodec.command;

import com.seattlesolvers.solverslib.command.InstantCommand;
import com.seattlesolvers.solverslib.command.SequentialCommandGroup;
import com.seattlesolvers.solverslib.command.WaitCommand;

import org.firstinspires.ftc.teamcodec.subsystem.Feeders;
import org.firstinspires.ftc.teamcodec.subsystem.Outtake;

public class Shoot extends SequentialCommandGroup {
    public Shoot(Feeders feeders, Outtake outtake, Feeders.Feeder feeder) {
        if (feeder == Feeders.Feeder.A) {
            addCommands(
                    new OuttakeSpinUp(outtake),
                    new InstantCommand(() -> feeders.raiseFeeder(feeder)),
                    new WaitCommand(500),
                    new InstantCommand(() -> feeders.lowerFeeder(feeder)),
                    new WaitCommand(500)
            );
        } else if (feeder == Feeders.Feeder.B) {
                    addCommands(
                            new OuttakeSpinUp(outtake),
                            new InstantCommand(() -> feeders.raiseFeeder(feeder)),
                            new WaitCommand(500),
                            new InstantCommand(() -> feeders.lowerFeeder(feeder)),
                            new InstantCommand(feeders::raiseFeederA),
                            new WaitCommand(500),
                            new InstantCommand(feeders::lowerFeederA),
                            new WaitCommand(500)
                    );
        }

        addRequirements(outtake, feeders);
    }
}
