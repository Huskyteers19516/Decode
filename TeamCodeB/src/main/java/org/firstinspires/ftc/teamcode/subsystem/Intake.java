package org.firstinspires.ftc.teamcodec.subsystem;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.seattlesolvers.solverslib.command.SubsystemBase;
import com.seattlesolvers.solverslib.hardware.motors.Motor;

public class Intake extends SubsystemBase {
    private final Motor intakeMotor;
    private boolean isRunning = false;

    public Intake(final HardwareMap hMap) {
        intakeMotor = new Motor(hMap, "intake", Motor.GoBILDA.BARE);
        intakeMotor.setRunMode(Motor.RunMode.RawPower);
        System.out.println("IntakeSubsystem initialized");
    }

    public void toggle() {
        isRunning = !isRunning;
    }

    public void setPower(double power) {
        intakeMotor.set(power);
    }
    public void start() {
        System.out.println("Intake started");
        intakeMotor.set(1.0);
    }

    public void stop() {
        intakeMotor.set(0.0);
    }

    public boolean active() {
        return isRunning;
    }
}
