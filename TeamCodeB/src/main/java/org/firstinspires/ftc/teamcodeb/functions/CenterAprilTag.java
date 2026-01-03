package org.firstinspires.ftc.teamcodeb.functions;

import com.pedropathing.follower.Follower;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import java.util.List;

public class CenterAprilTag {

    private final AprilTagProcessor aprilTagProcessor;
    private final Follower follower;
    private boolean isActive = false;

    public CenterAprilTag(Follower follower, AprilTagProcessor processor) {
        this.follower = follower;
        this.aprilTagProcessor = processor;
    }


    public boolean update(int tagId, double kP, double threshold) {
        if (!isActive) return false;

        List<AprilTagDetection> detections = aprilTagProcessor.getDetections();
        AprilTagDetection targetTag = null;

        if (detections != null) {
            for (AprilTagDetection detection : detections) {
                if (detection.id == tagId) {
                    targetTag = detection;
                    break;
                }
            }
        }


        if (targetTag == null) {
            stop();
            return false;
        }

        double bearing = targetTag.ftcPose.bearing;


        if (Math.abs(bearing) < threshold) {
            follower.setTeleOpDrive(0, 0, 0, true);
            isActive = false;
            return true;
        }


        double power = -bearing * kP;


        double minPower = 0.08;
        if (Math.abs(power) < minPower) {
            power = Math.signum(power) * minPower;
        }


        power = Math.max(-0.25, Math.min(0.25, power));


        follower.setTeleOpDrive(0, 0, power, true);
        return false;
    }

    public void start() {
        this.isActive = true;
    }

    public void stop() {
        this.isActive = false;
        follower.setTeleOpDrive(0, 0, 0, true);
    }

    public boolean isActive() {
        return isActive;
    }

    public double getBearing(int tagId) {
        List<AprilTagDetection> detections = aprilTagProcessor.getDetections();
        if (detections == null) return 0;
        for (AprilTagDetection detection : detections) {
            if (detection.id == tagId) return detection.ftcPose.bearing;
        }
        return 0;
    }
}