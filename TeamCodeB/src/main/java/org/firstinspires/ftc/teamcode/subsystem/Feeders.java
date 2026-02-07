package org.firstinspires.ftc.teamcodec.subsystem;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.seattlesolvers.solverslib.command.SubsystemBase;

public class Feeders extends SubsystemBase {
    private final Servo feederA;
    private final Servo feederB;
    private final Servo feederC;

    public enum Feeder {
        A, B, C
    }

    public Feeders(final HardwareMap hMap) {
        feederA = hMap.get(Servo.class, "feederA");
        feederB = hMap.get(Servo.class, "feederB");
        feederC = hMap.get(Servo.class, "feederC");
        lowerFeederA();
        lowerFeederB();
        lowerFeederC();
    }

    public void raiseFeeder(Feeder feeder) {
        switch (feeder) {
            case A:
                raiseFeederA();
                break;
            case B:
                raiseFeederB();
                break;
            case C:
                raiseFeederC();
                break;
        }
    }

    public void lowerFeeder(Feeder feeder) {
        switch (feeder) {
            case A:
                lowerFeederA();
                break;
            case B:
                lowerFeederB();
                break;
            case C:
                lowerFeederC();
                break;
        }
    }

    public void raiseFeederA() {
        feederA.setPosition(.79);
    }

    public void lowerFeederA() {
        feederA.setPosition(.34);
    }

    public void raiseFeederB() {
        feederB.setPosition(1.0);
    }
    public void lowerFeederB() {
        feederB.setPosition(0.35);
    }
    public void raiseFeederC() {
        feederC.setPosition(0);
    }
    public void lowerFeederC() {
        feederC.setPosition(.7);
    }
}
