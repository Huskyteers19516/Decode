package org.firstinspires.ftc.teamcode.opmode

import android.util.Log
import com.bylazar.telemetry.PanelsTelemetry
import com.huskyteers19516.shared.Alliance
import com.huskyteers19516.shared.Motif
import com.huskyteers19516.shared.hl
import com.pedropathing.geometry.BezierLine
import com.pedropathing.geometry.Pose
import com.pedropathing.paths.HeadingInterpolator
import com.pedropathing.paths.PathChain
import com.pedropathing.paths.PathPoint
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.Servo
import dev.frozenmilk.dairy.mercurial.continuations.Closure
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.deadline
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.exec
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.loop
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.match
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.noop
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.sequence
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.wait
import dev.frozenmilk.dairy.mercurial.ftc.Mercurial
import org.firstinspires.ftc.teamcode.constants.AutoConstants
import org.firstinspires.ftc.teamcode.constants.OuttakeConstants
import org.firstinspires.ftc.teamcode.hardware.Drive
import org.firstinspires.ftc.teamcode.hardware.Intake
import org.firstinspires.ftc.teamcode.hardware.Outtake
import org.firstinspires.ftc.teamcode.opmode.pathpackage.AutoNumber1
import kotlin.math.abs

fun createDumb() = Mercurial.Program {
    //#region Pre-Init
    val telemetryM = PanelsTelemetry.telemetry;


    // Changing Auto route number when it is necessarily. Comment out old one and put new one in
    val outtake = hardwareMap.get(DcMotor::class.java, "outtake")
    val transfer = hardwareMap.get(DcMotor::class.java, "transfer")
    val blocker = hardwareMap.get(Servo::class.java, "blocker")

    waitForStart()
    schedule(
        loop(
            exec {
                outtake.direction = DcMotorSimple.Direction.REVERSE
                transfer.direction = DcMotorSimple.Direction.REVERSE

                transfer.power = 1.0
                outtake.power = 1.0
                blocker.position = if (gamepad1.a) OuttakeConstants.BLOCKER_OPEN_POSITION else OuttakeConstants.BLOCKER_CLOSED_POSITION
            }
        )
    )

    dropToScheduler()
}

val Dumb = Mercurial.teleop("Shoot Off", "Huskyteers", createDumb())
