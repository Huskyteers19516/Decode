# Big Mac Bundle Testing - Control Map



## 1) Test Selection Stage (before running any test)

| Controller | Action |
|---|---|
| `dpad_up` | Select previous test mode |
| `dpad_down` | Select next test mode |
| `A` | Confirm selection and start the selected test |

## 2) Global While Running a Test

| Controller | Action |
|---|---|
| `BACK` | Return to test selection stage |

## 3) DRIVE Mode

| Controller | Action |
|---|---|
| `START` | Toggle Drive sub-mode (Individual motor test <-> normal drive control) |
| `X` | Toggle Drive sub-mode (same as `START`) |
| `dpad_up` | Reset drive orientation |
| `dpad_down` | Toggle Field-centric <-> Robot-centric |
| `left_stick_y` | Drive forward/backward (normal drive sub-mode) |
| `left_stick_x` | Strafe left/right (normal drive sub-mode) |
| `right_stick_x` | Rotate (normal drive sub-mode) |
| `X` | Front-left motor ON in Individual sub-mode |
| `Y` | Front-right motor ON in Individual sub-mode |
| `A` | Rear-left motor ON in Individual sub-mode |
| `B` | Rear-right motor ON in Individual sub-mode |

## 4) SHOOTER Mode

| Controller | Action |
|---|---|
| `START` | Toggle Shooter control mode (Velocity <-> Manual power) |
| `dpad_up` | Increase shooter target velocity (big step) |
| `dpad_down` | Decrease shooter target velocity (big step) |
| `dpad_left` | Decrease shooter target velocity (small step) |
| `dpad_right` | Increase shooter target velocity (small step) |
| `Y` | Toggle shooter on/off |
| `right_stick_y` | Manual shooter power input (Manual mode) |

## 5) SYSTEM Mode

| Controller | Action |
|---|---|
| `START` | Toggle outtake control mode (Velocity <-> Manual power) |
| `dpad_up` | Increase outtake target velocity (big step) |
| `dpad_down` | Decrease outtake target velocity (big step) |
| `dpad_left` | Decrease outtake target velocity (small step) |
| `dpad_right` | Increase outtake target velocity (small step) |
| `Y` | Toggle outtake on/off |
| `right_stick_y` | Manual outtake power input (Manual mode) |
| `gamepad2 left_stick_y` | Intake manual control |
| `A` (hold) | Raise Flipper A |
| `A` (release) | Lower Flipper A |
| `B` (hold) | Raise Flipper B |
| `B` (release) | Lower Flipper B |
| `X` (hold) | Raise Flipper C |
| `X` (release) | Lower Flipper C |

## 6) TURRET Mode

| Controller | Action |
|---|---|
| `right_stick_x` | Manual turret movement |
| `A` | Move turret +500 ticks |
| `B` | Move turret -500 ticks |
| `Y` | Reset turret encoder |

## 7) COLOR_SENSOR Mode

| Controller | Action |
|---|---|
| No dedicated button controls | Telemetry/debug display only |

## 8) CAMERA Mode

| Controller | Action |
|---|---|
| `A` | Find BLUE tag target and orient drivetrain toward it |

## 9) LAUNCHER Mode

| Controller | Action |
|---|---|
| `left_bumper` | Increase target launcher RPM |
| `right_bumper` | Decrease target launcher RPM |
| `A` | Increase hood target angle |
| `B` | Decrease hood target angle |
| `X` | Decrease hood minimum servo calibration |
| `Y` | Increase hood minimum servo calibration |
| `dpad_left` | Decrease hood maximum servo calibration |
| `dpad_right` | Increase hood maximum servo calibration |
