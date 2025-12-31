package org.firstinspires.ftc.teamcodeb.opmode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;


public class ColorDistanceTest extends LinearOpMode {

    private NormalizedColorSensor colorSensor;
    private DistanceSensor distanceSensor;

    @Override
    public void runOpMode() {
        colorSensor = hardwareMap.get(NormalizedColorSensor.class, "color_sensor");
        distanceSensor = hardwareMap.get(DistanceSensor.class, "distance_sensor");

        waitForStart();

        while (opModeIsActive()) {
            NormalizedRGBA colors = colorSensor.getNormalizedColors();
            double distance = distanceSensor.getDistance(DistanceUnit.INCH);

            int red = (int)(colors.red * 255);
            int green = (int)(colors.green * 255);
            int blue = (int)(colors.blue * 255);

            telemetry.clear();
            telemetry.addData("Distance", "%.1f", distance);
            telemetry.addData("Red", "%d", red);
            telemetry.addData("Green", "%d", green);
            telemetry.addData("Blue", "%d", blue);

            if (isPurple(red, green, blue)) {
                telemetry.addData("Color", "PURPLE");
            } else if (isGreen(red, green, blue)) {
                telemetry.addData("Color", "GREEN");
            } else {
                telemetry.addData("Color", "NONE");
            }

            telemetry.update();
            sleep(100);
        }
    }

    private boolean isPurple(int red, int green, int blue) {
        return red > 100 && blue > 100 && green < red && green < blue;

    }

    private boolean isGreen(int red, int green, int blue) {
        return green > 100 && green > red && green > blue;
    }
}