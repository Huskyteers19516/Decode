package org.firstinspires.ftc.teamcode;

import com.bylazar.configurables.annotations.Configurable;

@Configurable
public class OpModeConstants {
    public static double PID_P = 300;
    public static double PID_I = 0;
    public static double PID_D = 0;
    public static double PID_F = 10;
    public static double FEED_TIME_SECONDS = 0.20; //The feeder servos run this long when a shot is requested.
    public static double STOP_SPEED = 0.0; //We send this power to the servos when we want them to stop.
    public static double FULL_SPEED = 1.0;
    public static double LAUNCHER_TARGET_VELOCITY = 1300;
    public static double LAUNCHER_MIN_VELOCITY = 1200;
    /*
     * The number of seconds that we wait between each of our 3 shots from the launcher. This
     * can be much shorter, but the longer break is reasonable since it maximizes the likelihood
     * that each shot will score.
     */
    public static double TIME_BETWEEN_SHOTS = 2;
}