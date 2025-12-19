package org.firstinspires.ftc.teamcodeb.functions;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Gamepad;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcodeb.OpModeConstants;

public class FlyWheelVelocity {

    private DcMotorEx launcher;
    private Gamepad gamepad;
    private Telemetry telemetry;

    private double currentVelocity;
    private double targetVelocity = OpModeConstants.LAUNCHER_TARGET_VELOCITY;
    private boolean lastDpadUpState = false;
    private boolean lastDpadDownState = false;
    private boolean lastBState = false;

    public FlyWheelVelocity() {
    }

    public FlyWheelVelocity(DcMotorEx launcher, Gamepad gamepad, Telemetry telemetry) {
        this.launcher = launcher;
        this.gamepad = gamepad;
        this.telemetry = telemetry;
    }

    public void init(DcMotorEx launcher, Gamepad gamepad, Telemetry telemetry) {
        this.launcher = launcher;
        this.gamepad = gamepad;
        this.telemetry = telemetry;
    }

    public void update() {
        if (launcher == null || gamepad == null) return;

        currentVelocity = launcher.getVelocity();

        boolean currentDpadUpState = gamepad.dpad_up;
        boolean currentDpadDownState = gamepad.dpad_down;
        boolean currentBState = gamepad.b;

        if (currentDpadUpState && !lastDpadUpState) {
            targetVelocity += 100;
            launcher.setVelocity(targetVelocity);
        }

        if (currentDpadDownState && !lastDpadDownState) {
            targetVelocity -= 100;
            launcher.setVelocity(targetVelocity);
        }

        targetVelocity = Math.max(0, Math.min(targetVelocity, 4000));

        if (currentBState && !lastBState) {
            targetVelocity = OpModeConstants.LAUNCHER_TARGET_VELOCITY;
            launcher.setVelocity(targetVelocity);
        }

        lastDpadUpState = currentDpadUpState;
        lastDpadDownState = currentDpadDownState;
        lastBState = currentBState;

        addTelemetry();
    }

    private void addTelemetry() {
        if (telemetry != null) {
            telemetry.addLine("=== FlyWheel Tuner ===");
            telemetry.addData("Target Velocity", "%.0f ticks/s", targetVelocity);
            telemetry.addData("Current Velocity", "%.0f ticks/s", currentVelocity);
            telemetry.addData("Velocity Error", "%.0f ticks/s", Math.abs(targetVelocity - currentVelocity));
        }
    }

    public double getTargetVelocity() {
        return targetVelocity;
    }

    public void resetToDefault() {
        targetVelocity = OpModeConstants.LAUNCHER_TARGET_VELOCITY;
        if (launcher != null) {
            launcher.setVelocity(targetVelocity);
        }
    }

    public void setTargetVelocity(double velocity) {
        this.targetVelocity = velocity;
        targetVelocity = Math.max(0, Math.min(targetVelocity, 4000));
        if (launcher != null) {
            launcher.setVelocity(targetVelocity);
        }
    }
}