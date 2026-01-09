package org.firstinspires.ftc.teamcodeb.opmode;

import static org.firstinspires.ftc.teamcodeb.OpModeConstants.INTAKE_TARGET_VELOCITY;
import static org.firstinspires.ftc.teamcodeb.OpModeConstants.LAUNCHER_TARGET_VELOCITY;
import static org.firstinspires.ftc.teamcodeb.OpModeConstants.START_P1;
import static org.firstinspires.ftc.teamcodeb.OpModeConstants.START_P2;

import android.util.Size;
import com.pedropathing.follower.Follower;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.teamcodeb.OpModeConstants;
import org.firstinspires.ftc.teamcodeb.functions.CenterAprilTag;
import org.firstinspires.ftc.teamcodeb.functions.DistanceAdjust;
import org.firstinspires.ftc.teamcodeb.functions.FeederSequence;
import org.firstinspires.ftc.teamcodeb.pedroPathing.Constants;
import org.firstinspires.ftc.teamcodeb.pedroPathing.TestingPaths;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

import java.util.List;

@Autonomous(name = "RobotB Auto Testing", group = "Test")
public class RobotBAutoTestingVersion extends OpMode {

    private ElapsedTime autoTimer = new ElapsedTime();
    private ElapsedTime collectTimer = new ElapsedTime();

    private DcMotorEx launcher;
    private DcMotorEx intake;
    private Servo FeederA;
    private Servo FeederB;
    private Servo FeederC;
    private Servo AngleChanger;
    private FeederSequence feederSequence;

    public DistanceAdjust distanceAdjust;
    public CenterAprilTag centerAprilTag;
    private AprilTagProcessor aprilTagProcessor;
    private VisionPortal frontVisionPortal;
    private BallProcessor rearProcessor;
    private VisionPortal rearVisionPortal;

    private LaunchState launchState;
    public Alliance alliance = Alliance.RED;
    private TestingPaths testingPaths;

    private enum AutoState {
        PEDRO_PATH1, ALIGN, PREPARE, LAUNCH, WAIT_FOR_LAUNCH,
        PEDRO_PATH2, PEDRO_PATH3, SEARCH_FOR_BALLS, APPROACH_BALL,
        COLLECT_BALL, CHECK_BALL_COUNT, COMPLETE, PEDRO_PATH4, PEDRO_PATH5,
        GET_OFF_LINE
    }

    private AutoState autoState;
    private Follower follower;

    private int ballCount = 0;
    private final int TARGET_BALL_COUNT = 3;
    private boolean collectInitialized = false;

    @Override
    public void init() {
        launcher = hardwareMap.get(DcMotorEx.class, "launcher");
        intake = hardwareMap.get(DcMotorEx.class, "intake");
        FeederA = hardwareMap.get(Servo.class, "feederA");
        FeederB = hardwareMap.get(Servo.class, "feederB");
        FeederC = hardwareMap.get(Servo.class, "feederC");
        AngleChanger = hardwareMap.get(Servo.class, "AngleChanger");

        feederSequence = new FeederSequence(FeederA, FeederB, FeederC);

        launcher.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
        launcher.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
        launcher.setPIDFCoefficients(DcMotorEx.RunMode.RUN_USING_ENCODER, new PIDFCoefficients(OpModeConstants.PID_P, OpModeConstants.PID_I, OpModeConstants.PID_D, OpModeConstants.PID_F));

        int[] myPortalsList = VisionPortal.makeMultiPortalView(2, VisionPortal.MultiPortalLayout.HORIZONTAL);

        aprilTagProcessor = AprilTagProcessor.easyCreateWithDefaults();
        frontVisionPortal = new VisionPortal.Builder()
                .setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"))
                .addProcessor(aprilTagProcessor)
                .setCameraResolution(new Size(1280, 720))
                .setLiveViewContainerId(myPortalsList[0])
                .build();

        rearProcessor = new BallProcessor(5.0);
        rearVisionPortal = new VisionPortal.Builder()
                .setCamera(hardwareMap.get(WebcamName.class, "Webcam 2"))
                .addProcessor(rearProcessor.getProcessor())
                .setCameraResolution(new Size(640, 480))
                .setLiveViewContainerId(myPortalsList[1])
                .build();

        follower = Constants.createFollower(hardwareMap);
        distanceAdjust = new DistanceAdjust(follower, gamepad1, aprilTagProcessor);
        centerAprilTag = new CenterAprilTag(follower, aprilTagProcessor);

        autoState = AutoState.PEDRO_PATH1;
        launchState = LaunchState.IDLE;
        activateFrontCamera();
    }

    @Override
    public void init_loop() {
        if (gamepad1.b) {
            alliance = Alliance.RED;
            testingPaths = new TestingPaths(follower, 2);
            follower.setStartingPose(START_P1);
        } else if (gamepad1.x) {
            alliance = Alliance.BLUE;
            testingPaths = new TestingPaths(follower, 1);
            follower.setStartingPose(START_P2);
        }
    }

    @Override
    public void start() {
        autoTimer.reset();
        feederSequence.resetSequence();
    }

    @Override
    public void loop() {
        follower.update();
        feederSequence.update();

        double t = autoTimer.seconds();

        if (t >= 25.0 && autoState != AutoState.GET_OFF_LINE && autoState != AutoState.COMPLETE) {
            autoState = AutoState.GET_OFF_LINE;
        }

        switch (autoState) {
            case PEDRO_PATH1:
                launcher.setVelocity(LAUNCHER_TARGET_VELOCITY);
                follower.followPath(testingPaths.Path1);
                autoState = AutoState.ALIGN;
                break;

            case ALIGN:
                int targetId = (alliance == Alliance.RED) ? 20 : 22;
                List<AprilTagDetection> detections = aprilTagProcessor.getDetections();
                boolean tagVisible = false;
                if (detections != null) {
                    for (AprilTagDetection d : detections) {
                        if (d.id == targetId) {
                            tagVisible = true;
                            break;
                        }
                    }
                }

                if (tagVisible) {
                    centerAprilTag.start();
                    boolean centered = centerAprilTag.update(targetId, 0.02, 1.5);
                    boolean distanceOk = autoAlignAlliance(29);
                    if (centered && distanceOk) {
                        follower.setTeleOpDrive(0, 0, 0, true);
                        autoState = AutoState.PREPARE;
                    }
                } else {
                    follower.setTeleOpDrive(0, 0, 0.5, true);
                }
                break;

            case PREPARE:
                launcher.setVelocity(LAUNCHER_TARGET_VELOCITY);
                if (launcher.getVelocity() > OpModeConstants.LAUNCHER_MIN_VELOCITY) {
                    autoState = AutoState.LAUNCH;
                }
                break;

            case LAUNCH:
                if (launch(true)) {
                    autoState = AutoState.WAIT_FOR_LAUNCH;
                }
                break;

            case WAIT_FOR_LAUNCH:
                if (launch(false)) {
                    ballCount = 0;
                    activateRearCamera();
                    autoState = (t > 20) ? AutoState.PEDRO_PATH5 : AutoState.PEDRO_PATH2;
                }
                break;

            case PEDRO_PATH2:
                if (!follower.isBusy()) follower.followPath(testingPaths.Path2);
                if (!follower.isBusy()) autoState = AutoState.PEDRO_PATH3;
                break;

            case PEDRO_PATH3:
                if (!follower.isBusy()) follower.followPath(testingPaths.Path3);
                if (!follower.isBusy()) autoState = AutoState.SEARCH_FOR_BALLS;
                break;

            case SEARCH_FOR_BALLS:
                intake.setVelocity(INTAKE_TARGET_VELOCITY);
                if (rearProcessor.isBallFound()) autoState = AutoState.APPROACH_BALL;
                else follower.setTeleOpDrive(0, 0, 0.25, true);
                break;

            case APPROACH_BALL:
                if (!rearProcessor.isBallFound()) autoState = AutoState.SEARCH_FOR_BALLS;
                else {
                    double dist = rearProcessor.getDistance();
                    double offset = rearProcessor.getNormalizedOffsetX();
                    if (dist < 7.5) {
                        follower.setTeleOpDrive(0, 0, 0, true);
                        autoState = AutoState.COLLECT_BALL;
                    } else {
                        double drive = (dist > 15) ? 0.4 : 0.25;
                        follower.setTeleOpDrive(drive, 0, offset * 0.35, true);
                    }
                }
                break;

            case COLLECT_BALL:
                if (!collectInitialized) {
                    collectTimer.reset();
                    intake.setVelocity(INTAKE_TARGET_VELOCITY);
                    collectInitialized = true;
                }
                if (collectTimer.milliseconds() > 1200) {
                    ballCount++;
                    collectInitialized = false;
                    autoState = AutoState.CHECK_BALL_COUNT;
                }
                if (collectTimer.seconds() > 10.0 && ballCount > 0) {
                    collectInitialized = false;
                    intake.setVelocity(0);
                    autoState = AutoState.PEDRO_PATH4;
                }
                break;

            case CHECK_BALL_COUNT:
                if (ballCount >= TARGET_BALL_COUNT) {
                    intake.setVelocity(0);
                    autoState = AutoState.PEDRO_PATH4;
                } else {
                    autoState = AutoState.SEARCH_FOR_BALLS;
                }
                break;

            case PEDRO_PATH4:
                follower.followPath(testingPaths.Path4);
                autoState = AutoState.PEDRO_PATH5;
                break;

            case PEDRO_PATH5:
                follower.followPath(testingPaths.Path5);
                autoState = AutoState.ALIGN;
                break;

            case GET_OFF_LINE:
                intake.setVelocity(0);
                launcher.setVelocity(0);
                follower.followPath(testingPaths.Path4);
                autoState = AutoState.COMPLETE;
                break;

            case COMPLETE:
                stopAllMotors();
                telemetry.addData("FINAL BALL COUNT", ballCount);
                break;
        }

        telemetry.addData("State", autoState);
        telemetry.addData("Balls Inside", ballCount);
        telemetry.update();
    }

    private boolean launch(boolean requested) {
        switch (launchState) {
            case IDLE:
                if (requested) {
                    launchState = LaunchState.PREPARE;
                }
                break;
            case PREPARE:
                launcher.setVelocity(LAUNCHER_TARGET_VELOCITY);
                if (launcher.getVelocity() > OpModeConstants.LAUNCHER_MIN_VELOCITY) {
                    feederSequence.trigger();
                    launchState = LaunchState.LAUNCH;
                }
                break;
            case LAUNCH:
                if (!feederSequence.isBusy()) {
                    launchState = LaunchState.IDLE;
                    return true;
                }
                break;
        }
        return false;
    }

    private boolean autoAlignAlliance(double targetDist) {
        List<AprilTagDetection> detections = aprilTagProcessor.getDetections();
        if (detections == null || detections.isEmpty()) return false;
        AprilTagDetection tag = null;
        for (AprilTagDetection d : detections) {
            if (alliance == Alliance.RED && d.id == 20) tag = d;
            if (alliance == Alliance.BLUE && d.id == 22) tag = d;
        }
        if (tag == null) return false;
        double ef = tag.ftcPose.range - targetDist;
        double es = -tag.ftcPose.x;
        double et = -tag.ftcPose.yaw;
        double pf = Math.max(-0.25, Math.min(0.25, ef * 0.03));
        double ps = Math.max(-0.25, Math.min(0.25, es * 0.03));
        double pt = Math.max(-0.20, Math.min(0.20, et * 0.03));
        follower.setTeleOpDrive(pf, ps, pt, true);
        return Math.abs(ef) < 0.8 && Math.abs(es) < 0.8 && Math.abs(et) < 1.5;
    }

    private void activateRearCamera() {
        rearVisionPortal.resumeStreaming();
        frontVisionPortal.stopStreaming();
    }

    private void activateFrontCamera() {
        frontVisionPortal.resumeStreaming();
        rearVisionPortal.stopStreaming();
    }

    private void stopAllMotors() {
        launcher.setVelocity(0);
        intake.setVelocity(0);
        feederSequence.doubleCheckOff();
        follower.setTeleOpDrive(0, 0, 0, true);
    }

    class BallProcessor extends org.firstinspires.ftc.teamcodeb.functions.BallDistance {
        public BallProcessor(double ballDiameter) {
            super(hardwareMap.get(WebcamName.class, "Webcam 2"), ballDiameter);
        }
        public double getNormalizedOffsetX() {
            return (getX() - 160) / 160.0;
        }
        public org.firstinspires.ftc.vision.VisionProcessor getProcessor() {
            return (org.firstinspires.ftc.vision.VisionProcessor) this;
        }
    }

    private enum Alliance { RED, BLUE }
    private enum LaunchState { IDLE, PREPARE, LAUNCH }
}