package org.firstinspires.ftc.teamcodeb;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.geometry.Pose;

@Configurable
public class OpModeConstants {

    public static double PID_P = 300;
    public static double PID_I = 0;
    public static double PID_D = 0;
    public static double PID_F = 10;

    public static double FEED_TIME_SECONDS = 0.20;
    public static double STOP_SPEED = 0.0;
    public static double FULL_SPEED = 1.0;
    public static double FEED_TIME = 0.3;

    public static double LAUNCHER_TARGET_VELOCITY = 1300;
    public static double LAUNCHER_MIN_VELOCITY = 1200;

    public static double INTAKE_TARGET_VELOCITY =100;
    public static double INTAKE_MIN_VELOCITY =50;
    public static double TIME_BETWEEN_SHOTS = 2;

    public static double DRIVE_SPEED = 0.5;
    public static double ROTATE_SPEED = 0.2;
    public static double WHEEL_DIAMETER_MM = 96;
    public static double ENCODER_TICKS_PER_REV = 537.7;
    public static double TICKS_PER_MM =
            (ENCODER_TICKS_PER_REV / (WHEEL_DIAMETER_MM * Math.PI));
    public static double TRACK_WIDTH_MM = 404;

    public static final Pose START_P1 =
            new Pose(122.238, 121.003, Math.toRadians(45));

    public static final Pose START_P2 =
            new Pose(122.238, 121.003, Math.toRadians(45));

    public static final Pose TARGET_P1 =
            new Pose(122.238, 121.003, Math.toRadians(45));

    public static final Pose TARGET_P2 =
            new Pose(122.238, 121.003, Math.toRadians(45));

    public static final int DESIRED_TAG_ID = -1;
    public static final boolean USE_WEBCAM = true;
}