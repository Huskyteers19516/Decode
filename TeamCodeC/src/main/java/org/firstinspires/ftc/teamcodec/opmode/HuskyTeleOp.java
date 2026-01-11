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
import org.firstinspires.ftc.teamcodec.config.OuttakeConstants;
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
        feeders = new Feeders(hardwareMap);

        //#region Buttons
        GamepadEx driverOp = new GamepadEx(gamepad1);
        GamepadEx shooterOp = new GamepadEx(gamepad2);



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

        Button toggleShooter = new GamepadButton(shooterOp, GamepadKeys.Button.B);
        toggleShooter.whenPressed(new InstantCommand(outtake::toggle, outtake));
        Button increaseSpeed = new GamepadButton(shooterOp, GamepadKeys.Button.DPAD_UP);
        increaseSpeed.whenPressed(new InstantCommand(() -> outtake.setVelocity(outtake.getTargetVelocity() + 100), outtake));
        Button slightIncreaseSpeed = new GamepadButton(shooterOp, GamepadKeys.Button.DPAD_RIGHT);
        slightIncreaseSpeed.whenPressed(new InstantCommand(() -> outtake.setVelocity(outtake.getTargetVelocity() + 20), outtake));
        Button decreaseSpeed = new GamepadButton(shooterOp, GamepadKeys.Button.DPAD_DOWN);
        decreaseSpeed.whenPressed(new InstantCommand(() -> outtake.setVelocity(outtake.getTargetVelocity() - 100), outtake));
        Button slightDecreaseSpeed = new GamepadButton(shooterOp, GamepadKeys.Button.DPAD_LEFT);
        slightDecreaseSpeed.whenPressed(new InstantCommand(() -> outtake.setVelocity(outtake.getTargetVelocity() - 20), outtake));

        register(intake, outtake, feeders);
        schedule(new RunCommand(() -> outtake.start(), outtake));
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

        intake.setPower(gamepad2.right_trigger);
        telemetryM.addData("Intake", intake.active() ? "On" : "Off");
        telemetryM.addData("Outtake", outtake.canShoot() ? "READY" : "NOT READY");
        telemetryM.addData("Outtake velocity", outtake.getVelocity());
        telemetryM.addData("Outtake target velocity", outtake.getTargetVelocity());
        telemetryM.addData("Outtake power", outtake.getRawPower());

        telemetryM.addData("Drive Mode", isRobotCentric ? "Robot Centric" : "Field Centric");
        telemetryM.update(telemetry);
    }
}
