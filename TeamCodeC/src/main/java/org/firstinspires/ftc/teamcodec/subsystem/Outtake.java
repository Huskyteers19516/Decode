package org.firstinspires.ftc.teamcodec.subsystem;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.seattlesolvers.solverslib.command.SubsystemBase;
import com.seattlesolvers.solverslib.hardware.motors.Motor;
import com.seattlesolvers.solverslib.hardware.motors.MotorEx;

import org.firstinspires.ftc.teamcodec.config.OuttakeConstants;

public class Outtake extends SubsystemBase {

    private final DcMotorEx outtakeMotor;

    public Outtake(final HardwareMap hMap) {
        outtakeMotor = hMap.get(DcMotorEx.class, "outtake");
        outtakeMotor.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
        outtakeMotor.setVelocityPIDFCoefficients(OuttakeConstants.kp, OuttakeConstants.ki, OuttakeConstants.ki, OuttakeConstants.ks);
        outtakeMotor.setPower(0.0);
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
        System.out.println("set to " + velocity);
        targetVelocity = velocity;
        outtakeMotor.setVelocity(velocity);
    }

    public double getRawPower() {
        return outtakeMotor.getPower();
    }

    public void start() {
        active = true;
        System.out.println("start");
        setVelocity(targetVelocity);
    }

    public void toggle() {
        active = !active;
        if (active) {
            start();
        } else {
            stop();
        }
    }

    public boolean getActive() {
        return active;
    }
    public boolean canShoot() {
        return active && (Math.abs(outtakeMotor.getVelocity() - targetVelocity) < OuttakeConstants.allowance);
    }
    public void stop() {
        active = false;
        outtakeMotor.setPower(0.0);
    }
}
