package org.firstinspires.ftc.teamcodeb.functions;



import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import com.pedropathing.follower.Follower;
import java.util.List;

public class CenterAprilTag {

    private AprilTagProcessor aprilTagProcessor;
    private Follower follower;

    public CenterAprilTag (Follower follower, AprilTagProcessor processor) {
        this.follower = follower;
        this.aprilTagProcessor = processor;
    }


    public boolean centerTag(int tagId, double kP, double threshold) {
        List<AprilTagDetection> detections = aprilTagProcessor.getDetections();
        if (detections == null) return false;


        for (AprilTagDetection detection : detections) {
            if (detection.id == tagId) {
                double bearing = detection.ftcPose.bearing;


                if (Math.abs(bearing) < threshold) {
                    follower.setTeleOpDrive(0, 0, 0, true);
                    return true;
                }


                double power = -bearing * kP;
                power = Math.max(-0.25, Math.min(0.25, power));

                follower.setTeleOpDrive(0, 0, power, true);
                return false;
            }
        }

        return false;
    }


    public double getBearing(int tagId) {
        List<AprilTagDetection> detections = aprilTagProcessor.getDetections();
        if (detections == null) return 0;

        for (AprilTagDetection detection : detections) {
            if (detection.id == tagId) {
                return detection.ftcPose.bearing;
            }
        }
        return 0;
    }
}