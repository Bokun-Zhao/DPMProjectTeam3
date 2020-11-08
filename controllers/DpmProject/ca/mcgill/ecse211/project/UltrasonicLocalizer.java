package ca.mcgill.ecse211.project;

import static ca.mcgill.ecse211.project.Resources.BASE_WIDTH;
import static ca.mcgill.ecse211.project.Resources.INVALID_SAMPLE_LIMIT;
import static ca.mcgill.ecse211.project.Resources.MAX_SENSOR_DIST;
import static ca.mcgill.ecse211.project.Resources.ROTATE_SPEED;
import static ca.mcgill.ecse211.project.Resources.TIMEOUT_PERIOD;
import static ca.mcgill.ecse211.project.Resources.WHEEL_CIRC;
import static ca.mcgill.ecse211.project.Resources.leftMotor;
import static ca.mcgill.ecse211.project.Resources.odometer;
import static ca.mcgill.ecse211.project.Resources.rightMotor;
import static ca.mcgill.ecse211.project.Resources.usSensor;
import static simlejos.ExecutionController.sleepFor;

import java.util.Arrays;

public class UltrasonicLocalizer {

  /** The distance remembered by the filter() method. */
  private static int prevDistance;
  /** The current detected distance.  */
  private static int dist;
  /** The last detected distance. */
  private static int prevDist;
  /** The number of invalid samples seen by filter() so far. */
  private static int invalidSampleCount;
  /** The threshold for facing away from wall */
  private static int OPEN_SPACE = 100;
  /** Noise Margin = d+/- y. */
  private static int d = 50;
  private static int y = 10;
  /** angles when entering noise Margin. */
  private static double a0 = -1d;
  private static double b0 = -1d;
  /** angles when leaving noise Margin. */
  private static double a1 = -1d;
  private static double b1 = -1d;
  /** alphas and betas for localizing. */
  private static double alpha = -1d;
  private static double beta = -1d;
  // These arrays are used to avoid creating new ones at each iteration.
  /** Buffer (array) to store US samples. */
  private static float[] usData = new float[usSensor.sampleSize()];
  private static int[] window = new int[9];

  /**
   * An exclusive localizer just for this corner.
   * @author bokunzhao
   */
  public static void localize() {
    //To do
    boolean noiseMargin = false;

    // initialization, turn away from the walls if needed
    waltz();
    turnAwayFromWall();

    // after resetting theta, start rotating again
    waltz(360, true);
    //populate inital window array
    for (int i = 0; i < window.length; i++) {
      window[i] = readUsDistance();
    }
    // now we expect leftwall detection followed by backwall detection
    while (true) {
      // move window by one step (one new sample)
      moveWindow();
      // approaching left wall
      if (a0 == -1d && !noiseMargin && prevDist > d + y && dist <= d + y) {
        // entering left wall noise margin
        noiseMargin = true;
        a0 = odometer.getXyt()[2];
        System.out.println("located a0: " + a0);
      } else if (a1 == -1d && a0 != -1d && noiseMargin && prevDist > d - y && dist <= d - y) {
        // leaving left wall noise Margin
        a1 = odometer.getXyt()[2];
        noiseMargin = false;
        alpha = (a0 + a1) / 2d;
        System.out.println("located a1 and calculated alpha: " + a1);
      } else if (b0 == -1d && a1 != -1d && !noiseMargin && prevDist < d - y && dist >= d - y) {
        // entering back wall noise margin
        noiseMargin = true;
        b0 = odometer.getXyt()[2];
        System.out.println("located b0: " + b0);
      } else if (b1 == -1d && b0 != -1d && noiseMargin && prevDist < d + y && dist >= d + y) {
        //leaving back wall noise margin
        b1 = odometer.getXyt()[2];
        noiseMargin = false;
        beta = (b0 + b1) / 2d;
        System.out.println("located b1 and calculated beta: " + b1);
        break;
      }
      sleepFor(TIMEOUT_PERIOD);
    }

    leftMotor.stop();
    rightMotor.stop();
    // robot should now facing beta

    System.out.println("correcting...");
    // calculate offset and rotate to the offset angle
    if (alpha < beta) {
      waltz(45d - (alpha - beta) / 2d, false);
    } else {
      waltz(225d - (alpha - beta) / 2d, false);
    }
    
    // calibrate odometer
    odometer.setXyt(0.3048, 0.3048, 0);
  }

  /** Returns the filtered distance between the US sensor and an obstacle in cm. */
  public static int readUsDistance() {
    usSensor.fetchSample(usData, 0);
    // extract from buffer, cast to int, and filter
    return filter((int) (usData[0] * 100.0));
  }

  /**
   * Rudimentary filter - toss out invalid samples corresponding to null signal.
   * 
   * @param distance raw distance measured by the sensor in cm
   * @return the filtered distance in cm
   */
  static int filter(int distance) {
    if (distance >= MAX_SENSOR_DIST && invalidSampleCount < INVALID_SAMPLE_LIMIT) {
      // bad value, increment the filter value and return the distance remembered from before
      invalidSampleCount++;
      return prevDistance;
    } else {
      if (distance < MAX_SENSOR_DIST) {
        invalidSampleCount = 0; // reset filter and remember the input distance.
      }
      prevDistance = distance;
      return distance;
    }
  }

  /**
   * rotate the robot in place at predefined pace,
   * in clockwise direction if no arguments.
   * @author bokunzhao
   */
  public static void waltz() {
    //test code written by bokun: self rotation
    leftMotor.setSpeed(ROTATE_SPEED);
    rightMotor.setSpeed(ROTATE_SPEED);
    leftMotor.forward();
    rightMotor.backward();
  }

  /**
   * Overrided waltz() method based on turnBy().
   * @param degree the degree to turn by
   * @param immeRet to signify immediate return.
   * @author bokunzhao
   */
  public static void waltz(double degree, boolean immeRet) {
    leftMotor.setSpeed(ROTATE_SPEED);
    rightMotor.setSpeed(ROTATE_SPEED);
    leftMotor.rotate((int) -convertAngle(degree), true);
    rightMotor.rotate((int) convertAngle(degree), immeRet);
  }
  
  /**
   * Method to guarantee starting condition for US localizer,
   * no matter how the robot is placed, let it rotate away from the wall.
   * @author bokunzhao
   */
  private static void turnAwayFromWall() {
    while (true) {
      if (readUsDistance() > OPEN_SPACE) {
        leftMotor.stop();
        rightMotor.stop();
        // re-calibrate theta, offset will be added to this angle
        odometer.setTheta(0);
        System.out.println("starting localization...");
        prevDist = 255;
        dist = prevDist;
        break;
      }
    }
  }
  
  /**
   * Method to advance median window by one sample
   * and update optimized distance.
   * @author bokunzhao
   */
  private static void moveWindow() {
    // sort the window array
    int[] windowInstance = window.clone();
    Arrays.sort(windowInstance);
    // use median to update distance readings
    prevDist = dist;
    dist = windowInstance[windowInstance.length / 2];
    // shift window by 1 sample, newest sample at the end 
    for (int i = 0; i < window.length - 1; i++) {
      window[i] = window[i + 1];
    }
    window[window.length - 1] = readUsDistance();
    // for testing purpose:
    //System.out.println(Arrays.toString(windowInstance) + " median: " + dist);
  }

  /**
   * Converts input distance to the total rotation of each wheel needed to cover that distance.
   * 
   * @param distance the input distance in meters
   * @return the wheel rotations necessary to cover the distance
   * @author bokunzhao
   */
  public static double convertDistance(double distance) {
    // Compute and return the correct value (in degrees)
    return (360.0 * distance / (WHEEL_CIRC));
  }

  /**
   * Converts input angle to the total rotation of each wheel needed to rotate the robot by that
   * angle.
   * 
   * @param angle the input angle in degrees
   * @return the wheel rotations necessary to rotate the robot by the angle
   * @author bokunzhao
   */
  public static double convertAngle(double angle) {
    // Reuse convertDistance() for calculating correct angle
    return convertDistance(Math.PI * BASE_WIDTH * angle / 360.0);
  }
}
