package org.firstinspires.ftc.teamcodec.opmode;


import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierCurve;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.seattlesolvers.solverslib.command.CommandOpMode;
import com.seattlesolvers.solverslib.command.InstantCommand;
import com.seattlesolvers.solverslib.command.ParallelDeadlineGroup;
import com.seattlesolvers.solverslib.command.ParallelRaceGroup;
import com.seattlesolvers.solverslib.command.SequentialCommandGroup;
import com.seattlesolvers.solverslib.command.StartEndCommand;
import com.seattlesolvers.solverslib.command.WaitCommand;
import com.seattlesolvers.solverslib.pedroCommand.FollowPathCommand;
import com.seattlesolvers.solverslib.pedroCommand.HoldPointCommand;

import org.firstinspires.ftc.robotcore.internal.system.Deadline;
import org.firstinspires.ftc.teamcodec.command.Shoot;
import org.firstinspires.ftc.teamcodec.pedroPathing.Constants;
import org.firstinspires.ftc.teamcodec.subsystem.Feeders;
import org.firstinspires.ftc.teamcodec.subsystem.Intake;
import org.firstinspires.ftc.teamcodec.subsystem.Outtake;
import org.firstinspires.ftc.teamcodec.utils.Alliance;

import java.util.concurrent.TimeUnit;

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
        follower.update();

        intake = new Intake(hardwareMap);
        outtake = new Outtake(hardwareMap);
        feeders = new Feeders(hardwareMap);


        register(intake, outtake, feeders);
        schedule(
                new SequentialCommandGroup(new ParallelDeadlineGroup(new WaitCommand(25000), new SequentialCommandGroup(
                        new InstantCommand(outtake::start, outtake),
                        new FollowPathCommand(follower, fromStartToShoot),
                        new Shoot(feeders, outtake, Feeders.Feeder.A),
                        new Shoot(feeders, outtake, Feeders.Feeder.B),
                        new ParallelRaceGroup(new StartEndCommand(intake::start, intake::stop), new FollowPathCommand(follower, pickUpFirstRow)),
                        new FollowPathCommand(follower, firstRowToShoot),
                        new Shoot(feeders, outtake, Feeders.Feeder.A),
                        new Shoot(feeders, outtake, Feeders.Feeder.B),
                        new ParallelRaceGroup(new StartEndCommand(intake::start, intake::stop), new FollowPathCommand(follower, pickUpSecondRow)),
                        new FollowPathCommand(follower, secondRowToShoot),
                        new Shoot(feeders, outtake, Feeders.Feeder.A),
                        new Shoot(feeders, outtake, Feeders.Feeder.B),
                        new ParallelRaceGroup(new StartEndCommand(intake::start, intake::stop), new FollowPathCommand(follower, pickUpThirdRow))
                )), new HoldPointCommand(follower, mirrorIfBlue(new Pose(100, 53)), true))
        );
    }

    private PathChain fromStartToShoot, pickUpFirstRow, firstRowToShoot, pickUpSecondRow, secondRowToShoot, pickUpThirdRow;

    private Pose mirrorIfBlue(Pose a) {
        return alliance == Alliance.BLUE ? a.mirror() : a;
    }



    void buildPaths() {
        Pose startPosition = mirrorIfBlue(new Pose(122.364, 122.394, Math.toRadians(36)));
        Pose shootPosition = mirrorIfBlue(new Pose(84.8704156479217, 78.42542787286067));
        Pose firstRowControlPoint = mirrorIfBlue(new Pose(64.1, 79));
        Pose firstRow = mirrorIfBlue(new Pose(139.34, 82.51));
        Pose secondRow = mirrorIfBlue(new Pose(143.4, 57));
        Pose secondToShootControlPoint = mirrorIfBlue(new Pose(79.8, 79.8));
        Pose secondRowControlPoint = mirrorIfBlue(new Pose(59, 58.7));
        Pose thirdRow = mirrorIfBlue(new Pose(142.6, 33.95));
        Pose thirdRowControlPoint = mirrorIfBlue(new Pose(59.5, 27.1));

        double goalHeading = alliance == Alliance.RED ? Math.toRadians(50) : Math.toRadians(180 - 50);
        follower.setStartingPose(mirrorIfBlue(startPosition));
        fromStartToShoot = follower.pathBuilder().addPath(
                new BezierLine(startPosition, shootPosition)
        ).setConstantHeadingInterpolation(goalHeading).build();

        pickUpFirstRow = follower.pathBuilder()
                .addPath(new BezierCurve(shootPosition, firstRowControlPoint, firstRow))
                .setTangentHeadingInterpolation()
                .build();

        firstRowToShoot = follower.pathBuilder()
                .addPath(new BezierLine(firstRow, shootPosition))
                .setConstantHeadingInterpolation(goalHeading)
                .build();

        pickUpSecondRow = follower.pathBuilder()
                .addPath(new BezierCurve(shootPosition, secondRowControlPoint, secondRow))
                .setTangentHeadingInterpolation()
                .build();

        secondRowToShoot = follower.pathBuilder()
                .addPath(new BezierCurve(secondRow, secondToShootControlPoint, shootPosition))
                .setConstantHeadingInterpolation(goalHeading)
                .build();

        pickUpThirdRow = follower.pathBuilder()
                .addPath(new BezierCurve(shootPosition, thirdRowControlPoint, thirdRow))
                .setTangentHeadingInterpolation()
                .build();
    }

    @Override
    public void initialize_loop() {
        if (gamepad1.b) {
            alliance = Alliance.RED;
            buildPaths();
        } else if (gamepad1.x) {
            alliance = Alliance.BLUE;
            buildPaths();
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
