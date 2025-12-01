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
// gamepad 1
    //    A =====> central shooting position assistance
    //    B =====> facing goal assistance
    //    X =====> stop central shooting position assistance
    //    Y =====> stop facing goal assistance
    // start=====> direction pose
// gamepad 2
    //    A =====> start the launcher
    //    B =====> stop the launcher
    //    X =====>  red alliance
    //    Y =====>  blue alliance
    // right bumper  =====> shoot


    public static final Pose TARGET_P1 =
            new Pose(122.238, 121.003, Math.toRadians(45));

    public static final Pose TARGET_P2 =
            new Pose(122.238, 121.003, Math.toRadians(45));


    private AprilTagProcessor aprilTagProcessor;
    private VisionPortal visionPortal;
    private boolean aprilTagDetected = false;
    private AprilTagDetection desiredTag = null;
    private boolean apriltagRed = false;

    private Follower follower;
    public static Pose startingPose;
    private TelemetryManager telemetryM;

    private DcMotorEx launcher;
    private CRServo leftFeeder, rightFeeder;
    private ElapsedTime feederTimer = new ElapsedTime();

    private enum LaunchState {IDLE, SPIN_UP, LAUNCH, LAUNCHING}

    private LaunchState launchState;

    private boolean slowMode = false;
    private final double slowModeMultiplier = 0.5;

    private boolean redAlliance = false;

    @Override
    public void init() {

        follower = Constants.createFollower(hardwareMap);
        follower.setStartingPose(startingPose == null ? new Pose() : startingPose);
        follower.update();

        telemetryM = PanelsTelemetry.INSTANCE.getTelemetry();

        launcher = hardwareMap.get(DcMotorEx.class, "launcher");
        leftFeeder = hardwareMap.get(CRServo.class, "left_feeder");
        rightFeeder = hardwareMap.get(CRServo.class, "right_feeder");

        WebcamName camera = hardwareMap.get(WebcamName.class, "Webcam 1");
        aprilTagProcessor = new AprilTagProcessor.Builder().build();
        visionPortal = new VisionPortal.Builder()
                .setCamera(camera)
                .addProcessor(aprilTagProcessor)
                .setAutoStartStreamOnBuild(true)
                .build();

        launcher.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        launcher.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER,
                new PIDFCoefficients(300, 0, 0, 10));

        leftFeeder.setDirection(DcMotorSimple.Direction.REVERSE);

        leftFeeder.setPower(OpModeConstants.STOP_SPEED);
        rightFeeder.setPower(OpModeConstants.STOP_SPEED);

        telemetry.addLine("Press X = BLUE, Y = RED");
        telemetry.update();
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

        if (gamepad2.y) {
            redAlliance = false;
            telemetry.addLine("Alliance: BLUE");
        }
        if (gamepad2.x) {
            redAlliance = true;
            telemetry.addLine("Alliance: RED");
        }

        if (gamepad2.aWasPressed())
            launcher.setVelocity(OpModeConstants.LAUNCHER_TARGET_VELOCITY);

        if (gamepad2.backWasPressed())
            launcher.setVelocity(0);

        launch(gamepad2.rightBumperWasPressed());

        aprilTagDetected = false;
        desiredTag = null;

        if (detections != null) {
            for (AprilTagDetection detection : detections) {

                desiredTag = detection;
                aprilTagDetected = true;

                if (detection.id == 20) apriltagRed = true;
                else if (detection.id == 24) apriltagRed = false;

                telemetry.addData("Found", "ID %d (%s)", desiredTag.id, desiredTag.metadata.name);
                telemetry.addData("Range", detection.ftcPose.range);
                telemetry.addData("Bearing", detection.ftcPose.bearing);
                telemetry.addData("Yaw", detection.ftcPose.yaw);

                break;
            }
        }

        if (gamepad1.b) {

            if (!aprilTagDetected) {
                follower.turnTo(follower.getPose().getHeading() + Math.toRadians(180));
            } else if (aprilTagDetected) {
                if (redAlliance) {
                    if (!apriltagRed) {
                        follower.turnTo(follower.getPose().getHeading() + Math.toRadians(90));
                    } else {
                        telemetry.addLine("Correct RED AprilTag detected");
                    }
                } else if (!redAlliance) {
                    if (apriltagRed) {
                        follower.turnTo(follower.getPose().getHeading() - Math.toRadians(90));
                    } else {
                        telemetry.addLine("Correct BLUE AprilTag detected");
                    }
                }
            }



        // unfinished one
        if (gamepad1.a) {

            Pose pose = follower.getPose();
            telemetry.addData("Robot X", pose.getX());
            telemetry.addData("Robot Y", pose.getY());
            telemetry.addData("Robot Heading (deg)", Math.toDegrees(pose.getHeading()));

            Pose target = redAlliance ? TARGET_P1 : TARGET_P2;

            telemetry.addData("Target X", target.getX());
            telemetry.addData("Target Y", target.getY());
            telemetry.addData("Target Heading (deg)", Math.toDegrees(target.getHeading()));

            double dx = target.getX() - pose.getX();
            double dy = target.getY() - pose.getY();
            double dHeading = target.getHeading() - pose.getHeading();

            telemetry.addData("suggested X ", dx);
            telemetry.addData("suggested Y", dy);
            telemetry.addData("suggested Heading", Math.toDegrees(dHeading));
        }



            telemetry.update();
            telemetryM.debug("position", follower.getPose());
            telemetryM.debug("velocity", follower.getVelocity());



        }



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