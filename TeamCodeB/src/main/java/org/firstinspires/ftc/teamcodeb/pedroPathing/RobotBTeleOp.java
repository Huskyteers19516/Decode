package org.firstinspires.ftc.teamcodeb.pedroPathing;

import com.bylazar.configurables.annotations.Configurable;
import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.HeadingInterpolator;
import com.pedropathing.paths.Path;
import com.pedropathing.paths.PathChain;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import java.util.function.Supplier;

@Configurable
@TeleOp
public class RobotBTeleOp extends OpMode {
    private Follower follower;
    public static Pose startingPose; //See ExampleAuto to understand how to use this
    private TelemetryManager telemetryM;
    ElapsedTime feederTimer = new ElapsedTime();

    private boolean slowMode = false;
    private final double slowModeMultiplier = 0.5;

    double OUTTAKE_TARGET_VELOCITY = 1600;
    double OUTTAKE_MIN_VELOCITY = 1400;
    double LAUNCH_POS = 1.0;
    double IDLE_POS = 0.5;
    double FEED_TIME_SECONDS = 2.0;

    private enum LaunchState {
        IDLE,
        SPIN_UP,
        LAUNCH,
        LAUNCHING,
    }

    private LaunchState launchState;

    private DcMotorEx intake;
    private DcMotorEx outtake;
    private Servo pusher;

    @Override
    public void init() {
        follower = Constants.createFollower(hardwareMap);
        follower.setStartingPose(startingPose == null ? new Pose() : startingPose);
        follower.update();
        telemetryM = PanelsTelemetry.INSTANCE.getTelemetry();
        intake = hardwareMap.get(DcMotorEx.class, "intake");
        outtake = hardwareMap.get(DcMotorEx.class, "outtake");
        intake.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        outtake.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        outtake.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, new PIDFCoefficients(300, 0, 0, 0));
        pusher = hardwareMap.get(Servo.class, "pusher");
    }

    @Override
    public void start() {
        //The parameter controls whether the Follower should use break mode on the motors (using it is recommended).
        //In order to use float mode, add .useBrakeModeInTeleOp(true); to your Drivetrain Constants in Constant.java (for Mecanum)
        //If you don't pass anything in, it uses the default (false)
        follower.startTeleopDrive();
        launchState = LaunchState.IDLE;
    }

    @Override
    public void loop() {
        //Call this once per loop
        follower.update();
        telemetryM.update();

        //Make the last parameter false for field-centric
        //In case the drivers want to use a "slowMode" you can scale the vectors

        //This is the normal version to use in the TeleOp
        if (!slowMode) follower.setTeleOpDrive(
                -gamepad1.left_stick_y,
                -gamepad1.left_stick_x,
                -gamepad1.right_stick_x,
                true // Robot Centric
        );

            //This is how it looks with slowMode on
        else follower.setTeleOpDrive(
                -gamepad1.left_stick_y * slowModeMultiplier,
                -gamepad1.left_stick_x * slowModeMultiplier,
                -gamepad1.right_stick_x * slowModeMultiplier,
                true // Robot Centric
        );

        if (gamepad1.squareWasPressed()) {
            follower.setPose(new Pose());
        }

        if (gamepad1.yWasPressed()) {
            outtake.setVelocity(1600);
        }
        if (gamepad1.bWasPressed()) {
            outtake.setVelocity(0);
        }
        launch(gamepad1.rightBumperWasPressed());
        intake.setPower(gamepad1.left_trigger);

        //Slow Mode
        if (gamepad1.leftBumperWasPressed()) {
            slowMode = !slowMode;
        }



        telemetryM.debug("position", follower.getPose());
        telemetryM.debug("velocity", follower.getVelocity());
    }

    void launch(boolean shotRequested) {
        switch (launchState) {
            case IDLE:
                if (shotRequested) {
                    launchState = LaunchState.SPIN_UP;
                }
                break;
            case SPIN_UP:
                outtake.setVelocity(OUTTAKE_TARGET_VELOCITY);
                if (outtake.getVelocity() > OUTTAKE_MIN_VELOCITY) {
                    launchState = LaunchState.LAUNCH;
                }
                break;
            case LAUNCH:
                pusher.setPosition(LAUNCH_POS);
                feederTimer.reset();
                launchState = LaunchState.LAUNCHING;
                break;
            case LAUNCHING:
                if (feederTimer.seconds() > FEED_TIME_SECONDS) {
                    launchState = LaunchState.IDLE;
                    pusher.setPosition(IDLE_POS);
                }
                break;
        }
    }
}