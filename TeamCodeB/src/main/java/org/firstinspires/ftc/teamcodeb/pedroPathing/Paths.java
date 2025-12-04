package org.firstinspires.ftc.teamcodeb.pedroPathing;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierCurve;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;
/*
Path number 1 = Red routes have intake
Path number 2 = Red routes no intake
Path number 3 = Blue routes have intake
Path number 4 = Blue routes no intake
 */
public class Paths {

    private final Follower follower;
    private final int routeSerial;

    public PathChain Path1, Path2, Path3, Path4, Path5, Path6, Path7, Path8, Path9, Path10, Path11;

    public Paths(Follower follower, int routeSerial) {
        this.follower = follower;
        this.routeSerial = routeSerial;
        buildPaths();
    }

    private void buildPaths() {

        if (routeSerial == 1) {

            Path1 = follower.pathBuilder()
                    .addPath(new BezierLine(new Pose(122.238, 121.003), new Pose(101.865, 101.402)))
                    .setConstantHeadingInterpolation(Math.toRadians(45))
                    .build();

            Path2 = follower.pathBuilder()
                    .addPath(new BezierLine(new Pose(101.865, 101.402), new Pose(84.135, 83.664)))
                    .setTangentHeadingInterpolation()
                    .build();

            Path3 = follower.pathBuilder()
                    .addPath(new BezierLine(new Pose(84.135, 83.664), new Pose(125.992, 83.384)))
                    .setConstantHeadingInterpolation(Math.toRadians(180))
                    .build();

            Path4 = follower.pathBuilder()
                    .addPath(new BezierLine(new Pose(125.992, 83.384), new Pose(102.053, 101.443)))
                    .setConstantHeadingInterpolation(Math.toRadians(45))
                    .build();

            Path5 = follower.pathBuilder()
                    .addPath(new BezierLine(new Pose(102.053, 101.443), new Pose(83.295, 59.585)))
                    .setTangentHeadingInterpolation()
                    .build();

            Path6 = follower.pathBuilder()
                    .addPath(new BezierLine(new Pose(83.295, 59.585), new Pose(126.552, 59.585)))
                    .setConstantHeadingInterpolation(Math.toRadians(180))
                    .build();

            Path7 = follower.pathBuilder()
                    .addPath(new BezierLine(new Pose(126.552, 59.585), new Pose(102.193, 101.583)))
                    .setConstantHeadingInterpolation(Math.toRadians(45))
                    .build();

            Path8 = follower.pathBuilder()
                    .addPath(new BezierLine(new Pose(102.193, 101.583), new Pose(84.555, 33.407)))
                    .setTangentHeadingInterpolation()
                    .build();

            Path9 = follower.pathBuilder()
                    .addPath(new BezierLine(new Pose(84.555, 33.407), new Pose(128.232, 35.647)))
                    .setTangentHeadingInterpolation()
                    .build();

            Path10 = follower.pathBuilder()
                    .addPath(new BezierLine(new Pose(128.232, 35.647), new Pose(102.333, 101.443)))
                    .setConstantHeadingInterpolation(Math.toRadians(45))
                    .build();

            Path11 = follower.pathBuilder()
                    .addPath(new BezierLine(new Pose(102.333, 101.443), new Pose(102.258, 12.740)))
                    .setTangentHeadingInterpolation()
                    .build();
        }

        else if (routeSerial == 2) {
            Path1 = follower.pathBuilder()
                    .addPath(new BezierLine(new Pose(122.238, 121.003), new Pose(101.865, 101.402)))
                    .setConstantHeadingInterpolation(Math.toRadians(135))
                    .build();

            Path2 = follower.pathBuilder()
                    .addPath(new BezierLine(new Pose(101.865, 101.402), new Pose(91.987, 132.270)))
                    .setTangentHeadingInterpolation()
                    .build();

            Path3 = follower.pathBuilder()
                    .addPath(new BezierLine(new Pose(91.987, 132.270), new Pose(91.061, 34.109)))
                    .setTangentHeadingInterpolation()
                    .build();


        }else if (routeSerial == 3) {

                Path1 = follower
                        .pathBuilder()
                        .addPath(
                                new BezierLine(new Pose(21.762, 120.695), new Pose(43.061, 99.704))
                        )
                        .setConstantHeadingInterpolation(Math.toRadians(135))
                        .build();

                Path2 = follower
                        .pathBuilder()
                        .addPath(
                                new BezierLine(new Pose(43.061, 99.704), new Pose(56.136, 84.364))
                        )
                        .setTangentHeadingInterpolation()
                        .build();

                Path3 = follower
                        .pathBuilder()
                        .addPath(
                                new BezierLine(new Pose(56.136, 84.364), new Pose(16.519, 83.804))
                        )
                        .setConstantHeadingInterpolation(Math.toRadians(0))
                        .build();

                Path4 = follower
                        .pathBuilder()
                        .addPath(
                                new BezierLine(new Pose(16.519, 83.804), new Pose(42.977, 99.763))
                        )
                        .setConstantHeadingInterpolation(Math.toRadians(135))
                        .build();

                Path5 = follower
                        .pathBuilder()
                        .addPath(
                                new BezierLine(new Pose(42.977, 99.763), new Pose(62.996, 59.585))
                        )
                        .setTangentHeadingInterpolation()
                        .build();

                Path6 = follower
                        .pathBuilder()
                        .addPath(
                                new BezierLine(new Pose(62.996, 59.585), new Pose(15.679, 59.725))
                        )
                        .setConstantHeadingInterpolation(Math.toRadians(0))
                        .build();

                Path7 = follower
                        .pathBuilder()
                        .addPath(
                                new BezierLine(new Pose(15.679, 59.725), new Pose(43.397, 99.903))
                        )
                        .setConstantHeadingInterpolation(Math.toRadians(135))
                        .build();

                Path8 = follower
                        .pathBuilder()
                        .addPath(
                                new BezierLine(new Pose(43.397, 99.903), new Pose(58.516, 36.207))
                        )
                        .setTangentHeadingInterpolation()
                        .build();

                Path9 = follower
                        .pathBuilder()
                        .addPath(
                                new BezierLine(new Pose(58.516, 36.207), new Pose(15.819, 35.647))
                        )
                        .setConstantHeadingInterpolation(Math.toRadians(0))
                        .build();

                Path10 = follower
                        .pathBuilder()
                        .addPath(
                                new BezierLine(new Pose(15.819, 35.647), new Pose(43.257, 99.763))
                        )
                        .setConstantHeadingInterpolation(Math.toRadians(135))
                        .build();

                Path11 = follower
                        .pathBuilder()
                        .addPath(
                                new BezierLine(new Pose(43.257, 99.763), new Pose(41.997, 15.768))
                        )
                        .setTangentHeadingInterpolation()
                        .build();
            }


        else if (routeSerial == 4) {
            Path1 = follower.pathBuilder()
                    .addPath(new BezierLine(new Pose(21.762, 120.695), new Pose(43.061, 99.704)))
                    .setConstantHeadingInterpolation(Math.toRadians(135))
                    .build();

            Path2 = follower.pathBuilder()
                    .addPath(new BezierLine(new Pose(43.061, 99.704), new Pose(48.154, 129.646)))
                    .setTangentHeadingInterpolation()
                    .build();

            Path3 = follower.pathBuilder()
                    .addPath(new BezierLine(new Pose(48.154, 129.646), new Pose(47.383, 35.653)))
                    .setTangentHeadingInterpolation()
                    .build();
        }

        }
    }
