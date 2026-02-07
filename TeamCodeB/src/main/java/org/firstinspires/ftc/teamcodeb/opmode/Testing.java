package org.firstinspires.ftc.teamcodeb.opmode;

import static org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion.gamepad1;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

@Autonomous(name = "auto for no intake ", group = "Bot2")
public class Testing {

    private DcMotorEx spinner;
    private DcMotorEx outtake;

    public enum LaunchState {
        IDLE,
        SPIN_UP,
        LAUNCH,
        LAUNCHING
    }

    private LaunchState launchState = LaunchState.IDLE;



    public void init(HardwareMap hardwareMap) {
        spinner = hardwareMap.get(DcMotorEx.class, "spinner");
        outtake = hardwareMap.get(DcMotorEx.class, "launcher");
    }

    public void loop(){
        if(gamepad1.a){
            spinner.setVelocity(1);

        }
        if(gamepad1.b){
            spinner.setVelocity(0);
        }
        if(gamepad1.left_bumper){
            spinner.setVelocity(spinner.getVelocity()-0.1);
        }
        if(gamepad1.x){
            outtake.setVelocity(1);
        }
    }

    }
