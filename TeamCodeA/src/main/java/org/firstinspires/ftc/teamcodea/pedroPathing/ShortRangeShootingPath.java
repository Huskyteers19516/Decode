package org.firstinspires.ftc.teamcodea.pedroPathing;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierCurve;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

@Autonomous(name = "Pedro Long Range Auto", group = "Autonomous")
public class ShortRangeShootingPath extends OpMode {

    private Follower follower;
    private Paths paths;
    private int pathState = 0;

    public static class Paths {
        public final PathChain Path1;
        public final PathChain Path2;

        public Paths(Follower follower) {

            Path1 = follower
                    .pathBuilder()
                    .addPath(new BezierLine(
                            new Pose(127.428, 125.819),
                            new Pose(113.753, 112.636)
                    ))
                    .setTangentHeadingInterpolation()
                    .build();

            Path2 = follower
                    .pathBuilder()
                    .addPath(new BezierCurve(
                            new Pose(113.752, 112.626),
                            new Pose(93.801, 85.113),
                            new Pose(86.883, 10.780)
                    ))
                    .setTangentHeadingInterpolation()
                    .build();
        }
    }

    @Override
    public void init() {
        follower = Constants.createFollower(hardwareMap);
        follower.setStartingPose(new Pose(72, 8, Math.toRadians(90)));

        paths = new Paths(follower);

        telemetry.addLine("Status: Initialized");
        telemetry.update();
    }

    @Override
    public void loop() {
        follower.update();

        pathState = updatePathState(pathState);

        Pose p = follower.getPose();
        telemetry.addData("State", pathState);
        telemetry.addData("X", p.getX());
        telemetry.addData("Y", p.getY());
        telemetry.addData("Heading (deg)", Math.toDegrees(p.getHeading()));
        telemetry.update();
    }

    private int updatePathState(int state) {

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
                return 2;

            default:
                return 2;
        }
    }
}