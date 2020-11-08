package ca.mcgill.ecse211.project;

import java.nio.file.Path;
import java.nio.file.Paths;
import simlejos.hardware.motor.Motor;
import simlejos.hardware.port.SensorPort;
import simlejos.hardware.sensor.EV3ColorSensor;
import simlejos.hardware.sensor.EV3UltrasonicSensor;
import simlejos.robotics.RegulatedMotor;
import simlejos.robotics.SampleProvider;

/**
 * Class for static resources (things that stay the same throughout the entire program execution),
 * like constants and hardware.
 * <br><br>
 * Use these resources in other files by adding this line at the top (see examples):<br><br>
 * 
 * {@code import static ca.mcgill.ecse211.project.Resources.*;}
 */
public class Resources {
  
  /** The time between physics steps in milliseconds. */
  public static final int PHYSICS_STEP_PERIOD = 500; // ms
  
  /** The path of the input vector file, relative to controllers/DpmProject. */
  public static final Path VECTORS_FILE = Paths.get("vectors1.txt");
  
  // Adjust the following parameters based on your robot
  
  /** The maximum distance detected by the ultrasonic sensor, in cm. */
  public static final int MAX_SENSOR_DIST = 255;
  
  /** The limit of invalid samples that we read from the US sensor before assuming no obstacle. */
  public static final int INVALID_SAMPLE_LIMIT = 20;
  
  /** The wheel radius in meters. */
  public static final double WHEEL_RAD = 0.021;
  /** Scaling factor for rotation in place. */
  public static final double SCALING = 0.983533;
  /** True circumference based on true radius. */ 
  public static final double WHEEL_CIRC = 2.00 * Math.PI * WHEEL_RAD;
  
  /** The robot width in meters. */
  //From the middle of the left wheel to the middle of the right wheel
  public static final double BASE_WIDTH = 0.160; // true value: (0.05566 + 0.022) * 2 = 0.15532
  
  /** The distance between the color sensors and the wheels in feet (Y AXIS). */
//  public static final double COLOR_SENSOR_TO_WHEEL_DIST = 0.125;
  
  /** The speed at which the robot moves forward in degrees per second. */
  public static final int FORWARD_SPEED = 300;
  
  /** The speed at which the robot rotates in degrees per second. */
  public static final int ROTATE_SPEED = 100;
  
  /** The motor acceleration in degrees per second squared. */
  public static final int ACCELERATION = 3000;
  
  /** The tile size in meters. Note that 0.3048 m = 1 ft. */
  public static final double TILE_SIZE = 0.3048;
  
  /** Timeout period in milliseconds. */
  public static final int TIMEOUT_PERIOD = 650;
  
  /** The odometer. */
  public static Odometer odometer = Odometer.getOdometer();
  
  // Hardware resources

  /** The left motor. */
  public static final RegulatedMotor leftMotor = Motor.A;
  
  /** The right motor. */
  public static final RegulatedMotor rightMotor = Motor.D;
  
  /** The Motor that turn the Ultrasonic Sensor. */
  public static final RegulatedMotor mediumMotor = Motor.C;
  
  /** The ultrasonic sensor. */
  public static final EV3UltrasonicSensor usSensor = new EV3UltrasonicSensor(SensorPort.S1);
  
  /** The left color sensor. */
  public static final SampleProvider leftColorSensor =
      new EV3ColorSensor(SensorPort.S2).getRedMode();
  /** The right color sensor. */
  public static final SampleProvider rightColorSensor =
      new EV3ColorSensor(SensorPort.S3).getRedMode();
  
  //Threshold for detecting the black gridlines with a differential filter
  public static final double BlackLine = 3600; 
  
}
