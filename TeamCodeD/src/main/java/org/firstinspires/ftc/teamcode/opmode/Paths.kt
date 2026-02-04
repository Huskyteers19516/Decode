package org.firstinspires.ftc.teamcode.opmode

import com.pedropathing.follower.Follower
import com.pedropathing.geometry.BezierCurve
import com.pedropathing.geometry.BezierLine
import com.pedropathing.geometry.Pose
import com.pedropathing.paths.PathChain
import org.firstinspires.ftc.teamcode.constants.AutoConstants
import org.firstinspires.ftc.teamcode.utils.Alliance
import kotlin.math.atan2
import kotlin.math.sqrt
import kotlin.properties.Delegates

class Paths(private val follower: Follower) {
    init {
        buildPaths(Alliance.RED)
    }
    lateinit var fromStartToShoot: PathChain
    lateinit var pickUpFirstRow: PathChain
    lateinit var pickUpFirstRowWithGate: PathChain
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
    lateinit var innerEndLocation: Pose
    lateinit var goalLocation: Pose
    var aimHeading = 0.0;


    fun buildPaths(alliance: Alliance) {
        fun Pose.mirrorIfBlue(): Pose {
            return if (alliance == Alliance.BLUE) this.mirror() else this
        }

        // Assume robot starts aligned with goal, facing obelisk wall, with line in the middle of the drive train
        // https://www.desmos.com/calculator/qgkrj2eng6
        startPosition = Pose(130.046 - ROBOT_WIDTH / sqrt(2.0) / 2, 130.046 - ROBOT_WIDTH / sqrt(2.0) / 2, Math.toRadians(126.0)).mirrorIfBlue()
        shootPosition = Pose(84.8704156479217, 78.42542787286067).mirrorIfBlue()
        firstRowControlPoint = Pose(91.99073396778475, 83.2123552123552).mirrorIfBlue()
        firstRow = Pose(144 - 6 - ROBOT_LENGTH / 2 - ROBOT_FRONT_PROTRUSION, 83.51).mirrorIfBlue()
        secondRowControlPoint = Pose(83.14671814671814, 54.622779922779934).mirrorIfBlue()
        secondRow = Pose(144 - ROBOT_LENGTH / 2 - ROBOT_FRONT_PROTRUSION - 6, 54.7 + 2.0).mirrorIfBlue()
        thirdRow = Pose(98.30656370656376, 35.80328185328186).mirrorIfBlue()
        thirdRowControlPoint = Pose(83.03667953667953, 35.43976833976834).mirrorIfBlue()
        thirdRowEndPoint = Pose(144 - ROBOT_LENGTH / 2 - ROBOT_FRONT_PROTRUSION - 6, 35.43976833976834).mirrorIfBlue()

        endLocation = Pose(100.0, 53.0).mirrorIfBlue()
        innerEndLocation = Pose(86.0, 132.0).mirrorIfBlue()
        val pickupHeading = if (alliance == Alliance.RED) 0.0 else Math.toRadians(180.0)


        goalLocation = Pose(138.0, 144.0).mirrorIfBlue()
        aimHeading = calculateAimHeading(shootPosition, goalLocation)

        fromStartToShoot = follower.pathBuilder().addPath(
            BezierLine(startPosition, shootPosition)
        ).build()

        pickUpFirstRow = follower.pathBuilder()
            .addPath(BezierCurve(shootPosition, firstRowControlPoint, firstRow))
            .setConstantHeadingInterpolation(pickupHeading)
            .setReversed()
            .addPoseCallback(Pose(101.749, 82.334).mirrorIfBlue(), {
                follower.setMaxPower(AutoConstants.MAX_POWER_WHEN_RUNNING_INTAKE)
            }, 0.5)
            .build()

        pickUpFirstRowWithGate = follower.pathBuilder()
            .addPath(BezierCurve(shootPosition, firstRowControlPoint, firstRow))
            .setConstantHeadingInterpolation(pickupHeading)
            .setReversed()
            .addPoseCallback(Pose(101.749, 82.334 - 12.0).mirrorIfBlue(), {
                follower.setMaxPower(AutoConstants.MAX_POWER_WHEN_RUNNING_INTAKE)
            }, 0.5)
            .build()

        firstRowToShoot = follower.pathBuilder()
            .addPath(BezierLine(firstRow, shootPosition))
            .setLinearHeadingInterpolation(pickupHeading, aimHeading)
            .build()

        pickUpSecondRow = follower.pathBuilder()
            .addPath(BezierCurve(shootPosition, secondRowControlPoint, secondRow))
            .setConstantHeadingInterpolation(pickupHeading)
            .setReversed()
            .addPoseCallback(Pose(97.773, 60.0).mirrorIfBlue(), {
                follower.setMaxPower(AutoConstants.MAX_POWER_WHEN_RUNNING_INTAKE)
            }, 0.5)
            .build()

        secondRowToShoot = follower.pathBuilder()
            .addPath(BezierCurve(secondRow, secondRowControlPoint, shootPosition))
            .setLinearHeadingInterpolation(pickupHeading, aimHeading)
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
        const val ROBOT_LENGTH = 17.055;
        const val ROBOT_WIDTH = 16.850394;
        const val ROBOT_FRONT_PROTRUSION = 9.0;
        val RED_GOAL_FRONT = Pose(130.0, 130.0)
        val BLUE_GOAL_FRONT = Pose(14.0, 130.0)
    }
}