package org.firstinspires.ftc.teamcodea.opmode;


import static org.firstinspires.ftc.teamcodea.OpModeConstants.START_P1;
import static org.firstinspires.ftc.teamcodea.OpModeConstants.START_P2;


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

import org.firstinspires.ftc.robotcore.internal.camera.delegating.DelegatingCaptureSequence;
import org.firstinspires.ftc.teamcodea.pedroPathing.BlueHaveIntakeShortShoot;
import org.firstinspires.ftc.teamcodea.OpModeConstants;
import org.firstinspires.ftc.teamcodea.pedroPathing.Constants;
import org.firstinspires.ftc.teamcodea.pedroPathing.Constants;

@Autonomous(name="Use This Auto", group="StarterBot")
public class RobotAutoShort extends OpMode {
    private ElapsedTime shotTimer = new ElapsedTime();
    private ElapsedTime feederTimer = new ElapsedTime();
    private ElapsedTime autoTimer = new ElapsedTime();

    private DcMotorEx launcher;
    private CRServo leftFeeder;
    private CRServo rightFeeder;

    private enum LaunchState { IDLE, PREPARE, LAUNCH }
    private LaunchState launchState;
    private enum Alliance{
        RED,
        BLUE,
    }
    private Alliance alliance = Alliance.RED;
    private enum AutoState {
        LAUNCH,
        WAIT_FOR_LAUNCH,
        PEDRO_PATH1,
        PEDRO_PATH1_WAIT,
        PEDRO_PATH2,
        PEDRO_PATH2_WAIT,
        PEDRO_PATH3,
        PEDRO_PATH3_WAIT,
        WAIT,
        COMPLETE
    }
    private AutoState autoState;

    private Follower follower;
    private Paths paths;
    public static boolean redOrNot = true;
    public static boolean waitOrNot = false;
    public static class Paths {

        public PathChain Path1;
        public PathChain Path2;

        public Paths(Follower follower) {
            Path1 = follower
                    .pathBuilder()
                    .addPath(
                            new BezierLine(new Pose(122.238, 121.003), new Pose(100.630, 100.013))
                    )
                    .setConstantHeadingInterpolation(Math.toRadians(45))
                    .build();

            Path2 = follower
                    .pathBuilder()
                    .addPath(
                            new BezierLine(new Pose(100.630, 100.013), new Pose(83.035, 40.437))
                    )
                    .setTangentHeadingInterpolation()
                    .build();
        }
    }



    @Override
    public void init() {

        if (gamepad1.b) {
        alliance = Alliance.RED;
        redOrNot= true;
    } else if (gamepad1.x) {
        alliance = Alliance.BLUE;
        redOrNot = false;
    }

        if(redOrNot= true){
            follower.setStartingPose(START_P1);
        }else{
            follower.setStartingPose(START_P2);
        }

        telemetry.addData("Press X", "for BLUE");
        telemetry.addData("Press B", "for RED");
        telemetry.addData("Selected Alliance", alliance);


        autoState = AutoState.PEDRO_PATH1;
        launchState = LaunchState.IDLE;

        launcher = hardwareMap.get(DcMotorEx.class,"launcher");
        leftFeeder = hardwareMap.get(CRServo.class,"left_feeder");
        rightFeeder = hardwareMap.get(CRServo.class,"right_feeder");

        launcher.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);

        launcher.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);

        launcher.setPIDFCoefficients(DcMotorEx.RunMode.RUN_USING_ENCODER,
                new PIDFCoefficients(OpModeConstants.PID_P,OpModeConstants.PID_I,OpModeConstants.PID_D,OpModeConstants.PID_F));

        leftFeeder.setDirection(DcMotorSimple.Direction.REVERSE);

        follower = Constants.createFollower(hardwareMap);
        //follower.setStartingPose();
        paths = new Paths(follower);

        telemetry.addData("Status","Initialized");


    }

    private int shotsToFire = 3;

    @Override
    public void loop() {
        follower.update();

        switch (autoState) {
            case PEDRO_PATH1:
                autoTimer.reset();
                follower.followPath(paths.Path1);
                autoState = AutoState.PEDRO_PATH1_WAIT;
                break;

            case PEDRO_PATH1_WAIT:
                if (!follower.isBusy()) {
                    autoState = AutoState.LAUNCH;
                }
                break;
            case LAUNCH:
                launch(true);
                autoState = AutoState.WAIT_FOR_LAUNCH;
                break;

            case WAIT_FOR_LAUNCH:

                if (launch(false)) {
                    shotsToFire -= 1;

                    if (shotsToFire > 0) {
                        autoState = AutoState.LAUNCH;
                    } else {
                        launcher.setVelocity(0);
                        autoState = AutoState.PEDRO_PATH2;
                    }
                }
                break;

            case PEDRO_PATH2:
                follower.followPath(paths.Path2);
                autoState = AutoState.PEDRO_PATH2_WAIT;
                break;

            case PEDRO_PATH2_WAIT:
                if (!follower.isBusy()) {
                    if(pathNumber >2&& waitOrNot=true){
                        autoState = AutoState.WAIT;
                    }else(pathNumber >2 && waitOrNot =false){
                        autoState = AutoState.PEDRO_PATH3;
                    }else(pathNumber<2){
                        autoState = AutoState.COMPLETE;
                    }
                }
                break;
            case WAIT:
                if (autoTimer.seconds() > 25) {
                    autoState= AutoState.PEDRO_PATH3;
                }
            case PEDRO_PATH3:
                follower.followPath(paths.Path3);
                autoState= AutoState.PEDRO_PATH3_WAIT;
                break;

            case PEDRO_PATH3_WAIT:
                if (!follower.isBusy()) {
                    autoState = AutoState.COMPLETE;
                }
                break;
            case COMPLETE:
                break;
        }

        telemetry.addData("State", autoState);
        telemetry.addData("Launcher State", launchState);
        telemetry.addData("Launcher Velocity", launcher.getVelocity());
        telemetry.addData("Shot Timer", shotTimer.seconds());
        telemetry.addData("Feed Timer", feederTimer.seconds());
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
