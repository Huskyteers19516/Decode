package org.firstinspires.ftc.teamcodeb.pedroPathing;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierCurve;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcodeb.opmode.RobotBAutoTestingVersion;



    public class TestingPaths  {

        private final Follower follower;
        private final int routeSerial;
        public PathChain Path1,Path2,Path3,Path4,Path5,Path6;
        public TestingPaths(Follower follower, int routeSerial) {
            this.follower = follower;
            this.routeSerial = routeSerial;
            buildPaths();
        }

        private void buildPaths() {
            //for blue
            System.out.println("Building paths");

            if(routeSerial==1){
                System.out.println("Building paths 1");

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
                System.out.println("Building paths 2");
                //for red
                Path1 = follower.pathBuilder().addPath(
                                new BezierLine(
                                        new Pose(123.364, 122.394),

                                        new Pose(107.866, 106.927)
                                )
                        ).setConstantHeadingInterpolation(Math.toRadians(45))

                        .build();

                Path2 = follower.pathBuilder().addPath(
                                new BezierCurve(
                                        new Pose(107.866, 106.927),
                                        new Pose(43.067, 69.043),
                                        new Pose(129.208, 69.958)
                                )
                        ).setTangentHeadingInterpolation()

                        .build();

                Path3 = follower.pathBuilder().addPath(
                                new BezierCurve(
                                        new Pose(129.208, 69.958),
                                        new Pose(38.487, 44.400),
                                        new Pose(121.254, 46.952)
                                )
                        ).setConstantHeadingInterpolation(Math.toRadians(180))

                        .build();

                Path4 = follower.pathBuilder().addPath(
                                new BezierLine(
                                        new Pose(121.254, 46.952),

                                        new Pose(84.308, 56.858)
                                )
                        ).setTangentHeadingInterpolation()

                        .build();

                Path5 = follower.pathBuilder().addPath(
                                new BezierLine(
                                        new Pose(84.308, 56.858),

                                        new Pose(107.651, 106.894)
                                )
                        ).setTangentHeadingInterpolation()

                        .build();


            }



        }
    }

