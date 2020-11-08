package ca.mcgill.ecse211.project;

import static ca.mcgill.ecse211.project.Resources.ACCELERATION;
import static ca.mcgill.ecse211.project.Resources.BASE_WIDTH;
import static ca.mcgill.ecse211.project.Resources.ROTATE_SPEED;
import static ca.mcgill.ecse211.project.Resources.SCALING;
import static ca.mcgill.ecse211.project.Resources.TILE_SIZE;
import static ca.mcgill.ecse211.project.Resources.WHEEL_CIRC;
import static ca.mcgill.ecse211.project.Resources.leftColorSensor;
import static ca.mcgill.ecse211.project.Resources.leftMotor;
import static ca.mcgill.ecse211.project.Resources.odometer;
import static ca.mcgill.ecse211.project.Resources.rightColorSensor;
import static ca.mcgill.ecse211.project.Resources.rightMotor;
import static simlejos.ExecutionController.sleepFor;

import ca.mcgill.ecse211.project.FindAndPushBox.PushDirection;


public class LightLocalizer {
  /** Arrays to store left and right color sensor sample. */
  private static float[] leftSensorData = new float [leftColorSensor.sampleSize()];
  private static float[] rightSensorData = new float [rightColorSensor.sampleSize()];
  
  /** Mean value calculated from window. */
  private static double rightVal;
  private static double leftVal;

  /** Distance between the color sensors and the middle of the robot, in feet. */
  private static final double ALIGN_WHEELS_WITH_LINE_DISTANCE = 0.145; // = 0.0350 meters

  /** Threshold to distinguish tiles from lines. */
  private static final double LINE_THRESHOLD = 100.0;
  /** Window size. */
  private static final int window_size = 5;

  /**
   * Light Localization method to use light sensors to navigate to (1,1) and orient to 0 degrees.
   * @author arneetkalra
   */
  public static void localize() {
    setup();

    updateColorReadings();  
    System.out.println("Starting Light Localization...");

    forwardMotors();

    while (!onABlackLine()) {
      updateColorReadings();
      sleepFor(500);
    }
    print("Horizontal black Line Detected! - 1");

    // Fix robot orientation if needed
    correctRobotPosition();

    // Align wheel axis with black line
    setup();
    moveStraightFor(ALIGN_WHEELS_WITH_LINE_DISTANCE);

    // Turn 90 degrees towards (1,1)
    setSpeed(ROTATE_SPEED);
    turnBy(90.0);

    // Move towards (1,1)
    setSpeed(ROTATE_SPEED);
    forwardMotors();
    updateColorReadings();

    while (!onABlackLine()) {
      updateColorReadings();
      sleepFor(500);
    }
    print("Vertical black Line Detected! - 2");

    // Fix robot orientation if needed
    correctRobotPosition();

    //Align wheels axis with black line
    setup();
    moveStraightFor(ALIGN_WHEELS_WITH_LINE_DISTANCE);

    // Orient back to 0 degrees
    stopMotors();
    setSpeed(ROTATE_SPEED);
    turnBy(-90.0);

    stopMotors();
    print("Light Localization Complete!");
  }
  
  /**
   * Was suppose to backup AND do correction based on the push direction.
   * Correction was not successfully implemented.
   * @param pd the push direction of the box
   * @author bokunzhao
   */
  public static void correct(PushDirection pd) {
    setup();

    updateColorReadings();  
    System.out.println("Starting Light Correction...");
    
    moveBackwardsFor(1);
    return;
  }
  

  /**
   * Initial method to setup motors.
   * @author arneetkalra
   */
  public static void setup() {
    stopMotors();
    setSpeed(ROTATE_SPEED);
    setAcceleration(ACCELERATION);
  }

  public static boolean onABlackLine() {
    return (rightVal < LINE_THRESHOLD || leftVal < LINE_THRESHOLD);
  }

  /**
   * Moves the robot straight for the given distance.
   * 
   * @param distance in feet (tile sizes), may be negative
   * @author arneetkalra
   */
  public static void moveStraightFor(double distance) {
    // Pass meters to convertDistance
    leftMotor.rotate(convertDistance(distance * TILE_SIZE), true);
    rightMotor.rotate(convertDistance(distance * TILE_SIZE), false);
  }

  /**
   * Corrects the trajectory of the robot if one motor is ahead of the other.
   * @author arneetkalra
   */
  public static void correctRobotPosition() {
    stopMotors();
    if (leftVal <= LINE_THRESHOLD && rightVal <= LINE_THRESHOLD) {
      return;
    } else if (leftVal > LINE_THRESHOLD) { //Right side is ahead
      leftMotor.setSpeed(ROTATE_SPEED);
      leftMotor.forward();
      System.out.print("Right Side Ahead... Correcting. . .");
    } else if (rightVal > LINE_THRESHOLD) {
      rightMotor.setSpeed(ROTATE_SPEED);
      rightMotor.forward();
      System.out.print("Left Side Ahead... Correcting. . .");
    }
    while (leftVal > LINE_THRESHOLD || rightVal > LINE_THRESHOLD) {
      updateColorReadings();
      if (leftVal <= LINE_THRESHOLD && rightVal <= LINE_THRESHOLD) {
        break;
      }
    }
    stopMotors(); 
    System.out.println(" . . . Now in Sync");
  }

  /**
   * Overrided method used during box pushing.
   * @param isNavigation to indicate this is correction used during navigation
   */
  public static void correctRobotPosition(boolean isNavigation) {
    stopMotors();
    if (leftVal <= LINE_THRESHOLD && rightVal <= LINE_THRESHOLD) {
      return;
    } else if (leftVal > LINE_THRESHOLD) { //Right side is ahead
      leftMotor.setSpeed(ROTATE_SPEED);
      leftMotor.backward();
      System.out.print("Right Side Ahead... Correcting. . .");
    } else if (rightVal > LINE_THRESHOLD) {
      rightMotor.setSpeed(ROTATE_SPEED);
      rightMotor.backward();
      System.out.print("Left Side Ahead... Correcting. . .");
    }
    while (leftVal > LINE_THRESHOLD || rightVal > LINE_THRESHOLD) {
      updateColorReadings();
      if (leftVal <= LINE_THRESHOLD && rightVal <= LINE_THRESHOLD) {
        break;
      }
    }
    stopMotors(); 
    System.out.println(" . . . Now in Sync");
  }

  /**
   * Moves the robot backwards for the given distance.
   * 
   * @param distance in feet (tile sizes), may be negative
   * @author arneetkalra
   */
  public static void moveBackwardsFor(double distance) {
    // Pass meters to convertDistance
    leftMotor.rotate(-convertDistance(distance * TILE_SIZE), true);
    rightMotor.rotate(-convertDistance(distance * TILE_SIZE), false);
  }


  /**
   * Simply prints the sensor values.
   * @author arneetkalra
   */
  public static void printSensorValue(double left, double right) {
    System.out.println("Left: " + left + ", Right:" + right);
  }

  /**
   * Prints a line of text. Looks better than default Java syntax.
   * @param text to be printed
   */
  public static void print(String text) {
    System.out.println(text);
  }


  /**
   * Update the value intensity of the line for each sensor.
   * @author arneetkalra
   */
  public static void updateColorReadings() {
    //Get Left Sensor data
    double[] leftValues = new double[window_size];
    for (int i = 0; i < 5; i++) {
      leftColorSensor.fetchSample(leftSensorData, 0);
      leftValues[i] = (double) leftSensorData[0];
    }

    //Get Right Sensor Data
    double[] rightValues = new double[5];
    for (int i = 0; i < 5; i++) {
      rightColorSensor.fetchSample(rightSensorData, 0);
      rightValues[i] = (double) rightSensorData[0];
    }

    //Update Values by getting mean of array
    leftVal = getAverage(leftValues);
    rightVal = getAverage(rightValues);
  }

  /**
   * Helper method that calculates the average from an array of doubles.
   * @param data array
   * @return average value
   */
  public static double getAverage(double[] data) {
    double sum = 0;
    for (int i = 0; i < data.length; i++) {
      sum += data[i];
    }
    return (sum / (double) (data.length));
  }

  /**
   * Stops both motors.
   */
  public static void stopMotors() {
    leftMotor.stop();
    rightMotor.stop();
  }

  /**
   * Stops both motors.
   */
  public static void forwardMotors() {
    leftMotor.forward();
    rightMotor.forward();
  }
  
  /**
   * Used in correction.
   */
  public static void backwardMotors() {
    leftMotor.backward();
    rightMotor.backward();
  }


  /**
   * Sets the speed of both motors to the same values.
   * 
   * @param speed the speed in degrees per second
   * @author arneetkalra
   */
  public static void setSpeed(int speed) {
    leftMotor.setSpeed(speed);
    rightMotor.setSpeed(speed);
  }

  /**
   * Sets the acceleration of both motors.
   * 
   * @param acceleration the acceleration in degrees per second squared
   * @author arneetkalra
   */
  public static void setAcceleration(int acceleration) {
    leftMotor.setAcceleration(acceleration);
    rightMotor.setAcceleration(acceleration);
  }


  /**
   * Converts input distance to the total rotation of each wheel needed to cover that distance.
   * 
   * @param distance the input distance in meters
   * @return the wheel rotations necessary to cover the distance
   * @author arneetkalra
   */
  public static int convertDistance(double distance) {
    // Compute and return the correct value (in degrees)
    return (int) (360.0 * distance / WHEEL_CIRC * SCALING);
  }

  /**
   * Converts input angle to the total rotation of each wheel needed to rotate the robot by that
   * angle.
   * 
   * @param angle the input angle in degrees
   * @return the wheel rotations necessary to rotate the robot by the angle
   * @author arneetkalra
   */
  public static int convertAngle(double angle) {
    // Reuse convertDistance() for calculating correct angle
    return convertDistance(Math.PI * BASE_WIDTH * angle / 360.0);
  }

  /**
   * Turns the robot by a specified angle. Note that this method is different from
   * {@code Navigation.turnTo()}. For example, if the robot is facing 90 degrees, calling
   * {@code turnBy(90)} will make the robot turn to 180 degrees, but calling
   * {@code Navigation.turnTo(90)} should do nothing (since the robot is already at 90 degrees).
   * 
   * @param angle the angle by which to turn, in degrees
   * @author arneetkalra
   */
  public static void turnBy(double angle) {
    // Similar to moveStraightFor(), but use a minus sign
    leftMotor.rotate(convertAngle(angle), true);
    rightMotor.rotate(-convertAngle(angle), false);
  }

}
