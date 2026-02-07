package org.firstinspires.ftc.teamcodec.command;

import com.seattlesolvers.solverslib.command.CommandBase;

import org.firstinspires.ftc.teamcodec.config.OuttakeConstants;
import org.firstinspires.ftc.teamcodec.subsystem.Outtake;


public class OuttakeSpinUp extends CommandBase {
    Outtake outtake;

    public OuttakeSpinUp(Outtake outtake) {
        this.outtake = outtake;
        addRequirements(outtake);
    }
    @Override
    public void initialize() {
        outtake.start();
    }

    @Override
    public boolean isFinished() {
        return outtake.canShoot();
    }
}
