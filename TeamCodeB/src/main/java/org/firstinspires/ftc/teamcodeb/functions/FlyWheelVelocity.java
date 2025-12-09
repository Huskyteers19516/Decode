package org.firstinspires.ftc.teamcodeb.functions;

import org.firstinspires.ftc.teamcodeb.opmode.RobotBTeleOp;
import org.firstinspires.ftc.teamcodeb.OpModeConstants;

public class FlyWheelVelocity extends RobotBTeleOp {

    private double currentVelocity;
    private double targetVelocity = OpModeConstants.LAUNCHER_TARGET_VELOCITY;

    @Override
    public void init() {
        telemetry.update();
    }

    @Override
    public void loop() {


        currentVelocity = launcher.getVelocity();


        if (gamepad1.dpad_up) {
            targetVelocity += 200;
        }
        if (gamepad1.dpad_down) {
            targetVelocity -= 200;
        }


        targetVelocity = Math.max(0, Math.min(targetVelocity, 4000));


        if (gamepad1.b) {
            targetVelocity = OpModeConstants.LAUNCHER_TARGET_VELOCITY;
        }


        launcher.setVelocity(targetVelocity);


        telemetry.addLine("=== FlyWheel Tuner ===");
        telemetry.addData("Target Velocity", "%.0f ticks/s", targetVelocity);
        telemetry.addData("Current Velocity", "%.0f ticks/s", currentVelocity);
        telemetry.addData("Velocity Error", "%.0f ticks/s", Math.abs(targetVelocity - currentVelocity));
        telemetry.update();
    }
}