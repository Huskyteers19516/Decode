package org.firstinspires.ftc.teamcodeb.functions;

import static org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion.hardwareMap;

import com.pedropathing.follower.Follower;

import org.firstinspires.ftc.teamcodeb.pedroPathing.Constants;

public class CalculateBallLocation {
private Follower follower;
public void init(){
        follower = Constants.createFollower(hardwareMap);
        double x = follower.getPose().getX();
        double y = follower.getPose().getY();
        double heading = follower.getHeading();
}


}
