package org.firstinspires.ftc.teamcodeb.functions;
import com.pedropathing.follower.Follower;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import org.firstinspires.ftc.vision.apriltag.*;
import java.util.List;
public class DistanceTracker {
        private AprilTagProcessor aprilTagProcessor;
        private Follower follower;
        private AngleChanger angleChanger;
        private DcMotorEx launcherMotor;

        private static final double[] DIST_POINTS = {29.0, 40.0, 50.0, 60.0};
        private static final double[] RPM_POINTS = {1200, 1600, 2100, 2500};
        private static final double[] ANGLE_POINTS = {0.25, 0.50, 0.60, 0.75};

        public DistanceTracker(Follower follower, AprilTagProcessor processor, AngleChanger angle, DcMotorEx motor) {
            this.follower = follower;
            this.aprilTagProcessor = processor;
            this.angleChanger = angle;
            this.launcherMotor = motor;
        }

    public double autoAdjustLauncher(int targetTagId) {
        List<AprilTagDetection> detections = aprilTagProcessor.getDetections();
        if (detections == null) return 0;

        for (AprilTagDetection d : detections) {
            if (d.id == targetTagId) {
                double dist = d.ftcPose.range;
                double targetRPM = interpolate(dist, DIST_POINTS, RPM_POINTS);
                double targetAngle = interpolate(dist, DIST_POINTS, ANGLE_POINTS);
                launcherMotor.setVelocity(targetRPM);
                angleChanger.setPosition(targetAngle);
                return targetRPM;
            }
        }
        return 0;
    }
        private double interpolate(double x, double[] xPts, double[] yPts) {
            if (x <= xPts[0]) return yPts[0];
            if (x >= xPts[xPts.length - 1]) return yPts[yPts.length - 1];

            for (int i = 0; i < xPts.length - 1; i++) {
                if (x < xPts[i + 1]) {
                    double factor = (x - xPts[i]) / (xPts[i + 1] - xPts[i]);
                    return yPts[i] + factor * (yPts[i + 1] - yPts[i]);
                }
            }
            return yPts[yPts.length - 1];
        }
    }
