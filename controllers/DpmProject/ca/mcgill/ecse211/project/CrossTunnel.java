package ca.mcgill.ecse211.project;

import static ca.mcgill.ecse211.project.Navigation.distanceBetween;
import static ca.mcgill.ecse211.project.Navigation.travelTo;
import static ca.mcgill.ecse211.project.Resources.TILE_SIZE;
import static ca.mcgill.ecse211.project.Resources.odometer;
import static ca.mcgill.ecse211.project.Resources.FORWARD_SPEED;

import ca.mcgill.ecse211.playingfield.Point;

public class CrossTunnel {

  // Tunnel coordinate for sample map red zone
  public static final Point TNL_LL = new Point(10, 3);
  public static final Point TNL_UR = new Point(11, 5);
  
  private static boolean isHorizontalTunnel;
  
  public static void alignAndCross(Point tnl_ll, Point tnl_ur) {
    double tunnelY = (tnl_ll.y + tnl_ur.y) / 2d;
    double tunnelX = (tnl_ll.x + tnl_ur.x) / 2d;
    Point tunnelCenter = new Point(tunnelX, tunnelY);
    
    if (Math.abs(tnl_ll.x - tnl_ur.x) == 2) { // horizontal tunnel
      isHorizontalTunnel = true;
      // align with tunnel
      System.out.println("Aligning with tunnel...");
      Navigation.travelTo(new Point(odometer.getXyt()[0]/TILE_SIZE, tunnelY));
      // face the tunnel
      System.out.println("facing the tunnel...");
      Navigation.turnTo(tunnelCenter);

    } else if (Math.abs(tnl_ll.y - tnl_ur.y) == 2) { // vertical tunnel
      isHorizontalTunnel = false;
      // align with tunnel
      System.out.println("Aligning with tunnel...");
      Navigation.travelTo(new Point(tunnelX, odometer.getXyt()[1]/TILE_SIZE));
      // face the tunnel
      System.out.println("facing the tunnel...");
      Navigation.turnTo(tunnelCenter);
    }
    
    
    LightLocalizer.setSpeed(FORWARD_SPEED);
    LightLocalizer.forwardMotors();
    
    
    while(true){
      // Get current robot location
      double[] currentOdo = odometer.getXyt();
      Point robot = new Point(currentOdo[0] / TILE_SIZE, currentOdo[1] / TILE_SIZE);
      if(Navigation.distanceBetween(robot, tunnelCenter) < 2.5 * TILE_SIZE) {
        break;
      }
      
    }
    
    //prepare to correct
    LightLocalizer.correct();
    
    //now pass the tunnel, odo is not accurate here so don't use travelTo()
    LightLocalizer.setSpeed(FORWARD_SPEED);
    LightLocalizer.moveStraightFor(3.7);
    
    // localize again
    LightLocalizer.localize(); // improve this and below, if tunnel at the edge
    // Get current robot location
    double[] currentOdo = odometer.getXyt();
    Point robot = new Point(currentOdo[0] / TILE_SIZE, currentOdo[1] / TILE_SIZE);
    
    if (isHorizontalTunnel) {
      if (Navigation.distanceBetween(robot, tnl_ll) > Navigation.distanceBetween(robot, tnl_ur)) { // was traveling from west to east
        odometer.setXyt((tnl_ur.x + 1d) * TILE_SIZE, (tnl_ur.y - 1d) * TILE_SIZE, 90d);
        odometer.printPositionInTileLengths();
      } else { // was traveling from east to west
        odometer.setXyt((tnl_ll.x - 1d) * TILE_SIZE, (tnl_ll.y + 1d) * TILE_SIZE, 270d);
        odometer.printPositionInTileLengths();
      }
    } else { // vertical tunnel
      if (Navigation.distanceBetween(robot, tnl_ll) > Navigation.distanceBetween(robot, tnl_ur)) { // was traveling from south to north
        odometer.setXyt((tnl_ur.x) * TILE_SIZE, (tnl_ur.y + 1d) * TILE_SIZE, 0d);
        odometer.printPositionInTileLengths();
      } else { // was traveling from north to south
        odometer.setXyt((tnl_ll.x) * TILE_SIZE, (tnl_ll.y - 1d) * TILE_SIZE, 180d);
        odometer.printPositionInTileLengths();
      }
      
    }
  }

}
