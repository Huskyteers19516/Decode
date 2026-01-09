package org.firstinspires.ftc.teamcodeb.pedroPathing;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierCurve;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;

public class FarPathTesting {

    private final Follower follower;
    private final int routeSerial;

    public PathChain Path1, Path2, Path3, Path4, Path5;

    public FarPathTesting(Follower follower, int routeSerial) {
        this.follower = follower;
        this.routeSerial = routeSerial;
        buildPaths();
    }

    private void buildPaths() {

        if (routeSerial == 1) {

            Path1 = follower.pathBuilder()
                    .addPath(new BezierLine(
                            new Pose(63.663, 8.160),
                            new Pose(62.226, 10.776)
                    ))
                    .setConstantHeadingInterpolation(Math.toRadians(120))
                    .build();

            Path2 = follower.pathBuilder()
                    .addPath(new BezierCurve(
                            new Pose(62.226, 10.776),
                            new Pose(71.596, 83.853),
                            new Pose(12.874, 70.317)
                    ))
                    .setTangentHeadingInterpolation()
                    .build();

            Path3 = follower.pathBuilder()
                    .addPath(new BezierCurve(
                            new Pose(12.874, 70.317),
                            new Pose(113.376, 78.269),
                            new Pose(52.223, 46.425),
                            new Pose(17.035, 48.217)
                    ))
                    .setConstantHeadingInterpolation(Math.toRadians(10))
                    .build();

            Path4 = follower.pathBuilder()
                    .addPath(new BezierCurve(
                            new Pose(17.035, 48.217),
                            new Pose(70.932, 54.001),
                            new Pose(62.324, 10.792)
                    ))
                    .setConstantHeadingInterpolation(Math.toRadians(120))
                    .build();

            Path5 = follower.pathBuilder().addPath(
                            new BezierLine(
                                    new Pose(62.324, 10.792),

                                    new Pose(38.521, 9.747)
                            )
                    ).setTangentHeadingInterpolation()

                    .build();
        }

        if (routeSerial == 2) {

            Path1 = follower.pathBuilder()
                    .addPath(new BezierLine(
                            new Pose(follower.getPose().getX(), follower.getPose().getY()),
                            new Pose(108.790, 108.620)
                    ))
                    .setConstantHeadingInterpolation(Math.toRadians(135))
                    .build();

            Path2 = follower.pathBuilder()
                    .addPath(new BezierCurve(
                            new Pose(108.790, 108.620),
                            new Pose(43.753, 111.646),
                            new Pose(89.853, 71.749),
                            new Pose(133.271, 69.737)
                    ))
                    .setConstantHeadingInterpolation(Math.toRadians(90))
                    .build();

            Path3 = follower.pathBuilder()
                    .addPath(new BezierCurve(
                            new Pose(133.271, 69.737),
                            new Pose(43.753, 111.646),
                            new Pose(85.830, 4.359),
                            new Pose(73.090, 62.864),
                            new Pose(126.063, 45.765)
                    ))
                    .setConstantHeadingInterpolation(Math.toRadians(180))
                    .setReversed()
                    .build();

            Path4 = follower.pathBuilder()
                    .addPath(new BezierLine(
                            new Pose(follower.getPose().getX(), follower.getPose().getY()),
                            new Pose(108.790, 108.620)
                    ))
                    .setConstantHeadingInterpolation(Math.toRadians(135))
                    .build();

            Path5 = follower.pathBuilder()
                    .addPath(new BezierCurve(
                            new Pose(108.790, 108.620),
                            new Pose(64.373, 77.951),
                            new Pose(110.473, 29.169),
                            new Pose(108.964, 6.538)
                    ))
                    .setConstantHeadingInterpolation(Math.toRadians(180))
                    .setReversed()
                    .build();
        }
    }
}