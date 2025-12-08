package org.firstinspires.ftc.teamcodea.opmode;

import static org.firstinspires.ftc.teamcodea.OpModeConstants.START_P1;
import static org.firstinspires.ftc.teamcodea.OpModeConstants.START_P2;

import com.pedropathing.follower.Follower;
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

import org.firstinspires.ftc.teamcodea.OpModeConstants;
import org.firstinspires.ftc.teamcodea.pedroPathing.Constants;

@Autonomous(name = "auto for no intake", group = "Bot2")
public class RobotAutoShort extends OpMode {

    private ElapsedTime shotTimer = new ElapsedTime();
    private ElapsedTime feederTimer = new ElapsedTime();
    private ElapsedTime autoTimer = new ElapsedTime();

    private DcMotorEx launcher;
    private CRServo leftFeeder;
    private CRServo rightFeeder;

    private enum LaunchState { IDLE, PREPARE, LAUNCH }
    private LaunchState launchState;

    private enum AutoState {
        WAITING,
        LAUNCH,
        WAIT_FOR_LAUNCH,
        PEDRO_PATH1,
        PEDRO_PATH2,
        COMPLETE
    }
    private AutoState autoState;

    private Follower follower;
    private Paths paths;

    private int shotsToFire = 3;

    @Override
    public void init() {
        launcher = hardwareMap.get(DcMotorEx.class, "launcher");
        leftFeeder = hardwareMap.get(CRServo.class, "left_feeder");
        rightFeeder = hardwareMap.get(CRServo.class, "right_feeder");

        launcher.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
        launcher.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
        launcher.setPIDFCoefficients(
                DcMotorEx.RunMode.RUN_USING_ENCODER,
                new PIDFCoefficients(
                        OpModeConstants.PID_P,
                        OpModeConstants.PID_I,
                        OpModeConstants.PID_D,
                        OpModeConstants.PID_F
                )
        );

        leftFeeder.setDirection(DcMotorSimple.Direction.REVERSE);
        follower = Constants.createFollower(hardwareMap);
        follower.setStartingPose(new Pose(22.364245810055866, 120.83128491620111, Math.toRadians(135)));

        autoState = AutoState.WAITING;
        launchState = LaunchState.IDLE;
    }

    @Override
    public void start() {
        paths = new Paths(follower);
        autoTimer.reset();
    }

    @Override
    public void loop() {
        follower.update();

        switch (autoState) {

            case WAITING:
                if (autoTimer.seconds() > 1) autoState = AutoState.PEDRO_PATH1;
                break;

            case PEDRO_PATH1:
                follower.followPath(paths.Path1);
                autoState = AutoState.LAUNCH;
                break;

            case LAUNCH:
                launch(true);
                autoState = AutoState.WAIT_FOR_LAUNCH;
                break;

            case WAIT_FOR_LAUNCH:
                if (launch(false)) {
                    shotsToFire--;
                    if (shotsToFire > 0) autoState = AutoState.LAUNCH;
                    else {
                        launcher.setVelocity(0);
                        autoState = AutoState.PEDRO_PATH2;
                    }
                }
                break;

            case PEDRO_PATH2:
                follower.followPath(paths.Path2);
                autoState = AutoState.COMPLETE;
                break;

            case COMPLETE:
                break;
        }
    }

    public static class Paths {

        public PathChain Path1;
        public PathChain Path2;

        public Paths(Follower follower) {
            Path1 = follower
                    .pathBuilder()
                    .addPath(
                            new BezierLine(new Pose(22.364, 120.831), new Pose(47.464, 95.410))
                    )
                    .setConstantHeadingInterpolation(Math.toRadians(135))
                    .build();

            Path2 = follower
                    .pathBuilder()
                    .addPath(
                            new BezierLine(new Pose(47.464, 95.410), new Pose(60.818, 128.072))
                    )
                    .setTangentHeadingInterpolation()
                    .build();
        }
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
                launcher.setVelocity(OpModeConstants.LAUNCHER_TARGET_VELOCITY);
                if (launcher.getVelocity() > OpModeConstants.LAUNCHER_MIN_VELOCITY) {
                    launchState = LaunchState.LAUNCH;
                    leftFeeder.setPower(1);
                    rightFeeder.setPower(1);
                    feederTimer.reset();
                }
                break;

            case LAUNCH:
                if (feederTimer.seconds() > OpModeConstants.FEED_TIME) {
                    leftFeeder.setPower(0);
                    rightFeeder.setPower(0);
                    if (shotTimer.seconds() > OpModeConstants.TIME_BETWEEN_SHOTS) {
                        launchState = LaunchState.IDLE;
                        return true;
                    }
                }
                break;
        }
        return false;
    }
}