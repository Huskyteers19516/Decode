package org.firstinspires.ftc.teamcodeb.opmode;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import java.util.List;
@TeleOp(name = "Distance", group = "Test")
public class Distance extends LinearOpMode {

    private AprilTagProcessor aprilTagProcessor;
    private VisionPortal visionPortal;

    @Override
    public void runOpMode() {
        aprilTagProcessor = new AprilTagProcessor.Builder().build();
        visionPortal = new VisionPortal.Builder()
                .setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"))
                .addProcessor(aprilTagProcessor)
                .build();

        waitForStart();

        while (opModeIsActive()) {
            List<AprilTagDetection> detections = aprilTagProcessor.getDetections();

            if (detections != null && !detections.isEmpty()) {
                AprilTagDetection detection = detections.get(0);
                telemetry.addData("Distance", "%.1f", detection.ftcPose.range);
            }

            telemetry.update();
        }

        if (visionPortal != null) {
            visionPortal.close();
        }
    }
}