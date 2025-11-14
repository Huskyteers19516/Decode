package org.firstinspires.ftc.teamcodea.opmode;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierCurve;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcodea.pedroPathing.Constants;

@Autonomous(name="StarterBotAuto_Pedro", group="StarterBot")
public class RobotAutoShort extends OpMode {

    static class Config {
        static double launcherTargetVelocity = 1125;
        static double launcherMinVelocity = 1075;
        static double PID_P = 300;
        static double PID_I = 0;
        static double PID_D = 0;
        static double PID_F = 10;
    }

    final double FEED_TIME = 0.20;
    final double LAUNCHER_TARGET_VELOCITY = 1125;
    final double LAUNCHER_MIN_VELOCITY = 1075;
    final double TIME_BETWEEN_SHOTS = 2;

    private ElapsedTime shotTimer = new ElapsedTime();
    private ElapsedTime feederTimer = new ElapsedTime();

    private DcMotorEx leftlauncher;
    private DcMotorEx rightlauncher;
    private CRServo leftFeeder;
    private CRServo rightFeeder;

    private enum LaunchState { IDLE, PREPARE, LAUNCH }
    private LaunchState launchState;

    private enum AutoState {
        LAUNCH,
        WAIT_FOR_LAUNCH,
        PEDRO_PATH1,
        PEDRO_PATH1_WAIT,
        PEDRO_PATH2,
        PEDRO_PATH2_WAIT,
        COMPLETE
    }
    private AutoState autoState;

    private Follower follower;
    private Paths paths;

    public static class Paths {
        public final PathChain Path1;
        public final PathChain Path2;

        public Paths(Follower follower) {
            Path1 = follower
                    .pathBuilder()
                    .addPath(new BezierLine(
                            new Pose(127.428, 125.819),
                            new Pose(113.752, 112.626)
                    ))
                    .setTangentHeadingInterpolation()
                    .build();

            Path2 = follower
                    .pathBuilder()
                    .addPath(new BezierCurve(
                            new Pose(113.752, 112.626),
                            new Pose(93.801, 85.113),
                            new Pose(86.883, 10.780)
                    ))
                    .setTangentHeadingInterpolation()
                    .build();
        }
    }

    @Override
    public void init() {
        autoState = AutoState.LAUNCH;
        launchState = LaunchState.IDLE;

        leftlauncher = hardwareMap.get(DcMotorEx.class,"left_launcher");
        rightlauncher = hardwareMap.get(DcMotorEx.class,"right_launcher");
        leftFeeder = hardwareMap.get(CRServo.class,"left_feeder");
        rightFeeder = hardwareMap.get(CRServo.class,"right_feeder");

        leftlauncher.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
        rightlauncher.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);

        leftlauncher.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
        rightlauncher.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);

        leftlauncher.setPIDFCoefficients(DcMotorEx.RunMode.RUN_USING_ENCODER,
                new PIDFCoefficients(Config.PID_P,Config.PID_I,Config.PID_D,Config.PID_F));
        rightlauncher.setPIDFCoefficients(DcMotorEx.RunMode.RUN_USING_ENCODER,
                new PIDFCoefficients(Config.PID_P,Config.PID_I,Config.PID_D,Config.PID_F));

        leftFeeder.setDirection(DcMotorSimple.Direction.REVERSE);

        follower = Constants.createFollower(hardwareMap);
        follower.setStartingPose(new Pose(72, 8, Math.toRadians(90)));
        paths = new Paths(follower);

        telemetry.addData("Status","Initialized");
    }

    @Override
    public void loop() {
        follower.update();

        switch (autoState) {
            case LAUNCH:
                launch(true);
                autoState = AutoState.WAIT_FOR_LAUNCH;
                break;

            case WAIT_FOR_LAUNCH:
                if (launch(false)) {
                    autoState = AutoState.PEDRO_PATH1;
                }
                break;

            case PEDRO_PATH1:
                follower.followPath(paths.Path1);
                autoState = AutoState.PEDRO_PATH1_WAIT;
                break;

            case PEDRO_PATH1_WAIT:
                if (!follower.isBusy()) {
                    autoState = AutoState.PEDRO_PATH2;
                }
                break;

            case PEDRO_PATH2:
                follower.followPath(paths.Path2);
                autoState = AutoState.PEDRO_PATH2_WAIT;
                break;

            case PEDRO_PATH2_WAIT:
                if (!follower.isBusy()) {
                    autoState = AutoState.COMPLETE;
                }
                break;

            case COMPLETE:
                break;
        }

        telemetry.addData("State", autoState);
        telemetry.update();
    }

    boolean launch(boolean shotRequested) {
        switch (launchState) {
            case IDLE:
                if (shotRequested) {
                    launchState = LaunchState.PREPARE;
                    shotTimer.reset();
                }
                break;

            case PREPARE:
                leftlauncher.setVelocity(LAUNCHER_TARGET_VELOCITY);
                rightlauncher.setVelocity(LAUNCHER_TARGET_VELOCITY);
                if (leftlauncher.getVelocity() > LAUNCHER_MIN_VELOCITY &&
                        rightlauncher.getVelocity() > LAUNCHER_MIN_VELOCITY) {
                    launchState = LaunchState.LAUNCH;
                    leftFeeder.setPower(1);
                    rightFeeder.setPower(1);
                    feederTimer.reset();
                }
                break;

            case LAUNCH:
                if (feederTimer.seconds() > FEED_TIME) {
                    leftFeeder.setPower(0);
                    rightFeeder.setPower(0);
                    if (shotTimer.seconds() > TIME_BETWEEN_SHOTS) {
                        launchState = LaunchState.IDLE;
                        return true;
                    }
                }
                break;
        }
        return false;
    }
}