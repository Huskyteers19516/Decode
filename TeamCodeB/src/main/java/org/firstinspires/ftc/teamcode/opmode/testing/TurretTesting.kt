package org.firstinspires.ftc.teamcode.opmode.testing



import com.bylazar.telemetry.PanelsTelemetry
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.exec
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.loop
import dev.frozenmilk.dairy.mercurial.ftc.Mercurial
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotor
import com.huskyteers19516.shared.hl

@Suppress("unused")
val turretTesting = Mercurial.teleop("Turret Ticks Test", "Testing") {
    val telemetryM = PanelsTelemetry.telemetry
    val turretMotor = hardwareMap.get(DcMotorEx::class.java, "turretMotor")


    turretMotor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
    turretMotor.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
    turretMotor.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE

    waitForStart()

    var targetTicks = 0
    var isAutoMove = false


    bindExec(risingEdge { gamepad1.y }, exec {
        turretMotor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        turretMotor.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        targetTicks = 0
    })


    bindExec(risingEdge { gamepad1.a }, exec {
        targetTicks += 500
        turretMotor.targetPosition = targetTicks
        turretMotor.mode = DcMotor.RunMode.RUN_TO_POSITION
        turretMotor.power = 0.5
        isAutoMove = true
    })


    bindExec(risingEdge { gamepad1.b }, exec {
        targetTicks -= 500
        turretMotor.targetPosition = targetTicks
        turretMotor.mode = DcMotor.RunMode.RUN_TO_POSITION
        turretMotor.power = 0.5
        isAutoMove = true
    })

    schedule(
        loop(
            exec {
                val currentTicks = turretMotor.currentPosition

                telemetryM.addLine("=== Turret Ticks Calibration ===")
                telemetryM.addLine("Use Right Stick X for Manual Move")
                telemetryM.addLine("Press A/B to step 500 ticks")
                telemetryM.addLine("Press Y to reset encoder")
                telemetryM.hl()


                val manualPower = -gamepad1.right_stick_x.toDouble()
                if (Math.abs(manualPower) > 0.1) {
                    isAutoMove = false
                    turretMotor.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
                    turretMotor.power = manualPower * 0.6
                } else if (!isAutoMove) {
                    turretMotor.power = 0.0
                }


                val guessedTicksPerDegree = 537.7 / 360.0
                val estimatedDegrees = currentTicks / guessedTicksPerDegree

                telemetryM.addData("Current Ticks", currentTicks)
                telemetryM.addData("Estimated Degrees", "%.2f°".format(estimatedDegrees))
                telemetryM.addData("Motor Mode", turretMotor.mode)
                telemetryM.hl()

                telemetryM.addLine("Calibration Tip:")
                telemetryM.addLine("1. Manually rotate turret exactly 1 full turn")
                telemetryM.addLine("2. Record 'Current Ticks' value")
                telemetryM.addLine("3. TicksPerDegree = Value / 360.0")

                telemetryM.update(telemetry)
            }
        )
    )

    dropToScheduler()
}