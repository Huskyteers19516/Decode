package org.firstinspires.ftc.teamcode.opmode

import com.pedropathing.follower.Follower
import com.pedropathing.geometry.BezierCurve
import com.pedropathing.geometry.BezierLine
import com.pedropathing.geometry.Pose
import com.pedropathing.paths.PathChain
import org.firstinspires.ftc.teamcode.utils.Alliance
import kotlin.math.atan2

class TestingPaths {

    lateinit var fromStartToShoot: PathChain
    lateinit var pickUpSecondRow: PathChain
    lateinit var secondRowToShoot: PathChain
    lateinit var shootToGate: PathChain
    lateinit var gateToShoot: PathChain

    lateinit var startPosition: Pose
    lateinit var shootPosition: Pose
    lateinit var secondRow: Pose
    lateinit var secondRowControlPoint: Pose
    lateinit var gatePosition: Pose

    fun buildPaths(follower: Follower, alliance: Alliance) {

        fun mirrorIfBlue(pose: Pose): Pose {
            return if (alliance == Alliance.BLUE) pose.mirror() else pose
        }



        startPosition = mirrorIfBlue(
            Pose(122.364, 122.394, Math.toRadians(36.0))
        )

        shootPosition = mirrorIfBlue(
            Pose(84.870, 78.425)
        )

        secondRow = mirrorIfBlue(
            Pose(126.392, 59.584)
        )

        secondRowControlPoint = mirrorIfBlue(
            Pose(66.211, 55.131)
        )

        gatePosition = mirrorIfBlue(
            Pose(134.029, 69.612)
        )



        val goalLocation = mirrorIfBlue(Pose(144.0, 144.0))
        val aimHeading = calculateAimHeading(startPosition, goalLocation)

        follower.setStartingPose(startPosition)



        fromStartToShoot = follower.pathBuilder()
            .addPath(
                BezierLine(
                    startPosition,
                    shootPosition
                )
            )
            .setConstantHeadingInterpolation(aimHeading)
            .build()



        pickUpSecondRow = follower.pathBuilder()
            .addPath(
                BezierCurve(
                    shootPosition,
                    secondRowControlPoint,
                    secondRow
                )
            )
            .setTangentHeadingInterpolation()
            .build()



        secondRowToShoot = follower.pathBuilder()
            .addPath(
                BezierLine(
                    secondRow,
                    shootPosition
                )
            )
            .setConstantHeadingInterpolation(aimHeading)
            .build()



        shootToGate = follower.pathBuilder()
            .addPath(
                BezierLine(
                    shootPosition,
                    gatePosition
                )
            )
            .setTangentHeadingInterpolation()
            .build()



        gateToShoot = follower.pathBuilder()
            .addPath(
                BezierLine(
                    gatePosition,
                    shootPosition
                )
            )
            .setConstantHeadingInterpolation(aimHeading)
            .build()

    }

    companion object {
        fun calculateAimHeading(robot: Pose, goal: Pose): Double {
            return atan2(goal.y - robot.y, goal.x - robot.x)
        }
    }
}