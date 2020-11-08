package ca.mcgill.ecse211.test;

import static ca.mcgill.ecse211.project.Navigation.*;
import static ca.mcgill.ecse211.project.Resources.*;
import static org.junit.jupiter.api.Assertions.*;

import ca.mcgill.ecse211.playingfield.Point;
import org.junit.jupiter.api.Test;


/**
 * Tests the Navigation class. This test runs in Eclipse (right-click > Run as > Unit test) and
 * on the command line, not in Webots!
 * 
 * @author Younes Boubekeur
 */
public class TestNavigation {

  /** Tolerate up to this amount of error due to double imprecision. */
  private static final double ERROR_MARGIN = 0.01;

  @Test void testMinimalAngle() {
    // Going from 45° to 135° means turning by +90°
    assertEquals(90, minimalAngle(45, 135), ERROR_MARGIN);

    // Going from 185° to 175° means turning by -10°
    assertEquals(-10, minimalAngle(185, 175), ERROR_MARGIN);
    
    assertEquals(90, minimalAngle(360, 90), ERROR_MARGIN);
    
    assertEquals(-5, minimalAngle(0, 355), ERROR_MARGIN);
    assertEquals(180, minimalAngle(0, 180), ERROR_MARGIN);

    // TODO Add more test cases here. Don't forget about edge cases!
  }
  
  @Test void testGetDestinationAngle() {
    assertEquals(45, getDestinationAngle(new Point(10, 10), new Point(20, 20)));
    assertEquals(315, getDestinationAngle(new Point(10, 10), new Point(0, 20)));
    assertEquals(225, getDestinationAngle(new Point(10, 10), new Point(0, 0)));
    assertEquals(135, getDestinationAngle(new Point(10, 10), new Point(20, 0)));
    
  }

  // TODO Think about testing your other Navigation functions here

  // We can add helper methods below to be used in the tests above

}
