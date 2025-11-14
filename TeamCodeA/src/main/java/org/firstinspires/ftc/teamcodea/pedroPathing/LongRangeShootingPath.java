package org.firstinspires.ftc.teamcodea.pedroPathing;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierCurve;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcodea.pedroPathing.Constants;

@Autonomous(name = "Long Range Shooting Path", group = "Autonomous")
public class LongRangeShootingPath extends OpMode {

    private Follower follower;
    private Paths paths;
    private int pathState = 0;

    public static class Paths {
        public final PathChain PathLong;

        public Paths(Follower follower) {
            PathLong = follower
                    .pathBuilder()
                    .addPath(new BezierCurve(
                            new Pose(127.428, 125.819),
                            new Pose(41.832, 110.695),
                            new Pose(130.807, 75.298),
                            new Pose(92.675, 26.869),
                            new Pose(17.216, 13.837)
                    ))
                    .setLinearHeadingInterpolation(Math.toRadians(45), Math.toRadians(45))
                    .build();
        }
    }

    @Override
    public void init() {
        follower = Constants.createFollower(hardwareMap);
        follower.setStartingPose(new Pose(72, 8, Math.toRadians(90)));
        paths = new Paths(follower);

        telemetry.addData("Status", "Initialized");
        telemetry.update();
    }

    @Override
    public void loop() {
        follower.update();

        switch (pathState) {
            case 0:
                follower.followPath(paths.PathLong);
                pathState = 1;
                break;

            case 1:
                if (!follower.isBusy()) pathState = 2;
                break;

            case 2:
                break;
        }

        Pose p = follower.getPose();
        telemetry.addData("State", pathState);
        telemetry.addData("X", p.getX());
        telemetry.addData("Y", p.getY());
        telemetry.addData("Heading", Math.toDegrees(p.getHeading()));
        telemetry.update();
    }
}