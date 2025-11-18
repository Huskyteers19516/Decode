package org.firstinspires.ftc.teamcodea.opmode;

import android.graphics.Canvas;
import android.util.Size;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;


import java.util.ArrayList;
import java.util.List;
public class AprilTag {
    private AprilTagProcessor aprilTagProcessor;
    private AprilTagDetection aprilTagDetection;
    private VisionPortal visionPortal;
    private Telemetry telemetry;
    private List<AprilTagDetection> detectedTags = new ArrayList<>();
    public void loop(HardwareMap hardwareMap, Telemetry telemetry) {
        this.telemetry = telemetry;

        aprilTagProcessor = new AprilTagProcessor.Builder()
                .setDrawTagID(true)
                .setDrawTagOutline(true)
                .setDrawAxes(true)
                .setDrawCubeProjection(true)
                .setOutputUnits(DistanceUnit.INCH, AngleUnit.DEGREES)
                .build();

        VisionPortal.Builder builder = new VisionPortal.Builder();
        builder.setCamera(hardwareMap.get(WebcamName.class, "webcam 1"));
        builder.setCameraResolution(new Size(640,480));
        builder.addProcessor(aprilTagProcessor);

    }
    public void update(){

        detectedTags = aprilTagProcessor.getDetections();
    }

    public void detectionTelemetry(AprilTagDetection detectID){

        if(detectID==null){
            return;
        }
        if (detectID.metadata != null) {
            telemetry.addLine(String.format("\n==== (ID %d) %s", detectID.id, detectID.metadata.name));
            telemetry.addLine(String.format("XYZ %6.1f %6.1f %6.1f  (inch)", detectID.ftcPose.x, detectID.ftcPose.y, detectID.ftcPose.z));
            telemetry.addLine(String.format("PRY %6.1f %6.1f %6.1f  (deg)", detectID.ftcPose.pitch, detectID.ftcPose.roll, detectID.ftcPose.yaw));
            telemetry.addLine(String.format("RBE %6.1f %6.1f %6.1f  (inch, deg, deg)", detectID.ftcPose.range, detectID.ftcPose.bearing, detectID.ftcPose.elevation));
        } else {
            telemetry.addLine(String.format("\n==== (ID %d) Unknown", detectID.id));
            telemetry.addLine(String.format("Center %6.0f %6.0f   (pixels)", detectID.center.x, detectID.center.y));
        }
    }
    public List<AprilTagDetection>getDetections(){
        return detectedTags;
    }
    public AprilTagDetection getID(int id){
        for(AprilTagDetection detection : detectedTags){
            if(detection.id== id){
                return detection;
            }
        }
        return null;
    }

    public void stop(){
        if(visionPortal != null){
            visionPortal.close();
        }
    }
}

