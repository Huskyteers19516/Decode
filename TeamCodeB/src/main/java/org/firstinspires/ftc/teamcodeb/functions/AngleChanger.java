package org.firstinspires.ftc.teamcodeb.functions;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import com.qualcomm.robotcore.util.Range;

public class AngleChanger {
    private double currentPos = 0.5;
    private static final double MIN_POS = 0.0;
    private static final double MAX_POS = 1.0;
    private  Servo AngleChanger;
    private ElapsedTime timer = new ElapsedTime();
    private final static double MOVE_SPEED= 0.5;

    public AngleChanger(Servo servo) {
        this.AngleChanger = servo;
    }

    public void setPosition(double pos) {
        AngleChanger.setPosition(Range.clip(pos, 0, 1));
    }
    public double getPosition() {
        return AngleChanger.getPosition();
    }
}
