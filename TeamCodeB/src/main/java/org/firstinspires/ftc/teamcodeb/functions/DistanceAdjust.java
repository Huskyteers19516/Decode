package org.firstinspires.ftc.teamcodeb.functions;

import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.hardware.Gamepad;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.teamcodeb.opmode.RobotBTeleOp;
import org.firstinspires.ftc.vision.apriltag.*;

import java.util.List;

public class DistanceAdjust extends RobotBTeleOp{

    private AprilTagProcessor aprilTagProcessor;
    private RobotBTeleOp robot;

    private boolean autoDrive29 = false;

    public DistanceAdjust(RobotBTeleOp robot, AprilTagProcessor processor) {
        this.robot = robot;
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

        // override detection
        boolean driverOverride =
                Math.abs(robot.gamepad1.left_stick_x) > 0.05 ||
                        Math.abs(robot.gamepad1.left_stick_y) > 0.05 ||
                        Math.abs(robot.gamepad1.right_stick_x) > 0.05;

        if (driverOverride) autoDrive29 = false;

        if (!autoDrive29) return;

        // Get detections
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
            robot.follower.setTeleOpDrive(0, 0, 0, true);
            autoDrive29 = false;
        } else {
            robot.follower.setTeleOpDrive(
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
}