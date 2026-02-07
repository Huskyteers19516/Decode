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
}