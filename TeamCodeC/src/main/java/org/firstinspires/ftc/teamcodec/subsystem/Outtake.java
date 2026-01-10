package org.firstinspires.ftc.teamcodec.subsystem;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.seattlesolvers.solverslib.command.SubsystemBase;
import com.seattlesolvers.solverslib.hardware.motors.Motor;
import com.seattlesolvers.solverslib.hardware.motors.MotorEx;

public class Outtake extends SubsystemBase {
    private final MotorEx outtakeMotor;
    private boolean isRunning = false;

    public Outtake(final HardwareMap hMap) {
        outtakeMotor = new MotorEx(hMap, "outtake", Motor.GoBILDA.BARE);
        outtakeMotor.setRunMode(Motor.RunMode.VelocityControl);
        System.out.println("OuttakeSubsystem initialized");
    }

    public boolean canShoot() {
        return Math.abs(outtakeMotor.getVelocity() - 1500) < 50;
    }

    public double getVelocity() {
        return outtakeMotor.getVelocity();
    }

    public void start() {
        outtakeMotor.setVelocity(1500);
    }

    public void stop() {
        outtakeMotor.set(0.0);
    }
}
