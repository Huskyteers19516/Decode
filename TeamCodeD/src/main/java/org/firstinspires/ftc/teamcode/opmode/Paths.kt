package org.firstinspires.ftc.teamcode.opmode

import com.pedropathing.follower.Follower
import com.pedropathing.geometry.BezierCurve
import com.pedropathing.geometry.BezierLine
import com.pedropathing.geometry.Pose
import com.pedropathing.paths.PathChain
import org.firstinspires.ftc.teamcode.utils.Alliance
import kotlin.math.atan2

class Paths {

    lateinit var fromStartToShoot: PathChain
    lateinit var pickUpFirstRow: PathChain
    lateinit var firstRowToShoot: PathChain
    lateinit var pickUpSecondRow: PathChain
    lateinit var secondRowToShoot: PathChain
    lateinit var pickUpThirdRow: PathChain
    lateinit var startPosition: Pose
    lateinit var shootPosition: Pose
    lateinit var firstRowControlPoint: Pose
    lateinit var firstRow: Pose
    lateinit var secondRow: Pose
    lateinit var secondToShootControlPoint: Pose
    lateinit var secondRowControlPoint: Pose
    lateinit var thirdRow: Pose
    lateinit var thirdRowControlPoint: Pose
    lateinit var endLocation: Pose


    fun buildPaths(follower: Follower, alliance: Alliance) {
        fun mirrorIfBlue(pose: Pose): Pose {
            return if (alliance == Alliance.BLUE) pose.mirror() else pose
        }

        startPosition = mirrorIfBlue(Pose(122.364, 122.394, Math.toRadians(36.0)))
        shootPosition = mirrorIfBlue(Pose(84.8704156479217, 78.42542787286067))
        firstRowControlPoint = mirrorIfBlue(Pose(64.1, 79.0))
        firstRow = mirrorIfBlue(Pose(139.34, 82.51))
        secondRow = mirrorIfBlue(Pose(143.4, 57.0))
        secondToShootControlPoint = mirrorIfBlue(Pose(79.8, 79.8))
        secondRowControlPoint = mirrorIfBlue(Pose(59.0, 58.7))
        thirdRow = mirrorIfBlue(Pose(142.6, 33.95))
        thirdRowControlPoint = mirrorIfBlue(Pose(59.5, 27.1))

        endLocation = mirrorIfBlue(Pose(100.0, 53.0))


        val goalLocation = mirrorIfBlue(Pose(144.0, 144.0))
        val aimHeading = calculateAimHeading(startPosition, goalLocation)

        fromStartToShoot = follower.pathBuilder().addPath(
            BezierLine(startPosition, shootPosition)
        ).setConstantHeadingInterpolation(aimHeading).build()

        pickUpFirstRow = follower.pathBuilder()
            .addPath(BezierCurve(shootPosition, firstRowControlPoint, firstRow))
            .setTangentHeadingInterpolation()
            .build()

        firstRowToShoot = follower.pathBuilder()
            .addPath(BezierLine(firstRow, shootPosition))
            .setConstantHeadingInterpolation(aimHeading)
            .build()

        pickUpSecondRow = follower.pathBuilder()
            .addPath(BezierCurve(shootPosition, secondRowControlPoint, secondRow))
            .setTangentHeadingInterpolation()
            .build()

        secondRowToShoot = follower.pathBuilder()
            .addPath(BezierCurve(secondRow, secondToShootControlPoint, shootPosition))
            .setConstantHeadingInterpolation(aimHeading)
            .build()

        pickUpThirdRow = follower.pathBuilder()
            .addPath(BezierCurve(shootPosition, thirdRowControlPoint, thirdRow))
            .setTangentHeadingInterpolation()
            .build()
    }

    companion object {
        fun calculateAimHeading(robot: Pose, goal: Pose): Double {
            return atan2(goal.y - robot.y, goal.x - robot.x)
            // alternate implementation
            // goal.minus(robot).asVector.theta
        }
    }
}