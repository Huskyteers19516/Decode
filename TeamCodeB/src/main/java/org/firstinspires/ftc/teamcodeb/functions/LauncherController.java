package org.firstinspires.ftc.teamcodeb.functions;

import com.qualcomm.robotcore.hardware.DcMotorEx;

public class LauncherController {

    private DistanceTracker tracker;
    private DcMotorEx launcherMotor;
    private double lastTargetRPM = 0;

    public LauncherController(DistanceTracker tracker, DcMotorEx motor) {
        this.tracker = tracker;
        this.launcherMotor = motor;
    }

    public void updateLauncher(int targetTagId) {

        lastTargetRPM = tracker.autoAdjustLauncher(targetTagId);
    }

    public boolean isReadyToShoot(double toleranceRPM) {

        return lastTargetRPM > 0 &&
                Math.abs(launcherMotor.getVelocity() - lastTargetRPM) <= toleranceRPM;
    }

    public double getLastTargetRPM() {
        return lastTargetRPM;
    }
}