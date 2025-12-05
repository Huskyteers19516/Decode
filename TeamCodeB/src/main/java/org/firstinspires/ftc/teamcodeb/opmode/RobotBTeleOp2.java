package org.firstinspires.ftc.teamcodeb.opmode;

import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
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

import java.util.List;
import java.util.Optional;

@Configurable
@TeleOp
public class RobotBTeleOp2 extends OpMode {
    /*
    Gamepad.1
                Left Bumper             slow mode
                Start                   reset bot
                left stick              move forth and back
                right stick             turn around
                B                       auto located Apriltag goal d
                Y                       auto closed location for shooting and drive to that position
                                        double click or manual move the bot my joystick will stop this action
                            ( Warning: to avoid crashing on others bot, this function should be only use in small adjustment )
            Gamepad.2
                A                       start firewheel to target location
                Back                    shot the firewheel
                right bumper            shoot
                Y                       choose for red alliance
                X                       choose for blue alliance
                B                       set back to target velocity
                right stick             increase/decrease current velocity


                leftover bottom which can be use
                    gamepad1  X A
                    gamepad2
     */
    public static final Pose TARGET_P1 = new Pose(122.238, 121.003, Math.toRadians(45));
    public static final Pose TARGET_P2 = new Pose(122.238, 121.003, Math.toRadians(45));


    private AprilTagProcessor aprilTagProcessor;
    private VisionPortal visionPortal;
    private Follower follower;
    public static Pose startingPose;
    private TelemetryManager telemetryM;

    private DcMotorEx launcher;
    private DcMotorEx intake;
    private CRServo leftFeeder, rightFeeder;
    private ElapsedTime feederTimer = new ElapsedTime();

    private enum LaunchState {IDLE, SPIN_UP, LAUNCH, LAUNCHING}
    private LaunchState launchState;

    private boolean slowMode = false;
    private final double slowModeMultiplier = 0.5;

    private enum Alliance {RED, BLUE}
    private Alliance alliance = Alliance.RED;

    private final double kP_AIM = 0.02;
    private final double MAX_TURN = 0.5;
    private final double AIM_THRESHOLD = 2;

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
        intake = hardwareMap.get(DcMotorEx.class,"intake");

        WebcamName camera = hardwareMap.get(WebcamName.class, "Webcam 1");
        Position cameraPosition = new Position(DistanceUnit.INCH, 1.5,9,13,0);
        YawPitchRollAngles angle = new YawPitchRollAngles(AngleUnit.DEGREES, 0,115,0,0);

        aprilTagProcessor = new AprilTagProcessor.Builder().setCameraPose(cameraPosition, angle).build();
        visionPortal = new VisionPortal.Builder().setCamera(camera).addProcessor(aprilTagProcessor).setAutoStartStreamOnBuild(true).build();

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
        if(gamepad1.xWasPressed()) intake.setVelocity(OpModeConstants.INTAKE_TARGET_VELOCITY);
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

        if (driverOverride) {
            autoDrive29 = false;
        }

        if (gamepad1.yWasPressed()) {
            autoDrive29 = !autoDrive29;
        }

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
                    follower.setTeleOpDrive(0,0,0,true);
                    autoDrive29 = false;
                } else {
                    if(error<0){
                        follower.setTeleOpDrive(-power,0,0,true);
                    }else if(error>0){
                        follower.setTeleOpDrive(power,0,0,true);
                    }

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

        double stick = -gamepad2.right_stick_y;
        double delta = stick * 200;

        double currentVelocity = launcher.getVelocity();
        double newVelocity = currentVelocity + delta;


        newVelocity = Math.max(0, Math.min(newVelocity, 4000));

        launcher.setVelocity(newVelocity);

        telemetry.addData("Launcher Current", currentVelocity);
        telemetry.addData("Launcher New", newVelocity);
        if(gamepad1.bWasPressed())launcher.setVelocity(OpModeConstants.LAUNCHER_TARGET_VELOCITY);
    }

    void launch(boolean shotRequested) {
        switch (launchState) {
            case IDLE:
                intake.setVelocity(OpModeConstants.INTAKE_TARGET_VELOCITY);
                if (shotRequested) launchState = LaunchState.SPIN_UP;
                break;
            case SPIN_UP:
                intake.setVelocity(0);
                launcher.setVelocity(OpModeConstants.LAUNCHER_TARGET_VELOCITY);
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
                }
                break;
        }
    }
}