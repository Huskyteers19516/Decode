package org.firstinspires.ftc.teamcodec.opmode;


import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.seattlesolvers.solverslib.command.CommandOpMode;
import com.seattlesolvers.solverslib.command.ConditionalCommand;
import com.seattlesolvers.solverslib.command.InstantCommand;
import com.seattlesolvers.solverslib.command.RunCommand;
import com.seattlesolvers.solverslib.command.button.Button;
import com.seattlesolvers.solverslib.command.button.GamepadButton;
import com.seattlesolvers.solverslib.gamepad.GamepadEx;
import com.seattlesolvers.solverslib.gamepad.GamepadKeys;
import com.seattlesolvers.solverslib.util.TelemetryData;

import org.firstinspires.ftc.teamcodec.command.Shoot;
import org.firstinspires.ftc.teamcodec.pedroPathing.Constants;
import org.firstinspires.ftc.teamcodec.subsystem.Feeders;
import org.firstinspires.ftc.teamcodec.subsystem.Intake;
import org.firstinspires.ftc.teamcodec.subsystem.Outtake;

@TeleOp
public class HuskyTeleOp extends CommandOpMode {
    Follower follower;
    private TelemetryManager telemetryM;

    boolean isRobotCentric = false;
    Intake intake;
    Outtake outtake;
    Feeders feeders;

    @Override
    public void initialize() {

        telemetryM = PanelsTelemetry.INSTANCE.getTelemetry();
        follower = Constants.createFollower(hardwareMap);
        follower.setStartingPose(new Pose());
        follower.update();

        intake = new Intake(hardwareMap);
        outtake = new Outtake(hardwareMap);
        outtake.setDefaultCommand(new RunCommand(outtake::start, outtake));
        feeders = new Feeders(hardwareMap);

        //#region Buttons
        GamepadEx driverOp = new GamepadEx(gamepad1);
        GamepadEx shooterOp = new GamepadEx(gamepad2);


        Button intakeButton = new GamepadButton(driverOp, GamepadKeys.Button.RIGHT_BUMPER);
        intakeButton.whenPressed(new ConditionalCommand(
                new InstantCommand(intake::start, intake),
                new InstantCommand(intake::stop, intake),
                () -> {
                    intake.toggle();
                    return intake.active();
                }
        ));

        Button switchDriveMode = new GamepadButton(driverOp, GamepadKeys.Button.A);
        switchDriveMode.whenPressed(() -> {
            gamepad1.rumble(200);
            isRobotCentric = !isRobotCentric;
        });

        Button shootFeederA = new GamepadButton(driverOp, GamepadKeys.Button.Y);
        shootFeederA.whenPressed(new Shoot(feeders, outtake, Feeders.Feeder.A));
        Button shootFeederB = new GamepadButton(driverOp, GamepadKeys.Button.B);
        shootFeederB.whenPressed(new Shoot(feeders, outtake, Feeders.Feeder.B));
        Button shootFeederC = new GamepadButton(driverOp, GamepadKeys.Button.X);
        shootFeederC.whenPressed(new Shoot(feeders, outtake, Feeders.Feeder.C));

        outtake.setDefaultCommand(new RunCommand(outtake::start, outtake));
        register(intake, outtake, feeders);
        //#endregion
    }

    @Override
    public void run() {
        super.run();
        follower.startTeleOpDrive();

        follower.setTeleOpDrive(-gamepad1.left_stick_y, -gamepad1.left_stick_x, -gamepad1.right_stick_x, isRobotCentric);
        follower.update();
        Pose pose = follower.getPose();
        if (pose != null) {
            telemetryM.addData("X", pose.getX());
            telemetryM.addData("Y", pose.getY());
            telemetryM.addData("Heading", pose.getHeading());
        } else {
            telemetryM.addData("Pose", "null");
        }
        telemetryM.addData("Intake", intake.active() ? "On" : "Off");
        telemetryM.addData("Outtake", outtake.canShoot() ? "READY" : "NOT READY");
        telemetryM.addData("Outtake velocity", outtake.getVelocity());

        telemetryM.addData("Drive Mode", isRobotCentric ? "Robot Centric" : "Field Centric");
        telemetryM.update();
    }
}
