package org.firstinspires.ftc.teamcodeb.pedroPathing;

import android.graphics.Point;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierCurve;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;

public class TestingPaths {

    private final Follower follower;
    private final int routeSerial;
    public PathChain Path1, Path2,Path3,Path4,Path5;
    public TestingPaths(Follower follower, int routeSerial) {
        this.follower = follower;
        this.routeSerial = routeSerial;
        buildPaths();
    }

    private void buildPaths() {
        //for blue
        if(routeSerial==1){
            Path1 = follower.pathBuilder()
                    .addPath(new BezierLine(new Pose(follower.getPose().getX(),follower.getPose().getY()), new Pose(36.712, 106.78)))
                    .setConstantHeadingInterpolation(Math.toRadians(135))
                    .build();

            Path2 = follower
                    .pathBuilder()
                    .addPath(
                            new BezierCurve(
                                    new Pose(36.712, 106.780),
                                    new Pose(45.430, 117.346),
                                    new Pose(95.721, 68.731),
                                    new Pose(7.376, 70.072)
                            )
                    )
                    .setConstantHeadingInterpolation(Math.toRadians(180))
                    .setReversed()
                    .build();

            Path3= follower
                    .pathBuilder()
                    .addPath(
                            new BezierCurve(
                                    new Pose(7.376, 70.072),
                                    new Pose(117.010, 66.719),
                                    new Pose(44.256, 55.823),
                                    new Pose(22.296, 45.932)
                            )
                    )
                    .setConstantHeadingInterpolation(Math.toRadians(0))
                    .build();
            Path4 = follower.pathBuilder()
                    .addPath(new BezierLine(new Pose(follower.getPose().getX(),follower.getPose().getY()), new Pose(36.712, 106.78)))
                    .setConstantHeadingInterpolation(Math.toRadians(135))
                    .build();

            Path5 = follower
                    .pathBuilder()
                    .addPath(
                            new BezierCurve(
                                    new Pose(36.712, 106.780),
                                    new Pose(80.130, 88.680),
                                    new Pose(37.383, 8.382)
                            )
                    )
                    .setConstantHeadingInterpolation(Math.toRadians(0))
                    .build();

        }else if(routeSerial ==2){
            //for red
            Path1 = follower.pathBuilder()
                    .addPath(new BezierLine(new Pose(follower.getPose().getX(),follower.getPose().getY()), new Pose(108.79, 108.62)))
                    .setConstantHeadingInterpolation(Math.toRadians(135))
                    .build();

            Path2 = follower
                    .pathBuilder()
                    .addPath(
                            new BezierCurve(
                                    new Pose(108.790, 108.620),
                                    new Pose(43.753, 111.646),
                                    new Pose(89.853, 71.749),
                                    new Pose(133.271, 69.737)
                            )
                    )
                    .setConstantHeadingInterpolation(Math.toRadians(90))
                    .build();
            Path3 = follower
                    .pathBuilder()
                    .addPath(
                            new BezierCurve(
                                    new Pose(133.271, 69.737),
                                    new Pose(43.753, 111.646),
                                    new Pose(85.830, 4.359),
                                    new Pose(73.090, 62.864),
                                    new Pose(126.063, 45.765)
                            )
                    )
                    .setConstantHeadingInterpolation(Math.toRadians(180))
                    .setReversed()
                    .build();

            Path4 = follower.pathBuilder()
                    .addPath(new BezierLine(new Pose(follower.getPose().getX(),follower.getPose().getY()), new Pose(108.79, 108.62)))
                    .setConstantHeadingInterpolation(Math.toRadians(135))
                    .build();

            Path5 = follower
                    .pathBuilder()
                    .addPath(
                            new BezierCurve(
                                    new Pose(108.790, 108.620),
                                    new Pose(64.373, 77.951),
                                    new Pose(110.473, 29.169),
                                    new Pose(108.964, 6.538)
                            )
                    )
                    .setConstantHeadingInterpolation(Math.toRadians(180))
                    .setReversed()
                    .build();
        }



    }
}