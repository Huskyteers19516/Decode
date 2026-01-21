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
    lateinit var secondRowControlPoint: Pose
    lateinit var secondRow: Pose
    lateinit var thirdRow: Pose
    lateinit var thirdRowControlPoint: Pose
    lateinit var thirdRowEndPoint: Pose
    lateinit var endLocation: Pose


    fun buildPaths(follower: Follower, alliance: Alliance) {
        fun Pose.mirrorIfBlue(): Pose {
            return if (alliance == Alliance.BLUE) this.mirror() else this
        }

        startPosition = Pose(122.364, 122.394, Math.toRadians(36.0)).mirrorIfBlue()
        shootPosition = Pose(84.8704156479217, 78.42542787286067).mirrorIfBlue()
        firstRowControlPoint = Pose(104.04687882496938, 86.57894736842107).mirrorIfBlue()
        firstRow = Pose(139.34, 82.51).mirrorIfBlue()
        secondRowControlPoint = Pose(85.0, 58.7).mirrorIfBlue()
        secondRow = Pose(143.4, 57.0).mirrorIfBlue()
        thirdRow = Pose(142.6, 33.95).mirrorIfBlue()
        thirdRowControlPoint = Pose(59.5, 27.1).mirrorIfBlue()
        thirdRowEndPoint = Pose(143.22154222766218, 35.121175030599765).mirrorIfBlue()

        endLocation = Pose(100.0, 53.0).mirrorIfBlue()


        val goalLocation = Pose(144.0, 144.0).mirrorIfBlue()
        val aimHeading = calculateAimHeading(startPosition, goalLocation)

        fromStartToShoot = follower.pathBuilder().addPath(
            BezierLine(startPosition, shootPosition)
        ).setConstantHeadingInterpolation(aimHeading).build()

        pickUpFirstRow = follower.pathBuilder()
            .addPath(BezierCurve(shootPosition, firstRowControlPoint, firstRow))
            .setTangentHeadingInterpolation()
            .setReversed()
            .build()

        firstRowToShoot = follower.pathBuilder()
            .addPath(BezierLine(firstRow, shootPosition))
            .setConstantHeadingInterpolation(aimHeading)
            .build()

        pickUpSecondRow = follower.pathBuilder()
            .addPath(BezierCurve(shootPosition, secondRowControlPoint, secondRow))
            .setTangentHeadingInterpolation()
            .setReversed()
            .build()

        secondRowToShoot = follower.pathBuilder()
            .addPath(BezierCurve(secondRow, secondRowControlPoint, shootPosition))
            .setConstantHeadingInterpolation(aimHeading)
            .build()

        pickUpThirdRow = follower.pathBuilder()
            .addPaths(
                BezierCurve(shootPosition, thirdRowControlPoint, thirdRow),
                BezierLine(thirdRow, thirdRowEndPoint)
            )
            .setGlobalTangentHeadingInterpolation()
            .setGlobalReversed()
            .build()
    }

    companion object {
        fun calculateAimHeading(robot: Pose, goal: Pose): Double {
            return atan2(goal.y - robot.y, goal.x - robot.x)
            // alternate implementation
            // goal.minus(robot).asVector.theta
        }
        val obelisk = Pose(72.0, 144.0)

    }
}