package org.firstinspires.ftc.teamcodeb.opmode;

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
import org.firstinspires.ftc.teamcodeb.OpModeConstants;
import org.firstinspires.ftc.teamcodeb.pedroPathing.Constants;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import org.firstinspires.ftc.teamcodeb.functions.FlyWheelVelocity;
import org.firstinspires.ftc.teamcodeb.functions.CenterAprilTag;
import org.firstinspires.ftc.teamcodeb.functions.DistanceAdjust;
import java.util.List;

@Configurable
@TeleOp
public class RobotBTeleOp extends OpMode {
    public static final Pose TARGET_P1 = new Pose(122.238, 121.003, Math.toRadians(45));
    public static final Pose TARGET_P2 = new Pose(122.238, 121.003, Math.toRadians(45));

    private AprilTagProcessor aprilTagProcessor;
    private VisionPortal visionPortal;
    public Follower follower;
    public static Pose startingPose;
    private TelemetryManager telemetryM;

    public DcMotorEx launcher;
    public DcMotorEx intake;
    public CRServo leftFeeder, rightFeeder;
    public ElapsedTime feederTimer = new ElapsedTime();
    private DistanceAdjust distanceAdjust;
    private FlyWheelVelocity flyWheelVelocity;

    public enum LaunchState {IDLE, SPIN_UP, LAUNCH, LAUNCHING}
    public LaunchState launchState;

    public boolean slowMode = false;
    public final double slowModeMultiplier = 0.5;

    public enum Alliance {RED, BLUE}
    public Alliance alliance = Alliance.RED;

    public final double kP_AIM = 0.02;
    public final double MAX_TURN = 0.5;
    public final double AIM_THRESHOLD = 2;

    @Override
    public void init() {
        follower = Constants.createFollower(hardwareMap);
        follower.setStartingPose(startingPose == null ? new Pose() : startingPose);
        follower.update();

        telemetryM = PanelsTelemetry.INSTANCE.getTelemetry();

        launcher = hardwareMap.get(DcMotorEx.class, "launcher");
        leftFeeder = hardwareMap.get(CRServo.class, "left_feeder");
        rightFeeder = hardwareMap.get(CRServo.class, "right_feeder");
        intake = hardwareMap.get(DcMotorEx.class,"intake");

        WebcamName camera = hardwareMap.get(WebcamName.class, "Webcam 1");
        Position cameraPosition = new Position(DistanceUnit.INCH, 1.5,9,13,0);
        YawPitchRollAngles angle = new YawPitchRollAngles(AngleUnit.DEGREES, 0,115,0,0);

        aprilTagProcessor = new AprilTagProcessor.Builder().setCameraPose(cameraPosition, angle).build();
        visionPortal = new VisionPortal.Builder().setCamera(camera).addProcessor(aprilTagProcessor).setAutoStartStreamOnBuild(true).build();

        distanceAdjust = new DistanceAdjust(follower, gamepad1, aprilTagProcessor);
        flyWheelVelocity = new FlyWheelVelocity();
        flyWheelVelocity.init(launcher, gamepad1, telemetry);

        launcher.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        launcher.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, new PIDFCoefficients(300, 0, 0, 10));

        leftFeeder.setDirection(DcMotorSimple.Direction.REVERSE);
        leftFeeder.setPower(OpModeConstants.STOP_SPEED);
        rightFeeder.setPower(OpModeConstants.STOP_SPEED);

        telemetry.update();
    }

    private Pose getPoseFromCamera() {
        aprilTagProcessor.getDetections().forEach(detection -> {
            detection.robotPose.getPosition();
        });
        List<AprilTagDetection> detections = aprilTagProcessor.getDetections();
        if (detections == null || detections.isEmpty()) return null;

        AprilTagDetection d = detections.get(0);
        double x = d.robotPose.getPosition().x;
        double y = d.robotPose.getPosition().y;
        double heading = d.robotPose.getOrientation().getYaw(AngleUnit.RADIANS);

        return new Pose(x, y, heading);
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
        if (gamepad1.xWasPressed()) intake.setVelocity(OpModeConstants.INTAKE_TARGET_VELOCITY);
        if (gamepad1.leftBumperWasPressed()) slowMode = !slowMode;
        if (gamepad1.startWasPressed()) follower.setPose(new Pose());

        if (gamepad2.aWasPressed()) {
            flyWheelVelocity.setTargetVelocity(OpModeConstants.LAUNCHER_TARGET_VELOCITY);
        }

        if (gamepad2.bWasPressed()) {
            flyWheelVelocity.resetToDefault();
        }

        if (gamepad2.backWasPressed()) {
            launcher.setVelocity(0);
            flyWheelVelocity.setTargetVelocity(0);
        }

        if (gamepad1.aWasPressed()) {
            distanceAdjust.startAutoDrive29();
        }

        if (gamepad1.bWasPressed()) {
            launcher.setVelocity(0);
            flyWheelVelocity.setTargetVelocity(0);
        }

        if (gamepad1.yWasPressed()) {
            flyWheelVelocity.setTargetVelocity(1600);
        }

        if (gamepad2.rightBumperWasPressed() || gamepad1.rightBumperWasPressed()) {
            launch(true);
        }

        distanceAdjust.update();
        flyWheelVelocity.update();

        double currentTargetVelocity = flyWheelVelocity.getTargetVelocity();
        boolean isAutoDriveActive = distanceAdjust.isAutoDriveActive();

        if (!isAutoDriveActive) {
            if (holdingPoint == null){
                telemetry.addLine("not holding");
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
            } else {
                telemetry.addLine("Holding");
                follower.holdPoint(holdingPoint);
            }
        }

        if (gamepad1.squareWasPressed()) {
            follower.setPose(new Pose());
        }

        intake.setPower(gamepad1.left_trigger);

        if (gamepad1.leftBumperWasPressed()) {
            slowMode = !slowMode;
        }

        follower.update();
        telemetryM.update();

        telemetry.addLine("=== Distance Adjust ===");
        telemetry.addData("Auto Drive Active", isAutoDriveActive ? "YES" : "NO");
        telemetry.addData("Trigger Button", "A Button (Gamepad1)");

        telemetry.update();
    }

    private Pose holdingPoint;
    void launch(boolean shotRequested) {
        switch (launchState) {
            case IDLE:
                holdingPoint = follower.getPose();
                intake.setVelocity(OpModeConstants.INTAKE_TARGET_VELOCITY);
                if (shotRequested) launchState = LaunchState.SPIN_UP;
                break;
            case SPIN_UP:
                intake.setVelocity(0);
                launcher.setVelocity(flyWheelVelocity.getTargetVelocity());
                if (launcher.getVelocity() > OpModeConstants.LAUNCHER_MIN_VELOCITY) launchState = LaunchState.LAUNCH;
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
                    holdingPoint = null;
                }
                break;
        }
    }
}