package org.firstinspires.ftc.teamcodeb.functions;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.hardware.Gamepad;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.teamcodeb.opmode.RobotBTeleOp;
import org.firstinspires.ftc.vision.apriltag.*;

import java.util.List;

public class DistanceAdjust {

    private AprilTagProcessor aprilTagProcessor;
    private Follower follower;
    private Gamepad gamepad;

    private boolean autoDrive29 = false;

    public DistanceAdjust(Follower follower, Gamepad gamepad, AprilTagProcessor processor) {
        this.follower = follower;
        this.gamepad = gamepad;
        this.aprilTagProcessor = processor;
    }

    public Pose getCameraPose() {
        List<AprilTagDetection> detections = aprilTagProcessor.getDetections();
        if (detections == null || detections.isEmpty()) return null;

        AprilTagDetection d = detections.get(0);

        return new Pose(
                d.robotPose.getPosition().x,
                d.robotPose.getPosition().y,
                d.robotPose.getOrientation().getYaw(AngleUnit.RADIANS)
        );
    }

    public void update() {
        if (!autoDrive29) return;

        boolean driverOverride =
                Math.abs(gamepad.left_stick_x) > 0.05 ||
                        Math.abs(gamepad.left_stick_y) > 0.05 ||
                        Math.abs(gamepad.right_stick_x) > 0.05;

        if (driverOverride) autoDrive29 = false;

        if (!autoDrive29) return;

        List<AprilTagDetection> detections = aprilTagProcessor.getDetections();
        if (detections == null || detections.isEmpty()) {
            autoDrive29 = false;
            return;
        }

        AprilTagDetection tag = null;
        for (AprilTagDetection d : detections) {
            if (d.id == 20 || d.id == 24) {
                tag = d;
                break;
            }
        }

        if (tag == null) {
            autoDrive29 = false;
            return;
        }

        double error = tag.ftcPose.range - 29;
        double power = 0.3;

        if (Math.abs(error) < 2.0) {
            follower.setTeleOpDrive(0, 0, 0, true);
            autoDrive29 = false;
        } else {
            follower.setTeleOpDrive(
                    error > 0 ? power : -power,
                    0,
                    0,
                    true
            );
        }
    }

    public void startAutoDrive29() {
        autoDrive29 = true;
    }

    public boolean isAutoDriveActive() {
        return autoDrive29;
    }
}