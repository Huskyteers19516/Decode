package org.firstinspires.ftc.teamcodea.opmode;

import com.bylazar.configurables.annotations.Configurable;
import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.*;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.teamcodea.OpModeConstants;
import org.firstinspires.ftc.teamcodea.pedroPathing.Constants;

import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

import java.util.List;

@Configurable
@TeleOp
public class RobotATeleOp extends OpMode {
    private AprilTagProcessor aprilTagProcessor;
    private VisionPortal visionPortal;
    private Telemetry telemetry;


    private Follower follower;

    public static Pose startingPose;
    private TelemetryManager telemetryM;

    private WebcamName camera;

    private DcMotorEx launcher;
    private CRServo leftFeeder, rightFeeder;
    private ElapsedTime feederTimer = new ElapsedTime();

    private boolean slowMode = false;
    private final double slowModeMultiplier = 0.6;

    private boolean targetFound = false;
    private AprilTagDetection desiredTag = null;
    public static int DESIRED_TAG_ID = -1;
    private List<AprilTagDetection> currentDetections;

    private enum LaunchState { IDLE, SPIN_UP, LAUNCH, LAUNCHING }
    private LaunchState launchState;

    public boolean faceToGoal = false;

    @Override
    public void init() {

        follower = Constants.createFollower(hardwareMap);
        follower.setStartingPose(startingPose == null ? new Pose() : startingPose);
        follower.update();

        telemetryM = PanelsTelemetry.INSTANCE.getTelemetry();

        launcher = hardwareMap.get(DcMotorEx.class, "launcher");
        leftFeeder = hardwareMap.get(CRServo.class, "left_feeder");
        rightFeeder = hardwareMap.get(CRServo.class, "right_feeder");
        camera = hardwareMap.get(WebcamName.class, "Webcam 1");
        aprilTagProcessor = new AprilTagProcessor.Builder().build();
        visionPortal = new VisionPortal.Builder().setCamera(camera).setAutoStartStreamOnBuild(true).addProcessor(aprilTagProcessor).build();

        launcher.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        launcher.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, new PIDFCoefficients(300, 0, 0, 10));

        leftFeeder.setDirection(DcMotorSimple.Direction.REVERSE);
        leftFeeder.setPower(OpModeConstants.STOP_SPEED);
        rightFeeder.setPower(OpModeConstants.STOP_SPEED);
    }

    @Override
    public void start() {
        follower.startTeleopDrive();
        launchState = LaunchState.IDLE;
    }

    @Override
    public void loop() {
        List<AprilTagDetection> detections = aprilTagProcessor.getDetections();

        follower.update();
        telemetryM.update();

        if (!slowMode) {
            follower.setTeleOpDrive(
                    -gamepad1.left_stick_y,
                    -gamepad1.left_stick_x,
                    -gamepad1.right_stick_x,
                    true
            );
        } else {
            follower.setTeleOpDrive(
                    -gamepad1.left_stick_y * slowModeMultiplier,
                    -gamepad1.left_stick_x * slowModeMultiplier,
                    -gamepad1.right_stick_x * slowModeMultiplier,
                    true
            );
        }

        if (gamepad1.leftBumperWasPressed()) slowMode = !slowMode;
        if (gamepad1.startWasPressed()) follower.setPose(new Pose());

        if (gamepad1.yWasPressed()) launcher.setVelocity(OpModeConstants.LAUNCHER_TARGET_VELOCITY);
        if (gamepad1.bWasPressed()) launcher.setVelocity(0);

        launch(gamepad1.rightBumperWasPressed());

        if (gamepad1.bWasPressed() && targetFound) {
            telemetry.addLine("April tag spotted");
            double targetHeading = desiredTag.ftcPose.bearing;
            follower.turnTo(follower.getPose().getHeading() + Math.toRadians(targetHeading));
        }else{
            telemetry.addLine("April tag not spotted");
        }

        if (currentDetections != null) {
            targetFound = false;
            desiredTag = null;

            for (AprilTagDetection detection : currentDetections) {
                if (detection.metadata != null) {
                    if (DESIRED_TAG_ID < 0 || detection.id == DESIRED_TAG_ID) {
                        targetFound = true;
                        desiredTag = detection;
                        break;
                    } else {
                        telemetry.addData("Skipping", "Tag ID %d not desired", detection.id);
                    }
                } else {
                    telemetry.addData("Unknown", "Tag ID %d not in library", detection.id);
                }
            }

            if (targetFound) {
                telemetry.addData("Found", "ID %d (%s)", desiredTag.id, desiredTag.metadata.name);
                telemetry.addData("Range", "%.1f in", desiredTag.ftcPose.range);
                telemetry.addData("Bearing", "%.0f°", desiredTag.ftcPose.bearing);
                telemetry.addData("Yaw", "%.0f°", desiredTag.ftcPose.yaw);
            } else {
                telemetry.addData(">", "Use joysticks to find AprilTag");
            }
        }

        telemetryM.debug("position", follower.getPose());
        telemetryM.debug("velocity", follower.getVelocity());
    }

    void launch(boolean shotRequested) {
        switch (launchState) {
            case IDLE:
                if (shotRequested) launchState = LaunchState.SPIN_UP;
                break;

            case SPIN_UP:
                launcher.setVelocity(OpModeConstants.LAUNCHER_TARGET_VELOCITY);
                if (launcher.getVelocity() > OpModeConstants.LAUNCHER_MIN_VELOCITY)
                    launchState = LaunchState.LAUNCH;
                break;

            case LAUNCH:
                leftFeeder.setPower(OpModeConstants.FULL_SPEED);
                rightFeeder.setPower(OpModeConstants.FULL_SPEED);
                feederTimer.reset();
                launchState = LaunchState.LAUNCHING;
                break;

            case LAUNCHING:
                if (feederTimer.seconds() > OpModeConstants.FEED_TIME_SECONDS) {
                    leftFeeder.setPower(OpModeConstants.STOP_SPEED);
                    rightFeeder.setPower(OpModeConstants.STOP_SPEED);
                    launchState = LaunchState.IDLE;
                }
                break;
        }
    }
}