package org.firstinspires.ftc.teamcodec.subsystem;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.seattlesolvers.solverslib.command.SubsystemBase;
import com.seattlesolvers.solverslib.hardware.motors.Motor;
import com.seattlesolvers.solverslib.hardware.motors.MotorEx;

import org.firstinspires.ftc.teamcodec.config.OuttakeConstants;

public class Outtake extends SubsystemBase {

    private final MotorEx outtakeMotor;

    public Outtake(final HardwareMap hMap) {
        outtakeMotor = new MotorEx(hMap, "outtake", Motor.GoBILDA.BARE);
        outtakeMotor.setRunMode(Motor.RunMode.VelocityControl);
        outtakeMotor.setVeloCoefficients(OuttakeConstants.kp, OuttakeConstants.ki, OuttakeConstants.ki);
        outtakeMotor.setFeedforwardCoefficients(OuttakeConstants.ks, OuttakeConstants.kv);
        System.out.println("OuttakeSubsystem initialized");
    }

    public double getVelocity() {
        return outtakeMotor.getVelocity();
    }

    private double targetVelocity = OuttakeConstants.defaultTargetVelocity;
    private boolean active = false;

    public double getTargetVelocity() {
        return targetVelocity;
    }
    public void setVelocity(double velocity) {
        targetVelocity = velocity;
    }

    public double getRawPower() {
        return outtakeMotor.motorEx.getPower();
    }

    public void start() {
        active = true;
    }

    public void toggle() {
        active = !active;
    }

    public boolean getActive() {
        return active;
    }
    public boolean canShoot() {
        return active && (Math.abs(outtakeMotor.getVelocity() - getSetPoint()) < OuttakeConstants.allowance);
    }

    public double getSetPoint() {
        return targetVelocity * 0.9 * outtakeMotor.ACHIEVABLE_MAX_TICKS_PER_SECOND;
    }

    @Override
    public void periodic() {
        if (active) {
            System.out.println("periodic");

            outtakeMotor.set(targetVelocity);
            outtakeMotor.setVeloCoefficients(OuttakeConstants.kp, OuttakeConstants.ki, OuttakeConstants.kd);
            outtakeMotor.setFeedforwardCoefficients(OuttakeConstants.ks, OuttakeConstants.kv);
        } else {
            stop();
        }

    }
    public void stop() {
        active = false;
        outtakeMotor.stopMotor();
    }
}
