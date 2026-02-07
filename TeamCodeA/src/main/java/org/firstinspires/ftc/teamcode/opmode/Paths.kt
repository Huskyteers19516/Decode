package org.firstinspires.ftc.teamcode.opmode

import com.huskyteers19516.shared.Alliance
import com.pedropathing.follower.Follower
import com.pedropathing.geometry.BezierLine
import com.pedropathing.geometry.Pose
import com.pedropathing.paths.PathChain
import kotlin.math.atan2
import kotlin.math.sqrt

class Paths(private val follower: Follower) {
    init {
        buildPaths(Alliance.RED)
    }

    lateinit var fromStartToShoot: PathChain
    lateinit var startPosition: Pose
    lateinit var shootPosition: Pose
    lateinit var endLocation: Pose
    lateinit var goalLocation: Pose
    var aimHeading = 0.0;


    fun buildPaths(alliance: Alliance) {
        fun Pose.mirrorIfBlue(): Pose {
            return if (alliance == Alliance.BLUE) this.mirror() else this
        }

        // Assume robot starts aligned with goal, facing obelisk wall, with line in the middle of the drive train
        // https://www.desmos.com/calculator/qgkrj2eng6
        startPosition = Pose(
            130.046 - ROBOT_WIDTH / sqrt(2.0) / 2,
            130.046 - ROBOT_WIDTH / sqrt(2.0) / 2,
            Math.toRadians(126.0)
        ).mirrorIfBlue()
        shootPosition = Pose(92.07041564792169, 131.0072460546789).mirrorIfBlue()
        endLocation = Pose(97.45454545454545, 96.65454545454544).mirrorIfBlue()


        goalLocation = Pose(138.0, 144.0).mirrorIfBlue()
        aimHeading = calculateAimHeading(shootPosition, goalLocation)

        fromStartToShoot = follower.pathBuilder().addPath(
            BezierLine(startPosition, shootPosition)
        ).setLinearHeadingInterpolation(startPosition.heading, aimHeading).build()
    }

    companion object {
        fun calculateAimHeading(robot: Pose, goal: Pose): Double {
            return atan2(goal.y - robot.y, goal.x - robot.x)
        }

        const val ROBOT_WIDTH = 16.299;
    }
}