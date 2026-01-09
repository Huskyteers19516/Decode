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
import org.firstinspires.ftc.teamcodeb.functions.*;
import org.firstinspires.ftc.teamcodeb.pedroPathing.Constants;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

@Configurable
@TeleOp
public class RobotBTeleOp extends OpMode {
    private LauncherController launcherController;
    private DistanceTracker distanceTracker;
    private AngleChanger angleChangerWrapper;
    private FeederSequence feederSequence;

    private AprilTagProcessor aprilTagProcessor;
    private VisionPortal visionPortal;
    public Follower follower;
    public static Pose startingPose;
    private TelemetryManager telemetryM;

    public DcMotorEx launcher;
    public DcMotorEx intake;
    public Servo FeederA;
    public Servo FeederB;
    public Servo FeederC;
    public Servo Angle_Changer_Hardware;

    private DistanceAdjust distanceAdjust;
    private FlyWheelVelocity flyWheelVelocity;

    public enum LaunchState {IDLE, SPIN_UP, LAUNCH, LAUNCHING}
    public LaunchState launchState = LaunchState.IDLE;
    private Pose holdingPoint;

    public enum Alliance {RED, BLUE}
    public Alliance alliance = Alliance.RED;
    public boolean slowMode = false;

    @Override
    public void init() {
        follower = Constants.createFollower(hardwareMap);
        follower.setStartingPose(startingPose == null ? new Pose() : startingPose);
        telemetryM = PanelsTelemetry.INSTANCE.getTelemetry();

        launcher = hardwareMap.get(DcMotorEx.class, "launcher");
        intake = hardwareMap.get(DcMotorEx.class, "intake");
        FeederA = hardwareMap.get(Servo.class, "feederA");
        FeederB = hardwareMap.get(Servo.class, "feederB");
        FeederC = hardwareMap.get(Servo.class, "feederC");
        Angle_Changer_Hardware = hardwareMap.get(Servo.class, "hood");

        angleChangerWrapper = new AngleChanger(Angle_Changer_Hardware);
        feederSequence = new FeederSequence(FeederA, FeederB, FeederC);

        WebcamName camera = hardwareMap.get(WebcamName.class, "Webcam 1");
        Position cameraPos = new Position(DistanceUnit.INCH, 1.5, 9, 13, 0);
        YawPitchRollAngles camAngle = new YawPitchRollAngles(AngleUnit.DEGREES, 0, 115, 0, 0);

        aprilTagProcessor = new AprilTagProcessor.Builder().setCameraPose(cameraPos, camAngle).build();
        visionPortal = new VisionPortal.Builder().setCamera(camera).addProcessor(aprilTagProcessor).build();

        distanceTracker = new DistanceTracker(follower, aprilTagProcessor, angleChangerWrapper, launcher);
        launcherController = new LauncherController(distanceTracker, launcher);
        distanceAdjust = new DistanceAdjust(follower, gamepad1, aprilTagProcessor);
        flyWheelVelocity = new FlyWheelVelocity();
        flyWheelVelocity.init(launcher, gamepad1, telemetry);

        launcher.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    @Override
    public void init_loop() {
        if (gamepad2.y) alliance = Alliance.RED;
        else if (gamepad2.x) alliance = Alliance.BLUE;
    }

    @Override
    public void start() {
        follower.startTeleopDrive();
    }

    @Override
    public void loop() {
        int targetTagId = (alliance == Alliance.RED) ? 20 : 22;

        launcherController.updateLauncher(targetTagId);

        feederSequence.update();

        if (gamepad1.rightBumperWasPressed() || gamepad2.rightBumperWasPressed()) {
            launch(true);
        } else {
            launch(false);
        }

        if (gamepad1.leftBumperWasPressed()) slowMode = !slowMode;

        distanceAdjust.update();
        if (!distanceAdjust.isAutoDriveActive() && holdingPoint == null) {
            double mult = slowMode ? 0.5 : 1.0;
            follower.setTeleOpDrive(-gamepad1.left_stick_y * mult, -gamepad1.left_stick_x * mult, -gamepad1.right_stick_x * mult, true);
        } else if (holdingPoint != null) {
            follower.holdPoint(holdingPoint);
        }

        if (gamepad1.left_trigger > 0.1) intake.setPower(gamepad1.left_trigger);
        else if (gamepad2.left_trigger > 0.1) intake.setPower(-0.6);
        else intake.setPower(0);

        follower.update();
        telemetryM.update();

        telemetry.addData("Ready to Shoot", launcherController.isReadyToShoot(50));
        telemetry.addData("Auto RPM", launcherController.getLastTargetRPM());
        telemetry.addData("Auto Angle", angleChangerWrapper.getPosition());
        telemetry.update();
    }

    void launch(boolean shotRequested) {
        switch (launchState) {
            case IDLE:
                if (shotRequested) {
                    holdingPoint = follower.getPose();
                    launchState = LaunchState.SPIN_UP;
                }
                break;
            case SPIN_UP:
                if (launcherController.isReadyToShoot(50)) {
                    launchState = LaunchState.LAUNCH;
                }
                break;
            case LAUNCH:
                feederSequence.trigger();
                launchState = LaunchState.LAUNCHING;
                break;
            case LAUNCHING:
                if (!feederSequence.isBusy()) {
                    launchState = LaunchState.IDLE;
                    holdingPoint = null;
                }
                break;
        }
    }
}