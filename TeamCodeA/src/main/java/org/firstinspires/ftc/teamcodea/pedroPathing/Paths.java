package org.firstinspires.ftc.teamcodea.pedroPathing;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierCurve;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;

public class Paths {

    private final Follower follower;
    private final int routeSerial;

    public PathChain Path1, Path2, Path3, Path4, Path5, Path6, Path7, Path8;

    public Paths(Follower follower, int routeSerial) {
        this.follower = follower;
        this.routeSerial = routeSerial;

        buildPaths();
    }

    private void buildPaths() {

        if (routeSerial == 1) {

            Path1 = follower.pathBuilder()
                    .addPath(new BezierLine(
                            new Pose(21.762, 120.695),
                            new Pose(43.061, 99.704)
                    ))
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
                    .addPath(new BezierLine(
                            new Pose(17.216, 86.239),
                            new Pose(43.215, 99.859)
                    ))
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
                    .addPath(new BezierLine(
                            new Pose(16.733, 62.749),
                            new Pose(43.061, 99.550)
                    ))
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
                    .addPath(new BezierLine(
                            new Pose(15.285, 39.741),
                            new Pose(43.215, 99.704)
                    ))
                    .setTangentHeadingInterpolation()
                    .build();

            Path8 = follower.pathBuilder()
                    .addPath(new BezierLine(
                            new Pose(43.215, 99.704),
                            new Pose(54.482, 48.000)
                    ))
                    .setTangentHeadingInterpolation()
                    .build();
        }else if (routeSerial ==2){
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
                            new BezierLine(new Pose(43.061, 99.704), new Pose(48.154, 129.646))
                    )
                    .setTangentHeadingInterpolation()
                    .build();

            Path3 = follower
                    .pathBuilder()
                    .addPath(
                            new BezierLine(new Pose(48.154, 129.646), new Pose(47.383, 35.653))
                    )
                    .setTangentHeadingInterpolation()
                    .build();
        }else if(routeSerial ==3){
            Path1 = follower
                    .pathBuilder()
                    .addPath(
                            new BezierLine(new Pose(122.701, 121.929), new Pose(101.865, 101.402))
                    )
                    .setConstantHeadingInterpolation(Math.toRadians(45))
                    .build();

            Path2 = follower
                    .pathBuilder()
                    .addPath(
                            new BezierCurve(
                                    new Pose(101.865, 101.402),
                                    new Pose(63.125, 79.486),
                                    new Pose(125.325, 82.727)
                            )
                    )
                    .setTangentHeadingInterpolation()
                    .build();

            Path3 = follower
                    .pathBuilder()
                    .addPath(
                            new BezierLine(new Pose(125.325, 82.727), new Pose(101.711, 101.556))
                    )
                    .setTangentHeadingInterpolation()
                    .build();

            Path4 = follower
                    .pathBuilder()
                    .addPath(
                            new BezierCurve(
                                    new Pose(101.711, 101.556),
                                    new Pose(58.804, 51.859),
                                    new Pose(126.559, 58.804)
                            )
                    )
                    .setTangentHeadingInterpolation()
                    .build();

            Path5 = follower
                    .pathBuilder()
                    .addPath(
                            new BezierLine(new Pose(126.559, 58.804), new Pose(101.865, 101.402))
                    )
                    .setTangentHeadingInterpolation()
                    .build();

            Path6 = follower
                    .pathBuilder()
                    .addPath(
                            new BezierCurve(
                                    new Pose(101.865, 101.402),
                                    new Pose(41.672, 30.714),
                                    new Pose(124.707, 34.881)
                            )
                    )
                    .setTangentHeadingInterpolation()
                    .build();

            Path7 = follower
                    .pathBuilder()
                    .addPath(
                            new BezierLine(new Pose(124.707, 34.881), new Pose(101.556, 101.248))
                    )
                    .setTangentHeadingInterpolation()
                    .build();

            Path8 = follower
                    .pathBuilder()
                    .addPath(
                            new BezierLine(new Pose(101.556, 101.248), new Pose(86.122, 54.482))
                    )
                    .setTangentHeadingInterpolation()
                    .build();
        }else if(routeSerial==4){
            Path1 = follower
                    .pathBuilder()
                    .addPath(
                            new BezierLine(new Pose(122.238, 121.929), new Pose(101.865, 101.402))
                    )
                    .setConstantHeadingInterpolation(Math.toRadians(135))
                    .build();

            Path2 = follower
                    .pathBuilder()
                    .addPath(
                            new BezierLine(new Pose(101.865, 101.402), new Pose(91.987, 132.270))
                    )
                    .setTangentHeadingInterpolation()
                    .build();

            Path3 = follower
                    .pathBuilder()
                    .addPath(
                            new BezierLine(new Pose(91.987, 132.270), new Pose(91.061, 34.109))
                    )
                    .setTangentHeadingInterpolation()
                    .build();
        }

        }

        }

