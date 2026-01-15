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
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
import org.firstinspires.ftc.teamcodea.OpModeConstants;
import org.firstinspires.ftc.teamcodea.pedroPathing.Constants;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

import java.util.List;

@Configurable
@TeleOp
public class RobotATeleOp extends OpMode {

    public static final Pose TARGET_P1 = new Pose(122.238, 121.003, Math.toRadians(45));
    public static final Pose TARGET_P2 = new Pose(122.238, 121.003, Math.toRadians(45));
    public static Pose startingPose;
    private final double slowModeMultiplier = 0.5;
    private AprilTagProcessor aprilTagProcessor;
    private VisionPortal visionPortal;
    private Follower follower;
    private TelemetryManager telemetryM;
    private DcMotorEx launcher;
    private CRServo leftFeeder, rightFeeder;
    private ElapsedTime feederTimer = new ElapsedTime();
    private LaunchState launchState;

    private boolean slowMode = false;
    private Alliance alliance = Alliance.RED;
    private boolean autoDrive29 = false;

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
        Position cameraPosition = new Position(DistanceUnit.INCH, -1.5, 9, 13, 0);
        YawPitchRollAngles angle = new YawPitchRollAngles(AngleUnit.DEGREES, 0, 115, 0, 0);

        aprilTagProcessor = new AprilTagProcessor.Builder()
                .setCameraPose(cameraPosition, angle)
                .build();

        visionPortal = new VisionPortal.Builder()
                .setCamera(camera)
                .addProcessor(aprilTagProcessor)
                .setAutoStartStreamOnBuild(true)
                .build();

        launcher.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        launcher.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, new PIDFCoefficients(300, 0, 0, 10));

        leftFeeder.setDirection(DcMotorSimple.Direction.REVERSE);
        leftFeeder.setPower(OpModeConstants.STOP_SPEED);
        rightFeeder.setPower(OpModeConstants.STOP_SPEED);

        telemetry.update();
    }

    @Override
    public void start() {
        follower.startTeleopDrive();
        launchState = LaunchState.IDLE;
    }

    @Override
    public void init_loop() {
        if (gamepad2.y) alliance = Alliance.RED;
        else if (gamepad2.x) alliance = Alliance.BLUE;
    }

    @Override
    public void loop() {

        if (gamepad1.leftBumperWasPressed()) slowMode = !slowMode;
        if (gamepad1.startWasPressed()) follower.setPose(new Pose());
        if (gamepad2.aWasPressed()) launcher.setVelocity(OpModeConstants.LAUNCHER_TARGET_VELOCITY);
        if (gamepad2.backWasPressed()) launcher.setVelocity(0);

        launch(gamepad2.rightBumperWasPressed());

        List<AprilTagDetection> detections = aprilTagProcessor.getDetections();

        if (gamepad1.yWasPressed()) autoDrive29 = true;

        boolean driverOverride =
                Math.abs(gamepad1.left_stick_y) > 0.05 ||
                        Math.abs(gamepad1.left_stick_x) > 0.05 ||
                        Math.abs(gamepad1.right_stick_x) > 0.05;

        if (driverOverride) autoDrive29 = false;

        if (gamepad1.yWasPressed()) autoDrive29 = !autoDrive29;

        if (autoDrive29) {

            AprilTagDetection tag = null;

            if (detections != null && !detections.isEmpty()) {
                for (AprilTagDetection d : detections) {
                    if (d.id == 20 || d.id == 24) {
                        tag = d;
                        break;
                    }
                }
            }

            if (tag == null) {
                autoDrive29 = false;
            } else {
                double currentDist = tag.ftcPose.range;
                double error = currentDist - 29;
                double power = 0.3;

                if (Math.abs(error) < 15.0) {
                    follower.setTeleOpDrive(0, 0, 0, true);
                    autoDrive29 = false;
                } else {
                    if (error < 0) follower.setTeleOpDrive(-power, 0, 0, true);
                    else if (error > 0) follower.setTeleOpDrive(power, 0, 0, true);
                }
            }
        }

        if (!autoDrive29) {
            follower.setTeleOpDrive(
                    -gamepad1.left_stick_y * (slowMode ? slowModeMultiplier : 1),
                    -gamepad1.left_stick_x * (slowMode ? slowModeMultiplier : 1),
                    -gamepad1.right_stick_x * (slowMode ? slowModeMultiplier : 1),
                    true
            );
        }

        follower.update();
        telemetryM.update();
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

    private enum LaunchState {IDLE, SPIN_UP, LAUNCH, LAUNCHING}

    private enum Alliance {RED, BLUE}
}