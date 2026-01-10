package org.firstinspires.ftc.teamcodec.config;

import com.bylazar.configurables.annotations.Configurable;

@Configurable
public class OuttakeConstants {
    public static double kp = 50;
    public static double ki = 1500;
    public static double kd = 0;
    public static double ks = 0;
    public static double kv = 0;

    public static double defaultTargetVelocity = 0.6;
    public static double allowance = 50;
}