package org.firstinspires.ftc.teamcodeb.functions;

import android.graphics.Canvas;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.VisionProcessor;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import java.util.ArrayList;
import java.util.List;
import org.opencv.core.MatOfPoint2f;

public class BallDistance {

    private double distanceInches = 0;
    private double centerX = 160;
    private double centerY = 120;
    private boolean ballFound = false;

    private double focalLength = 640.0;
    private double ballRealSize = 5.0;

    private VisionPortal portal;

    public BallDistance(WebcamName webcam, double ballDiameter) {
        this.ballRealSize = ballDiameter;

        portal = new VisionPortal.Builder()
                .setCamera(webcam)
                .addProcessor(new SimpleVisionProcessor())
                .setStreamFormat(VisionPortal.StreamFormat.YUY2)
                .build();
    }

    public void calibrate(double knownDistance, double pixelSize) {
        focalLength = (pixelSize * knownDistance) / ballRealSize;
    }

    public double getDistance() {
        return distanceInches;
    }

    public double getX() {
        return centerX;
    }

    public double getY() {
        return centerY;
    }

    public boolean isBallFound() {
        return ballFound;
    }

    public void close() {
        if (portal != null) {
            portal.close();
        }
    }

    private class SimpleVisionProcessor implements VisionProcessor {
        private Mat hsv = new Mat();
        private Mat mask = new Mat();

        @Override
        public void init(int width, int height, org.firstinspires.ftc.robotcore.internal.camera.calibration.CameraCalibration calibration) {
        }

        @Override
        public Object processFrame(Mat frame, long captureTimeNanos) {
            Imgproc.cvtColor(frame, hsv, Imgproc.COLOR_RGB2HSV);

            Scalar lower = new Scalar(10, 150, 100);
            Scalar upper = new Scalar(25, 255, 255);
            Core.inRange(hsv, lower, upper, mask);

            List<org.opencv.core.MatOfPoint> contours = new ArrayList<>();
            Mat hierarchy = new Mat();
            Imgproc.findContours(mask, contours, hierarchy,
                    Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            ballFound = false;
            double maxArea = 0;
            org.opencv.core.MatOfPoint largest = null;

            for (org.opencv.core.MatOfPoint contour : contours) {
                double area = Imgproc.contourArea(contour);
                if (area > maxArea && area > 50) {
                    maxArea = area;
                    largest = contour;
                }
            }

            if (largest != null) {
                Point center = new Point();
                float[] radius = new float[1];

                MatOfPoint2f contour2f = new MatOfPoint2f(largest.toArray());
                Imgproc.minEnclosingCircle(contour2f, center, radius);

                centerX = center.x;
                centerY = center.y;
                double pixelDiameter = radius[0] * 2;

                if (pixelDiameter > 5) {
                    distanceInches = (ballRealSize * focalLength) / pixelDiameter;
                    ballFound = true;
                }
            } else {
                distanceInches = 0;
            }

            return frame;
        }

        @Override
        public void onDrawFrame(Canvas canvas, int onscreenWidth, int onscreenHeight, float scaleBmpPxToCanvasPx, float scaleCanvasDensity, Object userContext) {

        }


        public void onDrawFrame(Mat canvas, int onscreenWidth, int onscreenHeight,
                                float scaleBmpPxToCanvasPx, float scaleCanvasDensity,
                                Object userContext) {
        }
    }
}