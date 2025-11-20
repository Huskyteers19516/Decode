package org.firstinspires.ftc.teamcodea;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.Pose;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

@Configurable
public class OpModeConstants {
    public static double PID_P = 300;
    public static double PID_I = 0;
    public static double PID_D = 0;
    public static double PID_F = 10;

    public static double FEED_TIME_SECONDS = 0.20; //The feeder servos run this long when a shot is requested.
    public static double STOP_SPEED = 0.0; //We send this power to the servos when we want them to stop.
    public static double FULL_SPEED = 1.0;
    public static double FEED_TIME = 0.30; //The feeder servos run this long when a shot is requested.

    /*
     * When we control our launcher motor, we are using encoders. These allow the control system
     * to read the current speed of the motor and apply more or less power to keep it at a constant
     * velocity. Here we are setting the target and minimum velocity that the launcher should run
     * at. The minimum velocity is a threshold for determining when to fire.
     */
    public static double LAUNCHER_TARGET_VELOCITY = 1300;
    public static double LAUNCHER_MIN_VELOCITY = 1200;

    /*
     * The number of seconds that we wait between each of our 3 shots from the launcher. This
     * can be much shorter, but the longer break is reasonable since it maximizes the likelihood
     * that each shot will score.
     */
    public static double TIME_BETWEEN_SHOTS = 2;

    /*
     * Here we capture a few variables used in driving the robot. DRIVE_SPEED and ROTATE_SPEED
     * are from 0-1, with 1 being full speed. Encoder ticks per revolution is specific to the motor
     * ratio that we use in the kit; if you're using a different motor, this value can be found on
     * the product page for the motor you're using.
     * Track width is the distance between the center of the drive wheels on either side of the
     * robot. Track width is used to determine the amount of linear distance each wheel needs to
     * travel to create a specified rotation of the robot.
     */
    public static double DRIVE_SPEED = 0.5;
    public static double ROTATE_SPEED = 0.2;
    public static double WHEEL_DIAMETER_MM = 96;
    public static double ENCODER_TICKS_PER_REV = 537.7;
    public static double TICKS_PER_MM = (ENCODER_TICKS_PER_REV / (WHEEL_DIAMETER_MM * Math.PI));
    public static double TRACK_WIDTH_MM = 404;
    public static final Pose START_P1=
            new Pose(122.238, 121.003, Math.toRadians(45));

    public static final Pose START_P2 =
            new Pose(122.238, 121.003, Math.toRadians(45));



    private static final int DESIRED_TAG_ID = -1;
    private static final boolean USE_WEBCAM = true;



}