package org.firstinspires.ftc.teamcode.pedroPathing;


import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierCurve;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

@Autonomous(name = "Pedro Pathing Autonomous", group = "Autonomous")

public class BlueHaveIntakeShortShoot extends OpMode {


    public Follower follower;
    private int pathState = 0;
    private Paths paths;


    public void init() {

        follower = Constants.createFollower(hardwareMap);
        follower.setStartingPose(new Pose(72, 8, Math.toRadians(90)));

        paths = new Paths(follower);


    }


    public void loop() {
        follower.update();


    }

    public static class Paths {

        public PathChain Path1, Path2, Path3, Path4, Path5, Path6, Path7, Path8;

        public Paths(Follower follower) {

            Path1 = follower.pathBuilder()
                    .addPath(new BezierLine(new Pose(21.762, 120.695), new Pose(43.061, 99.704)))
                    .setConstantHeadingInterpolation(Math.toRadians(135))
                    .build();

            Path2 = follower.pathBuilder()
                    .addPath(new BezierCurve(
                            new Pose(43.061, 99.704),
                            new Pose(78.714, 82.264),
                            new Pose(17.216, 86.239)
                    ))
                    .setTangentHeadingInterpolation()
                    .build();

            Path3 = follower.pathBuilder()
                    .addPath(new BezierLine(new Pose(17.216, 86.239), new Pose(43.215, 99.859)))
                    .setTangentHeadingInterpolation()
                    .build();

            Path4 = follower.pathBuilder()
                    .addPath(new BezierCurve(
                            new Pose(43.215, 99.859),
                            new Pose(88.592, 55.563),
                            new Pose(16.733, 62.749)
                    ))
                    .setTangentHeadingInterpolation()
                    .build();

            Path5 = follower.pathBuilder()
                    .addPath(new BezierLine(new Pose(16.733, 62.749), new Pose(43.061, 99.550)))
                    .setTangentHeadingInterpolation()
                    .build();

            Path6 = follower.pathBuilder()
                    .addPath(new BezierCurve(
                            new Pose(43.061, 99.550),
                            new Pose(95.383, 31.177),
                            new Pose(15.285, 39.741)
                    ))
                    .setTangentHeadingInterpolation()
                    .build();

            Path7 = follower.pathBuilder()
                    .addPath(new BezierLine(new Pose(15.285, 39.741), new Pose(43.215, 99.704)))
                    .setTangentHeadingInterpolation()
                    .build();

            Path8 = follower.pathBuilder()
                    .addPath(new BezierLine(new Pose(43.215, 99.704), new Pose(54.482, 48.000)))
                    .setTangentHeadingInterpolation()
                    .build();
        }
    }


}