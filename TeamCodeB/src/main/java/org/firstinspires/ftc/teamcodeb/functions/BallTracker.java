package org.firstinspires.ftc.teamcodeb.functions;

import android.graphics.Canvas;
import org.firstinspires.ftc.robotcore.internal.camera.calibration.CameraCalibration;
import org.firstinspires.ftc.vision.VisionProcessor;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import java.util.ArrayList;
import java.util.List;

public class BallTracker implements VisionProcessor {

    private volatile double distanceInches = 0;
    private volatile double offsetX = 0;
    private volatile boolean ballFound = false;

    private final double ballRealSize;
    private double focalLength = 640.0;
    private int frameWidth = 320;

    private Mat hsv = new Mat();
    private Mat mask = new Mat();

    public BallTracker(double ballDiameter) {
        this.ballRealSize = ballDiameter;
    }

    @Override
    public void init(int width, int height, CameraCalibration calibration) {
        this.frameWidth = width;
    }

    @Override
    public Object processFrame(Mat frame, long captureTimeNanos) {
        Imgproc.cvtColor(frame, hsv, Imgproc.COLOR_RGB2HSV);

        Scalar lower = new Scalar(5, 120, 80);
        Scalar upper = new Scalar(25, 255, 255);
        Core.inRange(hsv, lower, upper, mask);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        double maxArea = 0;
        MatOfPoint largestContour = null;

        for (MatOfPoint contour : contours) {
            double area = Imgproc.contourArea(contour);
            if (area > maxArea && area > 100) {
                maxArea = area;
                largestContour = contour;
            }
        }

        if (largestContour != null) {
            Point center = new Point();
            float[] radius = new float[1];
            MatOfPoint2f contour2f = new MatOfPoint2f(largestContour.toArray());
            Imgproc.minEnclosingCircle(contour2f, center, radius);

            offsetX = (center.x - (frameWidth / 2.0)) / (frameWidth / 2.0);
            double pixelDiameter = radius[0] * 2;

            if (pixelDiameter > 5) {
                distanceInches = (ballRealSize * focalLength) / pixelDiameter;
                ballFound = true;
            }
            Imgproc.circle(frame, center, (int)radius[0], new Scalar(0, 255, 0), 2);
        } else {
            ballFound = false;
            distanceInches = 0;
            offsetX = 0;
        }

        hierarchy.release();
        return null;
    }

    @Override
    public void onDrawFrame(Canvas canvas, int onscreenWidth, int onscreenHeight, float scaleBmpPxToCanvasPx, float scaleCanvasDensity, Object userContext) {}

    public boolean isBallFound() { return ballFound; }
    public double getDistance() { return distanceInches; }
    public double getXOffset() { return offsetX; }
}
