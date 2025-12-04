package org.firstinspires.ftc.teamcodeb.opmode;

import static org.firstinspires.ftc.teamcodeb.OpModeConstants.START_P1;
import static org.firstinspires.ftc.teamcodeb.OpModeConstants.START_P2;
import static org.firstinspires.ftc.teamcodeb.OpModeConstants.INTAKE_TARGET_VELOCITY;

import com.pedropathing.follower.Follower;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcodeb.OpModeConstants;
import org.firstinspires.ftc.teamcodeb.pedroPathing.Constants;
import org.firstinspires.ftc.teamcodeb.pedroPathing.Paths;

@Autonomous(name = "auto for no intake ", group = "Bot2")
public class RobotBAuto2 extends OpMode {

    private ElapsedTime shotTimer = new ElapsedTime();
    private ElapsedTime feederTimer = new ElapsedTime();
    private ElapsedTime autoTimer = new ElapsedTime();

    private DcMotorEx launcher;
    private DcMotorEx intake;
    private CRServo leftFeeder;
    private CRServo rightFeeder;

    private enum LaunchState { IDLE, PREPARE, LAUNCH }
    private LaunchState launchState;

    private enum Alliance { RED, BLUE }
    private Alliance alliance = Alliance.RED;

    private enum AutoState {
        LAUNCH,
        WAIT_FOR_LAUNCH,
        PEDRO_PATH1,
        PEDRO_PATH2,
        PEDRO_PATH3,
        COMPLETE
    }
    private AutoState autoState;

    private Follower follower;
    private Paths paths;
    public static boolean redOrNot = true;

    private int shotsToFire = 3;

    @Override
    public void init() {

        launcher = hardwareMap.get(DcMotorEx.class, "launcher");
        intake = hardwareMap.get(DcMotorEx.class, "intake");
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


        autoState = AutoState.PEDRO_PATH1;
        launchState = LaunchState.IDLE;

        telemetry.addData("Press X", "Select BLUE");
        telemetry.addData("Press B", "Select RED");
        telemetry.addData("Alliance", alliance);
        telemetry.update();
    }

    @Override
    public void init_loop() {

        if (gamepad1.b) {
            alliance = Alliance.RED;
            redOrNot = true;
            paths = new Paths(follower, 2);
            follower.setStartingPose(START_P1);
        } else if (gamepad1.x) {
            alliance = Alliance.BLUE;
            redOrNot = false;
            paths = new Paths(follower, 4);
            follower.setStartingPose(START_P2);
        }

        telemetry.addData("Alliance", alliance);
        telemetry.update();
    }

    @Override
    public void start() {
        autoTimer.reset();
    }

    @Override
    public void loop() {
        follower.update();

        switch (autoState) {

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
                    if (shotsToFire > 0) {
                        autoState = AutoState.LAUNCH;
                    } else {
                        launcher.setVelocity(0);
                        autoState = AutoState.PEDRO_PATH2;
                    }
                }
                break;

            case PEDRO_PATH2:
                intake.setVelocity(INTAKE_TARGET_VELOCITY);
                follower.followPath(paths.Path2);
                autoState = AutoState.PEDRO_PATH3;
                break;

            case PEDRO_PATH3:
                intake.setVelocity(0);
                follower.followPath(paths.Path3);
                autoState = AutoState.COMPLETE;
                break;


            case COMPLETE:
                break;
        }

        telemetry.addData("Auto State", autoState);
        telemetry.addData("Launcher State", launchState);
        telemetry.addData("Launcher Velocity", launcher.getVelocity());
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