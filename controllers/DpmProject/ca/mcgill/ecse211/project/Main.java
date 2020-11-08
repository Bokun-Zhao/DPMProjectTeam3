package ca.mcgill.ecse211.project;

import static ca.mcgill.ecse211.project.Resources.FORWARD_SPEED;
import static ca.mcgill.ecse211.project.Resources.ROTATE_SPEED;
import static ca.mcgill.ecse211.project.Resources.PHYSICS_STEP_PERIOD;
import static ca.mcgill.ecse211.project.Resources.VECTORS_FILE;
import static ca.mcgill.ecse211.project.Resources.leftMotor;
import static ca.mcgill.ecse211.project.Resources.odometer;
import static ca.mcgill.ecse211.project.Resources.rightMotor;
import static simlejos.ExecutionController.performPhysicsStep;
import static simlejos.ExecutionController.setNumberOfParties;
import static simlejos.ExecutionController.sleepFor;
import static simlejos.ExecutionController.waitUntilNextStep;

import ca.mcgill.ecse211.playingfield.Point;
import simlejos.hardware.ev3.LocalEV3;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Main class of the program.
 */
public class Main {

  /**
   * The number of threads used in the program (main, odometer), other than the one used to
   * perform physics steps. It is possible to do this lab with 2 threads, but you can add more.
   */
  public static final int NUMBER_OF_THREADS = 2;

  public static int boxnumber = 0;

  /**
   * The start and end points for the blocks, read from the vectors file path defined in Resources.
   * Each vector entry has a number and a point array, where the first point is the vector head and
   * the second is its tail. To access these properties in your code, see the main method.
   * 
   * <p>Example: Block number 1 -> [(1, 3), (1, 5)]
   */
  public static Map<Integer, Point[]> vectors;

  /** Main entry point. */
  public static void main(String[] args) {
    initialize();

    // Start the odometer thread
    new Thread(odometer).start();


    // Localize in the corner like in the previous lab
    UltrasonicLocalizer.localize();
//    leftMotor.setSpeed(ROTATE_SPEED);
//    rightMotor.setSpeed(ROTATE_SPEED);
//    LightLocalizer.turnBy(720);
    pause();
//    leftMotor.setSpeed(ROTATE_SPEED);
//    rightMotor.setSpeed(ROTATE_SPEED);
//    LightLocalizer.turnBy(-360);
    LightLocalizer.localize();
    
    /* TODO logic to implement:
     * 1. if starting_zone_LL == (0, 0), setOdo (0.3048, 0.3048, 0) (i.e. (1, 1, 0))
     * 2. else if starting_zone_UR == (15, 9), setOdo (4.2672, 2.4384, 180) (i.e. (14, 8, 0))
     * 3. else: calculate UL and LR
     * 4. check UL and LR, similar to 1. and 2. 
     */
    odometer.setXyt(0.3048, 2.4384, 90); // red zone:(1, 8, facing east)
    
    // beep three times
    LocalEV3.audio.beep();
    sleepFor(20000);
    LocalEV3.audio.beep();
    sleepFor(20000);
    LocalEV3.audio.beep();
    pause();
    
    odometer.printPositionInTileLengths();

    CrossTunnel.alignAndCross(CrossTunnel.TNL_LL, CrossTunnel.TNL_UR);
    /* lab 5 stuffs, not used in project

    // Iterate over the vectors like this (both of these ways work, choose one):
    for (var vector: vectors.entrySet()) {
      int number = vector.getKey();
      Point head = vector.getValue()[0];
      Point tail = vector.getValue()[1];
      // main method for pushing blocks
      boxnumber++;
      FindAndPushBox.navigate(head, tail);

      System.out.println("Move block " + number + " from " + head + " to " + tail + ".");
    }

    odometer.printPositionInTileLengths();
    // to counter backup of last block as it is not needed
    LightLocalizer.moveStraightFor(1d);

    // Determine which block number to print here based on the measurements taken above
    System.out.println("The heaviest block is..." + heaviestBox());
    
    */

  }

  private static String heaviestBox() {
    if (FindAndPushBox.firstBox > FindAndPushBox.secondBox
        && FindAndPushBox.firstBox > FindAndPushBox.thirdBox) {
      return "first box";
    } else if (FindAndPushBox.secondBox > FindAndPushBox.firstBox
        && FindAndPushBox.secondBox > FindAndPushBox.thirdBox) {
      return "second box";
    } else if (FindAndPushBox.thirdBox > FindAndPushBox.secondBox
        && FindAndPushBox.thirdBox > FindAndPushBox.firstBox) {
      return "third box";
    }
    return null;
  }

  /**
   * Initializes the robot logic. It starts a new thread to perform physics steps regularly.
   */
  private static void initialize() {
    try {
      vectors = parseBlockVectors(Files.readAllLines(VECTORS_FILE));
    } catch (IOException e) {
      System.err.println("Could not open file: " + VECTORS_FILE);
      System.exit(-1);
    }

    // Run a few physics steps to make sure everything is initialized and has settled properly
    for (int i = 0; i < 50; i++) {
      performPhysicsStep();
    }

    // We are going to start two threads, so the total number of parties is 2
    setNumberOfParties(NUMBER_OF_THREADS);

    // Does not count as a thread because it is only for physics steps
    new Thread(() -> {
      while (performPhysicsStep()) {
        sleepFor(PHYSICS_STEP_PERIOD);
      }
    }).start();
  }

  /** Parses input lines into block vectors. */
  public static Map<Integer, Point[]> parseBlockVectors(List<String> lines) {
    var result = new HashMap<Integer, Point[]>();
    lines.forEach(line -> {
      if (!line.startsWith("#")) { // line is not a comment
        var n = Arrays.stream(line.split(" ")).map(Integer::parseInt).toArray(Integer[]::new);
        result.put(n[0], new Point[] {new Point(n[1], n[2]), new Point(n[3], n[4])});
      }
    });
    return result;
  }

  /**
   * Halts the robot for a while to allow pausing the simulation to evaluate ultrasonic
   * localization.
   */
  private static void pause() {
    System.out.println("Localization completed. Pause simulation now!");
    leftMotor.setSpeed(0);
    rightMotor.setSpeed(0);

    for (int i = 0; i < 10000; i++) {
      waitUntilNextStep();
    }

    leftMotor.setSpeed(FORWARD_SPEED);
    rightMotor.setSpeed(FORWARD_SPEED);
  }

}
