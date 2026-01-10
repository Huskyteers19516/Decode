package org.firstinspires.ftc.teamcodec.opmode;


import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierCurve;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;
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
import org.firstinspires.ftc.teamcodec.utils.Alliance;

@TeleOp
public class HuskyAuto extends CommandOpMode {
    Follower follower;
    private TelemetryManager telemetryM;

    Intake intake;
    Outtake outtake;
    Feeders feeders;

    Alliance alliance = Alliance.RED;

    @Override
    public void initialize() {

        telemetryM = PanelsTelemetry.INSTANCE.getTelemetry();
        follower = Constants.createFollower(hardwareMap);
        follower.setStartingPose(new Pose());
        follower.update();

        intake = new Intake(hardwareMap);
        outtake = new Outtake(hardwareMap);
        feeders = new Feeders(hardwareMap);


        register(intake, outtake, feeders);
        schedule(

        );
    }

    private PathChain driveThere;

    void buildPaths() {
        driveThere = follower.pathBuilder().addPath(new BezierCurve(new Pose(1, 1), new Pose(2, 2))).build();
    }

    @Override
    public void initialize_loop() {
        if (gamepad1.b) {
            alliance = Alliance.RED;
        } else if (gamepad1.x) {
            alliance = Alliance.BLUE;
        }
        telemetry.addLine("Press B for red, X for blue");
        telemetry.addData("Alliance", alliance);
    }

    @Override
    public void run() {
        super.run();
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
        telemetryM.addData("Outtake target velocity", outtake.getTargetVelocity());
        telemetryM.addData("Outtake power", outtake.getRawPower());

        telemetryM.addData("Set Point", outtake.getSetPoint());
        telemetryM.update(telemetry);
    }
}
