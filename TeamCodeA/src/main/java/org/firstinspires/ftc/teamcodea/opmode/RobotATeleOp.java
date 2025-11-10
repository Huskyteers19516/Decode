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

import org.firstinspires.ftc.teamcodea.OpModeConstants;
import org.firstinspires.ftc.teamcodea.pedroPathing.Constants;

@Configurable
@TeleOp
public class RobotATeleOp extends OpMode {
    private Follower follower;
    public static Pose startingPose; //See ExampleAuto to understand how to use this
    private TelemetryManager telemetryM;
    ElapsedTime feederTimer = new ElapsedTime();

    private boolean slowMode = false;
    private final double slowModeMultiplier = 0.5;

    private enum LaunchState {
        IDLE,
        SPIN_UP,
        LAUNCH,
        LAUNCHING,
    }

    private LaunchState launchState;

    private DcMotorEx launcher;
    private CRServo leftFeeder = null;
    private CRServo rightFeeder = null;


    @Override
    public void init() {
        follower = Constants.createFollower(hardwareMap);
        follower.setStartingPose(startingPose == null ? new Pose() : startingPose);
        follower.update();
        telemetryM = PanelsTelemetry.INSTANCE.getTelemetry();
        launcher = hardwareMap.get(DcMotorEx.class, "launcher");
        leftFeeder = hardwareMap.get(CRServo.class, "left_feeder");
        rightFeeder = hardwareMap.get(CRServo.class, "right_feeder");

        launcher.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        leftFeeder.setPower(OpModeConstants.STOP_SPEED);
        rightFeeder.setPower(OpModeConstants.STOP_SPEED);

        launcher.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, new PIDFCoefficients(300, 0, 0, 10));

        /*
         * Much like our drivetrain motors, we set the left feeder servo to reverse so that they
         * both work to feed the ball into the robot.
         */
        leftFeeder.setDirection(DcMotorSimple.Direction.REVERSE);
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
                false // Robot Centric
        );

            //This is how it looks with slowMode on
        else follower.setTeleOpDrive(
                -gamepad1.left_stick_y * slowModeMultiplier,
                -gamepad1.left_stick_x * slowModeMultiplier,
                -gamepad1.right_stick_x * slowModeMultiplier,
                false // Robot Centric
        );

        if (gamepad1.squareWasPressed()) {
            follower.setPose(new Pose());
        }

        if (gamepad1.yWasPressed()) {
            launcher.setVelocity(1600);
        }
        if (gamepad1.bWasPressed()) {
            launcher.setVelocity(0);
        }
        launch(gamepad1.rightBumperWasPressed());

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
                launcher.setVelocity(OpModeConstants.LAUNCHER_TARGET_VELOCITY);
                if (launcher.getVelocity() > OpModeConstants.LAUNCHER_MIN_VELOCITY) {
                    launchState = LaunchState.LAUNCH;
                }
                break;
            case LAUNCH:
                leftFeeder.setPower(OpModeConstants.FULL_SPEED);
                rightFeeder.setPower(OpModeConstants.FULL_SPEED);
                feederTimer.reset();
                launchState = LaunchState.LAUNCHING;
                break;
            case LAUNCHING:
                if (feederTimer.seconds() > OpModeConstants.FEED_TIME_SECONDS) {
                    launchState = LaunchState.IDLE;
                    leftFeeder.setPower(OpModeConstants.STOP_SPEED);
                    rightFeeder.setPower(OpModeConstants.STOP_SPEED);
                }
                break;
        }
    }
}