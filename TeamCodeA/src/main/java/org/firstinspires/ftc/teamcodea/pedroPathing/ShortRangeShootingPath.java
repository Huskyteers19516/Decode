package org.firstinspires.ftc.teamcodea.pedroPathing;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierCurve;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

// Make sure your Constants.java has the same package:
// package org.firstinspires.ftc.teamcodea.pedroPathing;
import org.firstinspires.ftc.teamcodea.pedroPathing.Constants;

/**
 * Short-range autonomous using Pedro Pathing.
 * Single, top-level OpMode class.
 */
@Autonomous(name = "Short Range Shooting", group = "Autonomous")
public class ShortRangeShootingPath extends OpMode {

    public Follower follower;

    // Simple state machine
    private int pathState = 0;
    private Paths paths;


    @Override
    public void init() {
        // Create follower from your Constants helper
        follower = Constants.createFollower(hardwareMap);

        // Set your real starting pose (must match your field setup)
        follower.setStartingPose(new Pose(72, 8, Math.toRadians(90)));

        // Build paths
        paths = new Paths(follower);

        telemetry.addLine("Status: Initialized");
        telemetry.update();
    }

    @Override
    public void loop() {
        // Always update the follower
        follower.update();

        // Run state machine
        pathState = autonomousPathUpdate(pathState);

        // Telemetry (Driver Station)
        Pose p = follower.getPose();
        telemetry.addData("Path State", pathState);
        telemetry.addData("X", p.getX());
        telemetry.addData("Y", p.getY());
        telemetry.addData("Heading (deg)", Math.toDegrees(p.getHeading()));
        telemetry.update();
    }
    private int autonomousPathUpdate(int state) {
        switch (state) {
            case 0:
                follower.followPath(paths.Path1);
                return 1;

            case 1:
                if (!follower.isBusy()) {
                    follower.followPath(paths.Path2);
                    return 2;
                }
                return 1;

            case 2:
                // Finished; you could add mechanism actions or park here.
                return 2;

            default:
                return 2;
        }
    }

    /** Path definitions */
    public static class Paths {
        public final PathChain Path1;
        public final PathChain Path2;

        public Paths(Follower follower) {
            // Path1: a short line segment
            Path1 = follower
                    .pathBuilder()
                    .addPath(
                            new BezierLine(
                                    new Pose(128.876, 127.911),
                                    new Pose(118.901, 117.774)
                            )
                    )
                    .setLinearHeadingInterpolation(Math.toRadians(45), Math.toRadians(45))
                    .build();

            // Path2: a curve sequence
            Path2 = follower
                    .pathBuilder()
                    .addPath(
                            new BezierCurve(
                                    new Pose(118.901, 117.774),
                                    new Pose(110.212, 70.472),
                                    new Pose(42.798, 85.917),
                                    new Pose(101.202, 10.780),
                                    new Pose(17.216, 13.837)
                            )
                    )
                    .setLinearHeadingInterpolation(Math.toRadians(45), Math.toRadians(60))
                    .build();
        }
    }
}