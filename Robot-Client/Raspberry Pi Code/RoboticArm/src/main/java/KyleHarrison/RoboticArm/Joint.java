package KyleHarrison.RoboticArm;
// Joint.java
// Andrew Davison, ad@fivedots.coe.psu.ac.th, June 2011

/* Class for rotating a joint in the robot arm

   Each joint instance maintains state information about its joint:
     * its current rotation angle (currAngle);
     * its rotation rate (rotRate), so a rotation 
       angle can be translated into a rotation time;
     * rotation limits (posLimit, negLimit) beyond which a rotation
       is impossible

    The state information is read from (and written to) a file called
    <JointID>JI.txt, which has the form:
          rotate:  <rotTime>
          limits: <posLimit> <negLimit>
          angle: <currAngle>

    The loaded rotation time is converted into a rotation rate.
*/

import java.io.*;
import ch.ntb.usb.*;


public class Joint
{
  private static final String JOINT_INFO_EXT = "JI.txt";

  private static final int MIN_TIME = 200;   // ms
             // don't move if time is less than MIN_TIME

  private JointID jointID;
  private ArmCommunicator armComms;

  // data read in from joint info file
  private int rotTime;          
            // time (in ms) for the joint to move between its limits
  private int posLimit, negLimit;   // in degrees    (negLimit is -ve)
  private int currAngle;            // may range between posLimit and negLimit 

  private float rotRate;      // joint rotation rate (between limits)



  public Joint(JointID id, ArmCommunicator ac)
  {  
    jointID = id;
    armComms = ac;

    readJointInfo(getName() + JOINT_INFO_EXT);
    checkInfo();
  }  // end of Joint()


  public String getName()
  {  return jointID.toString().toLowerCase(); }


  public int getCurrAngle()
  {  return currAngle; }


  public void saveState()
  {  writeJointInfo(getName() + JOINT_INFO_EXT);  }



  public void turnByOffset(int offsetAngle)
  /* turn by specfied offset, which may be
     positive or negative; +ve is forwards/right,
     -ve is backwards/left) */
  {
    int newAngle = withinLimits(currAngle + offsetAngle);
    timedAngleTurn(newAngle);
  }  // end of turnByOffset()



  private int withinLimits(int angle)
  // adjust angle if outside limits
  {
    if (angle >= posLimit) {
      System.out.println("  " + jointID + ": Angle (" + angle +
                                ") exceeds +ve limit; turning to limit only");
      angle = posLimit-1;
    }
    else if (angle <= negLimit) {
      System.out.println("  " + jointID + ": Angle (" + angle +
                                   ") exceeds -ve limit; turning to limit only");
      angle = negLimit+1;
    }
    return angle;
  }  // end of withinLimits()



  private void timedAngleTurn(int angle)
  // move to angle by executing a timed rotation
  {
    System.out.println("  " + jointID + " angle turn to: " + angle);
    int offsetAngle = angle - currAngle;     // offset may be +ve or -ve

    if (offsetAngle < 0) {
      int time = Math.round( -offsetAngle/rotRate );
      if (time < MIN_TIME)
        System.out.println("  " + jointID + ": -ve turn time too short (" +
                             time + "); ignoring");
      else {
        armComms.turn(jointID, ArmCommunicator.NEGATIVE, time);
        currAngle = angle;
      }
    }
    else {    // offset is +ve
      int time = Math.round( offsetAngle/rotRate );
      if (time < MIN_TIME)
        System.out.println("  " + jointID + ": +ve turn time too short (" +
                             time + "); ignoring");
      else {
        armComms.turn(jointID, ArmCommunicator.POSITIVE, time);
        currAngle = angle;
      }
    }
  }  // end of timedAngleTurn()



  public void turnToAngle(int angle)
  /* turn to specified angle (+ve is forwards/right,
     -ve is backwards/left) */
  {  timedAngleTurn( withinLimits(angle) );  }



  public boolean isInRange(int angle)
  {  return ((angle > negLimit) && (angle < posLimit));  }



 // ------ read and write joint info to a file ---------------


  private void readJointInfo(String fnm)
  /* read joint info lines
          rotate:  <rotTime int>
          limits: <posLimit int> <negLimit int>
          angle: <currAngle int>
  */
  {
    System.out.println("Openning joint info file: " + fnm);
    String line;
    String[] toks;
    try {
      BufferedReader in = new BufferedReader(new FileReader(fnm));

      // get rotTime
      line = in.readLine();
      toks = line.split("\\s+");
      try {
        rotTime = Integer.parseInt(toks[1]); 
      }
      catch (NumberFormatException e)
      { System.out.println("Error reading line \"" + line + "\"");  }

      // get pos and neg limits
      line = in.readLine();
      toks = line.split("\\s+");
      try {
        posLimit = Integer.parseInt(toks[1]);
        negLimit = Integer.parseInt(toks[2]);
      }
      catch (NumberFormatException e)
      { System.out.println("Error reading line \"" + line + "\"");  }

      // get current angles
      line = in.readLine();
      toks = line.split("\\s+");
      try {
        currAngle = Integer.parseInt(toks[1]);
      }
      catch (NumberFormatException e)
      { System.out.println("Error reading line \"" + line + "\"");  }
      in.close();
/*
      System.out.println("  rotTime: " + rotTime);
      System.out.println("  posLimit: " + posLimit + "; negLimit: " + negLimit);
      System.out.println("  currAngle: " + currAngle);
      System.out.println("-------");
*/
    }
    catch (IOException e)
    { System.out.println("Could not read joint info from " + fnm );
      System.exit(0);
    }
  }  // end of readJointInfo()



  private void checkInfo()
  {
    // check rotation time 
    if (rotTime == 0) {
      System.out.println("Positive rotation time cannot be 0");
      System.exit(0);
    }
    else if (rotTime < 0) {
      System.out.println("Rotation time must be positive");
      rotTime = -rotTime;
    }

    // check positive angle limit 
    if (posLimit == 0) {
      System.out.println("Positive limit cannot be 0");
      System.exit(0);
    }
    else if (posLimit < 0) {
      System.out.println("Positive limit must be positive");
      posLimit = -posLimit;
    }

    // check negative angle limit 
    if (negLimit == 0) {
      System.out.println("Negative limit cannot be 0");
      System.exit(0);
    }
    else if (negLimit > 0) {
      System.out.println("Negative limit must be negative");
      negLimit = -negLimit;     // don't negative value
    }

    rotRate = ((float)(posLimit-negLimit))/rotTime;

    // check current angle 
    if ((currAngle >= posLimit) || (currAngle <= negLimit)) {
      System.out.println("Rest angle out of range; using 0");
      currAngle = 0;
    }
  }  // end of checkInfo()



  private void writeJointInfo(String fnm)
  // write out three lines for the joint info
  {
    try {
      PrintWriter out = new PrintWriter( new FileWriter(fnm));
      out.println("rotate: " + rotTime);
      out.println("limits: " + posLimit + " " + negLimit);
      out.println("angle: " + currAngle);
      out.close();
      System.out.println("Saved joint info to " + fnm);
    }
    catch (IOException e)
    {  System.out.println("Could not save joint info to " + fnm); }
  }  // end of writeJointInfo()


}  // end of Joint class
