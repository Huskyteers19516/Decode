package org.firstinspires.ftc.teamcodeb.functions;

import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

public class FeederSequence {
    private Servo feederA;
    private Servo feederB;
    private Servo feederC;
    private ElapsedTime timer = new ElapsedTime();

    private enum Step {
        IDLE,
        A_UP, A_DOWN,
        B_UP, B_DOWN,
        C_UP, C_DOWN,
        FINISHED
    }

    private Step currentStep = Step.IDLE;

    private final double TARGET_POS = 0.5;
    private final double RESET_POS = 0.0;
    private final double DWELL_TIME = 0.3; // 舵机从0到0.5停留的时间

    public FeederSequence(Servo a, Servo b, Servo c) {
        this.feederA = a;
        this.feederB = b;
        this.feederC = c;
    }

    public void trigger() {
        if (currentStep == Step.IDLE || currentStep == Step.FINISHED) {
            currentStep = Step.A_UP;
            timer.reset();
        }
    }

    public boolean isBusy() {
        return currentStep != Step.IDLE && currentStep != Step.FINISHED;
    }

    public void update() {
        switch (currentStep) {
            case A_UP:
                feederA.setPosition(TARGET_POS);
                if (timer.seconds() > DWELL_TIME) {
                    currentStep = Step.A_DOWN;
                    timer.reset();
                }
                break;
            case A_DOWN:
                feederA.setPosition(RESET_POS);
                if (timer.seconds() > DWELL_TIME) {
                    currentStep = Step.B_UP;
                    timer.reset();
                }
                break;

            case B_UP:
                feederB.setPosition(TARGET_POS);
                if (timer.seconds() > DWELL_TIME) {
                    currentStep = Step.B_DOWN;
                    timer.reset();
                }
                break;
            case B_DOWN:
                feederB.setPosition(RESET_POS);
                if (timer.seconds() > DWELL_TIME) {
                    currentStep = Step.C_UP;
                    timer.reset();
                }
                break;

            case C_UP:
                feederC.setPosition(TARGET_POS);
                if (timer.seconds() > DWELL_TIME) {
                    currentStep = Step.C_DOWN;
                    timer.reset();
                }
                break;
            case C_DOWN:
                feederC.setPosition(RESET_POS);
                if (timer.seconds() > DWELL_TIME) {
                    currentStep = Step.FINISHED;
                }
                break;

            case FINISHED:
                currentStep = Step.IDLE;
                break;

            default:
                break;
        }
    }

    public void resetSequence() {
        feederA.setPosition(RESET_POS);
        feederB.setPosition(RESET_POS);
        feederC.setPosition(RESET_POS);
        currentStep = Step.IDLE;
    }

    public void doubleCheckOff() {
        feederA.setPosition(RESET_POS);
        feederB.setPosition(RESET_POS);
        feederC.setPosition(RESET_POS);
        currentStep = Step.IDLE;
    }
}