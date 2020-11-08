package ca.mcgill.ecse211.project;

import static ca.mcgill.ecse211.project.Navigation.distanceBetween;
import static ca.mcgill.ecse211.project.Navigation.travelTo;
import static ca.mcgill.ecse211.project.Resources.TILE_SIZE;
import static ca.mcgill.ecse211.project.Resources.odometer;

import ca.mcgill.ecse211.playingfield.Point;


public class FindAndPushBox {

  public static enum PushDirection { 
    E, S, W, N 
  }

  private static Point pushPos;
  private static Point wayPointA1;
  private static Point wayPointA2;
  private static Point wayPointB1;
  private static Point wayPointB2;
  public static boolean isPushing = false;
  public static double boxTorque = 0;
  public static double firstBox = 0;
  public static double secondBox = 0;
  public static double thirdBox = 0;

  static final double wayPointOffset = 0.75;
  private static final double boxOffset = 0.5;

  /**
   * Main method for reaching and pushing boxes.
   * @param box the box position
   * @param dest where the box should be
   * @author bokunzhao
   */
  public static void navigate(Point box, Point dest) {
    // Get current robot location
    double[] currentOdo = odometer.getXyt();
    Point robot = new Point(currentOdo[0] / TILE_SIZE, currentOdo[1] / TILE_SIZE);

    // Get the vecter component (for lab 5, one of the following is zero)
    double vecX = dest.x - box.x;
    double vecY = dest.y - box.y;

    // Determine which side of the box should the robot get to
    pushPos = new Point(0, 0);
    if (vecX > 0) {
      // Push east, get to the west side of the box
      pushPos.x = box.x - wayPointOffset;
      pushPos.y = box.y;
      // Determine if pushPos is closer than the box
      if (distanceBetween(robot, pushPos) < distanceBetween(robot, box)) {
        // Can get to pushPos directly
        travelTo(pushPos);

        // Then push the box
        dest.x -= boxOffset; // take account of box size
        isPushing = true;
        travelTo(dest);
        isPushing = false;
        recordBoxTorque();
      } else {
        // Need intermediate way points
        wayPointA1 = new Point(pushPos.x, pushPos.y + wayPointOffset);
        wayPointA2 = new Point(pushPos.x, pushPos.y - wayPointOffset);
        wayPointB1 = new Point(pushPos.x + wayPointOffset, pushPos.y + wayPointOffset);
        wayPointB2 = new Point(pushPos.x + wayPointOffset, pushPos.y - wayPointOffset);
        if (distanceBetween(robot, wayPointA1) < distanceBetween(robot, wayPointA2)) {
          // Closer to A1
          travelTo(wayPointB1);
          travelTo(wayPointA1);
        } else {
          travelTo(wayPointB2);
          travelTo(wayPointA2);
        }
        travelTo(pushPos);
        dest.x -= boxOffset; // take account of box size
        isPushing = true;
        travelTo(dest);
        isPushing = false;
        recordBoxTorque();
      }

      //TODO Backup a bit
      // as well as Correction (unused for lab 5)
      LightLocalizer.correct(PushDirection.E);

    } else if (vecX < 0) {
      // Push west, get to the east side of the box
      pushPos.x = box.x + wayPointOffset;
      pushPos.y = box.y;
      // Determine if pushPos is closer than the box
      if (distanceBetween(robot, pushPos) < distanceBetween(robot, box)) {
        // Can get to pushPos directly
        travelTo(pushPos);
        // Then push the box
        dest.x += boxOffset; // take account of box size
        isPushing = true;
        travelTo(dest);
        isPushing = false;
        recordBoxTorque();
      } else {
        // Need intermediate way points
        wayPointA1 = new Point(pushPos.x, pushPos.y + wayPointOffset);
        wayPointA2 = new Point(pushPos.x, pushPos.y - wayPointOffset);
        wayPointB1 = new Point(pushPos.x - wayPointOffset, pushPos.y + wayPointOffset);
        wayPointB2 = new Point(pushPos.x - wayPointOffset, pushPos.y - wayPointOffset);
        if (distanceBetween(robot, wayPointA1) < distanceBetween(robot, wayPointA2)) {
          // Closer to A1
          travelTo(wayPointB1);
          travelTo(wayPointA1);
        } else {
          travelTo(wayPointB2);
          travelTo(wayPointA2);
        }
        travelTo(pushPos);
        dest.x += boxOffset; // take account of box size
        isPushing = true;
        travelTo(dest);
        isPushing = false;
        recordBoxTorque();
      }

      // Backup a bit
      // as well as Correction (unused for lab 5)
      LightLocalizer.correct(PushDirection.W);
    } else if (vecY > 0) {
      // Push north, get to the south side of the box
      pushPos.x = box.x;
      pushPos.y = box.y - wayPointOffset;
      // Determine if pushPos is closer than the box
      if (distanceBetween(robot, pushPos) < distanceBetween(robot, box)) {
        // Can get to pushPos directly
        travelTo(pushPos);
        // Then push the box
        dest.y -= boxOffset; // take account of box size
        isPushing = true;
        travelTo(dest);
        isPushing = false;
        recordBoxTorque();
      } else {
        // Need intermediate way points
        wayPointA1 = new Point(pushPos.x + wayPointOffset, pushPos.y);
        wayPointA2 = new Point(pushPos.x - wayPointOffset, pushPos.y);
        wayPointB1 = new Point(pushPos.x + wayPointOffset, pushPos.y + wayPointOffset);
        wayPointB2 = new Point(pushPos.x - wayPointOffset, pushPos.y + wayPointOffset);
        if (distanceBetween(robot, wayPointA1) < distanceBetween(robot, wayPointA2)) {
          // Closer to A1
          travelTo(wayPointB1);
          travelTo(wayPointA1);
        } else {
          travelTo(wayPointB2);
          travelTo(wayPointA2);
        }
        travelTo(pushPos);
        dest.y -= boxOffset; // take account of box size
        isPushing = true;
        travelTo(dest);
        isPushing = false;
        recordBoxTorque();
      }
      // Backup a bit
      // as well as Correction (unused for lab 5)
      LightLocalizer.correct(PushDirection.N);
    } else { //if (vecY < 0)
      // Push south, get to the north side of the box
      pushPos.x = box.x;
      pushPos.y = box.y + wayPointOffset;
      // Determine if pushPos is closer than the box
      if (distanceBetween(robot, pushPos) < distanceBetween(robot, box)) {
        // Can get to pushPos directly
        travelTo(pushPos);
        // Then push the box
        dest.y += boxOffset; // take account of box size
        isPushing = true;
        travelTo(dest);
        isPushing = false;
        recordBoxTorque();
      } else {
        // Need intermediate way points
        wayPointA1 = new Point(pushPos.x + wayPointOffset, pushPos.y);
        wayPointA2 = new Point(pushPos.x - wayPointOffset, pushPos.y);
        wayPointB1 = new Point(pushPos.x + wayPointOffset, pushPos.y - wayPointOffset);
        wayPointB2 = new Point(pushPos.x - wayPointOffset, pushPos.y - wayPointOffset);
        if (distanceBetween(robot, wayPointA1) < distanceBetween(robot, wayPointA2)) {
          // Closer to A1
          travelTo(wayPointB1);
          travelTo(wayPointA1);
        } else {
          travelTo(wayPointB2);
          travelTo(wayPointA2);
        }
        travelTo(pushPos);
        dest.y += boxOffset; // take account of box size
        isPushing = true;
        travelTo(dest);
        isPushing = false;
        recordBoxTorque();
      }
      // Backup a bit
      // as well as Correction (unused for lab 5)
      LightLocalizer.correct(PushDirection.S);
    }

  }

  /**
   * Record the torque for the corresponding box.
   * @author bokunzhao
   */
  private static void recordBoxTorque() {
    int range = odometer.collectedTorque.size();
    int start = (range / 4) * 3;
    int end = range - 1;
    for (int i = start; i < end; i++) {
      boxTorque += odometer.collectedTorque.get(i);
    }
    odometer.collectedTorque.clear();
    boxTorque = boxTorque / (range / 4);
    switch (Main.boxnumber) {
      case 1: firstBox = boxTorque;
        System.out.println(firstBox);
      break;
      case 2: secondBox = boxTorque;
        System.out.println(secondBox);
      break;
      case 3: thirdBox = boxTorque;
        System.out.println(thirdBox);
      break;
      default: break;
    }
    boxTorque = 0;
    System.out.println("boxTorque reset to 0");

  }

}
