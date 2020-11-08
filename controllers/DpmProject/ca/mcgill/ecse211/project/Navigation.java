package ca.mcgill.ecse211.project;

import static ca.mcgill.ecse211.project.Resources.BASE_WIDTH;
import static ca.mcgill.ecse211.project.Resources.FORWARD_SPEED;
import static ca.mcgill.ecse211.project.Resources.ROTATE_SPEED;
import static ca.mcgill.ecse211.project.Resources.TILE_SIZE;
import static ca.mcgill.ecse211.project.Resources.WHEEL_CIRC;
import static ca.mcgill.ecse211.project.Resources.leftMotor;
import static ca.mcgill.ecse211.project.Resources.odometer;
import static ca.mcgill.ecse211.project.Resources.rightMotor;

import ca.mcgill.ecse211.playingfield.Point;

/**
 * The Navigation class is used to make the robot navigate around the playing field.
 */
public class Navigation {

  /** Do not instantiate this class. */
  private Navigation() {}

  /** Travels to the given destination.
   * 
   * @param destination the destination waypoint
   * @author bokunzhao
   */
  public static void travelTo(Point destination) {
    // Think carefully about how you would integrate line detection here, if necessary
    double[] currentPosition = odometer.getXyt();
    double currentX = currentPosition[0] / TILE_SIZE;
    double currentY = currentPosition[1] / TILE_SIZE;
    Point current = new Point(currentX, currentY);
    double destAngle = getDestinationAngle(current, destination);
    turnTo(destAngle);
    // distance needed to travel, in meters
    double dist = distanceBetween(current, destination);
    leftMotor.setSpeed(FORWARD_SPEED);
    rightMotor.setSpeed(FORWARD_SPEED);
    leftMotor.rotate(convertDistance(dist), true);
    rightMotor.rotate(convertDistance(dist), false);
    // Don't forget that destination.x and destination.y are in feet, not meters
  }
  
  /** turn to face the given destination.
   * 
   * @param destination the destination waypoint
   * @author bokunzhao
   */
  public static void turnTo(Point destination) {
    // Think carefully about how you would integrate line detection here, if necessary
    double[] currentPosition = odometer.getXyt();
    double currentX = currentPosition[0] / TILE_SIZE;
    double currentY = currentPosition[1] / TILE_SIZE;
    Point current = new Point(currentX, currentY);
    double destAngle = getDestinationAngle(current, destination);
    turnTo(destAngle);
  }

  /**
   * Turns the robot with a minimal angle towards the given input angle in degrees, no matter what
   * its current orientation is. This method is different from {@code turnBy()}.
   * @param angle the angle to turn to
   * @author bokunzhao
   */
  public static void turnTo(double angle) {
    // Hint: You can do this in one line by reusing some helper methods declared in this class
    turnBy(minimalAngle(odometer.getXyt()[2], angle), false);
  }

  /** Returns the angle that the robot should point towards to face the destination in degrees.
   * 
   * @param current current location of the robot
   * @param destination destination location
   * @return double angle the angle to face to, in degree, north is 0, west is 270
   * @author filip
   * @author bokunzhao 
   */
  public static double getDestinationAngle(Point current, Point destination) {
    // perfect cases
    if (destination.x == current.x) {
      if (destination.y > current.y) {
        return 0d;
      } else if (destination.y < current.y) {
        return 180d;
      } else {
        return 0d;
      }
    }
    
    if (destination.y == current.y) {
      if (destination.x > current.x) {
        return 90d;
      } else if (destination.x < current.x) {
        return 270d;
      } else {
        return 0d;
      }
    }
    // not so perfect cases
    if (destination.x > current.x && destination.y > current.y) {
      // first quadrant
      return Math.atan((destination.x - current.x) / (destination.y - current.y)) * 180 / Math.PI;
    } else if (destination.x < current.x && destination.y > current.y) {
      // second quadrant
      return Math.atan((destination.x - current.x) / (destination.y - current.y))
          * 180 / Math.PI + 360d;
    } else if (destination.x < current.x && destination.y < current.y) {
      // third quadrant
      return Math.atan((destination.x - current.x) / (destination.y - current.y))
          * 180 / Math.PI + 180d;
    } else if (destination.x > current.x && destination.y < current.y) {
      // fourth quadrant
      return Math.atan((destination.x - current.x) / (destination.y - current.y))
          * 180 / Math.PI + 180d;
    } else {
      // Not likely to happen...
      return 0d;
    }
  }

  /** Returns the signed minimal angle in degrees from initial angle to destination angle (deg).
   * 
   * @param initialAngle the facing angle
   * @param destAngle the angle to turn to
   * @return double the signed angle to turn by
   * @author bokunzhao
   * 
   *      Tested.
   */
  public static double minimalAngle(double initialAngle, double destAngle) {
    // ideal case
    if (initialAngle - destAngle == 0d || initialAngle - destAngle == 360d) {
      return 0d;
    }
    // common cases
    if (initialAngle > destAngle) {
      double diff = initialAngle - destAngle;
      if (diff <= 180) {
        return -diff;
      } else {
        return 360 - diff;
      }
    } else if (initialAngle < destAngle) {
      double diff = destAngle - initialAngle;
      if (diff <= 180) {
        return diff;
      } else {
        return diff - 360;
      }
    }
    return destAngle;
  }

  /** Returns the distance between the two points in tile lengths (feet).
   * 
   * @param p1 the starting point
   * @param p2 the destination point
   * @return double
   * @author bokunzhao
   */
  public static double distanceBetween(Point p1, Point p2) {
    // distance needed to travel, in meters
    double dist = TILE_SIZE * Math.sqrt(Math.pow((p1.x - p2.x), 2) 
        + Math.pow((p1.y - p2.y), 2));
    return dist;
  }

  // TODO Bring Navigation-related helper methods from Labs 2 and 3 here
  // You can also add other helper methods here, but remember to document them with Javadoc (/**)!

  /**
   * Converts input distance to the total rotation of each wheel needed to cover that distance
   * This is for moving forward, as no scaling factor is applied.
   * 
   * @param distance the input distance in meters
   * @return the wheel rotations necessary to cover the distance
   * @author bokunzhao
   */
  public static int convertDistance(double distance) {
    // Compute and return the correct value (in degrees)
    return (int) (360.0 * distance / WHEEL_CIRC);
  }


  /**
   * Overrided turnBy().
   * @param degree the degree to turn by
   * @param immeRet to signify immediate return.
   * @author bokunzhao
   */
  public static void turnBy(double degree, boolean immeRet) {
    leftMotor.setSpeed(ROTATE_SPEED);
    rightMotor.setSpeed(ROTATE_SPEED);
    leftMotor.rotate((int) convertAngle(degree), true);
    rightMotor.rotate((int) -convertAngle(degree), immeRet);
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
