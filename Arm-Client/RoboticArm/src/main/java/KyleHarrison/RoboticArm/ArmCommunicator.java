package KyleHarrison.RoboticArm;

// ArmCommunicator.java
// Andrew Davison, ad@fivedots.coe.psu.ac.th, June 2011

/* Class for rotating the Maplin Robot Arm
 (http://www.maplin.co.uk/robotic-arm-kit-with-usb-pc-interface-266257) 
 It's the UK version of the US OWI-535
 (http://www.owirobot.com/products/Robotic-Arm-Edge.html)
 with the OWI USB interface included

 --------
 The robot arm ops are spread over three bytes:

 First byte:
 Gripper close == 0x01     Gripper open == 0x02
 Wrist forwards == 0x04          Wrist backwards == 0x08
 Elbow forwards == 0x10          Elbow backwards == 0x20
 Shoulder forwards == 0x40       Shoulder backwards == 0x80

 Second byte:
 Base rotate right == 0x01  Base rotate left == 0x02

 Third byte:
 Light on == 0x01

 Set all bytes to zero for all off
 --------

 This Java coded utilizes the libraries 
 * libusbjava (http://libusbjava.sourceforge.net/)
 * libusb-win32 (http://sourceforge.net/apps/trac/libusb-win32/wiki)

 Other requirements:
 - the Robot Arm must be plugged into a USB port;

 - a libusb-win32 device driver for the arm must have been created and
 installed into Windows (use inf-wizard.exe)

 Usage:
 > compile ArmCommunicator.java
 > run ArmCommunicator
 */

import java.io.Console;
import java.util.Scanner;

import ch.ntb.usb.Device;
import ch.ntb.usb.LibusbJava;
import ch.ntb.usb.USB;
import ch.ntb.usb.USBException;

public class ArmCommunicator {
	public final static int POSITIVE = 0;
	public final static int NEGATIVE = 1;
	// +ve for forward/right turns; -ve for backwards/left turns

	private final static short VENDOR_ID = (short) 0x1267;
	private final static short PRODUCT_ID = (short) 0x0;
	// the IDs were obtained by looking at the robot arm using USBDeview

	private final static int GRIPPER_PERIOD = 1700; // ms time to open/close

	private Device dev = null; // used to communicate with the USB device

	// start state for the light
	private boolean isLightOn = false;

	public ArmCommunicator() {
		System.out.println("Looking for device: (vendor: "
				+ toHexString(VENDOR_ID) + "; product: "
				+ toHexString(PRODUCT_ID) + ")");
		dev = USB.getDevice(VENDOR_ID, PRODUCT_ID);
		try {
			System.out.println("Opening device");
			dev.open(1, 0, 0);
			// open device with configuration 1, interface 0 and no alt
			// interface
		} catch (USBException e) {
			System.out.println(e);
			System.exit(1);
		}
	} // end of ArmCommunicator()

	public void close() {
		System.out.println("Closing device");
		try {
			if (dev != null)
				dev.close();
		} catch (USBException e) {
			System.out.println(e);
			System.exit(1);
		}
	} // end of close()

	// ------------------------------ command ops --------------------------

	/*
	 * First byte: Gripper close == 0x01 Gripper open == 0x02
	 */

	public void openGripper(boolean isOpen) {
		if (isOpen) {
			System.out.println("  Gripper: open");
			sendCommand(0x02, 0x00, GRIPPER_PERIOD);
		} else {
			System.out.println("  Gripper: close");
			sendCommand(0x01, 0x00, GRIPPER_PERIOD);
		}
	} // end of openGripper()

	public void setLight(boolean turnOn)
	// Third byte: Light on/off
	{
		isLightOn = turnOn;
		System.out.println("  Light is on: " + isLightOn);
		sendControl(0x00, 0x00, getLightVal(isLightOn));
	}

	private int getLightVal(boolean isLightOn)
	// light on/off
	{
		return (isLightOn) ? 0x01 : 0x00;
	}

	public void turn(JointID jid, int dir, int period)
	/*
	 * First byte: Wrist forwards == 0x04 Wrist backwards == 0x08 Elbow forwards
	 * == 0x10 Elbow backwards == 0x20 Shoulder forwards == 0x40 Shoulder
	 * backwards == 0x80
	 * 
	 * Second byte: Base rotate right == 0x01 Base rotate left == 0x02
	 */
	{
		int opCode1 = 0x00;
		int opCode2 = 0x00;

		if (jid == JointID.BASE)
			opCode2 = (dir == POSITIVE) ? 0x01 : 0x02;
		else if (jid == JointID.SHOULDER)
			opCode1 = (dir == POSITIVE) ? 0x80 : 0x40;
		else if (jid == JointID.ELBOW)
			opCode1 = (dir == POSITIVE) ? 0x20 : 0x10;
		else if (jid == JointID.WRIST)
			opCode1 = (dir == POSITIVE) ? 0x08 : 0x04;
		else
			System.out.println("Unknown joint ID: " + jid);

		if (period < 0) {
			System.out.println("Turn period cannot be negative");
			period = 0;
		}

		System.out.println("  " + jid + " timed turn: " + dir + " " + period
				+ "ms");
		sendCommand(opCode1, opCode2, period);
	} // end of turn()

	private void sendCommand(int opCode1, int opCode2, int period)
	// execute the operation for period millisecs
	{
		int opCode3 = getLightVal(isLightOn); // third byte == light on/off
		if (dev != null) {
			sendControl(opCode1, opCode2, opCode3);
			wait(period);
			sendControl(0, 0, opCode3); // stop arm
		}
	} // end of sendCommand()

	private void sendControl(int opCode1, int opCode2, int opCode3)
	// send a USB control transfer
	{
		/*
		 * System.out.println("Sending ops: <" + toHexString(opCode1) + ", " +
		 * toHexString(opCode2) + ", " + toHexString(opCode3) + ">");
		 */
		byte[] bytes = { new Integer(opCode1).byteValue(),
				new Integer(opCode2).byteValue(),
				new Integer(opCode3).byteValue() };
		try {
			int rval = dev.controlMsg(USB.REQ_TYPE_DIR_HOST_TO_DEVICE
					| USB.REQ_TYPE_TYPE_VENDOR, // 0x40,
					0x06, 0x0100, 0, bytes, bytes.length, 2000, false);
			// System.out.println("usb_control_msg() result: " + rval);
			if (rval < 0) {
				System.out.println("Control Error (" + rval + "):\n  "
						+ LibusbJava.usb_strerror());
			}
		} catch (USBException e) {
			System.out.println(e);
		}
	} // end of sendControl()

	private String toHexString(int b)
	// chanage the hexadecimal integer into "0x.." string format
	{
		String hex = Integer.toHexString(b);
		if (hex.length() == 1)
			return "0x0" + hex;
		else
			return "0x" + hex;
	} // end of toHexString

	public void wait(int ms)
	// sleep for the specified no. of millisecs
	{
		// System.out.println("Waiting " + ms + " ms...");
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
		}
	} // end of wait()

	// ------------------------------------ test rig --------------------------

	private static final int DELAY = 250;

	public static void main(String[] args) {
		ArmCommunicator arm = new ArmCommunicator();

		System.out.println("Enter a single letter command (and <ENTER>:");
		printHelp();
		Console console = System.console();
		String line = null;
		char ch;

		System.out.print(">> ");

		Scanner in = new Scanner(System.in);

		while ((line = in.next()) != null) {
			if (line.length() == 0)
				break;
			ch = line.charAt(0);
			if (ch == 'q')
				break;
			else if (ch == '?')
				printHelp();
			else
				doArmOp(ch, arm);
			System.out.print(">> ");
		}

		// restore start state
		// arm.openGripper(true);
		// arm.setLight(false);

		arm.close();
	} // end of main()

	public static void printHelp() {
		System.out.println("  Gripper: close == w ; open == s");
		System.out.println("  Wrist:     fwd == e ; back == d");
		System.out.println("  Elbow:     fwd == r ; back == f");
		System.out.println("  Shoulder:  fwd == u ; back == j");
		System.out.println("  Base:     left == k ; right == i");
		System.out.println("  Light:      on == l ; off == p");
		System.out.println("            quit == q ; help == ?");
	} // end of printHelp()

	public static void doArmOp(char ch, ArmCommunicator arm)
	// use POSITIVE for forwards/right turns; NEGATIVE for backwards/left turns
	{
		if (ch == 'w') // gripper close
			arm.openGripper(false);
		else if (ch == 's') // gripper open
			arm.openGripper(true);
		else if (ch == 'e') // wrist forwards
			arm.turn(JointID.WRIST, ArmCommunicator.POSITIVE, DELAY);
		else if (ch == 'd') // wrist backwards
			arm.turn(JointID.WRIST, ArmCommunicator.NEGATIVE, DELAY);
		else if (ch == 'r') // elbow forwards
			arm.turn(JointID.ELBOW, ArmCommunicator.POSITIVE, DELAY);
		else if (ch == 'f') // elbow backwards
			arm.turn(JointID.ELBOW, ArmCommunicator.NEGATIVE, DELAY);
		else if (ch == 'u') // shoulder forwards
			arm.turn(JointID.SHOULDER, ArmCommunicator.POSITIVE, DELAY);
		else if (ch == 'j') // shoulder backwards
			arm.turn(JointID.SHOULDER, ArmCommunicator.NEGATIVE, DELAY);
		else if (ch == 'k') // base left
			arm.turn(JointID.BASE, ArmCommunicator.NEGATIVE, DELAY);
		else if (ch == 'i') // base right
			arm.turn(JointID.BASE, ArmCommunicator.POSITIVE, DELAY);
		else if (ch == 'l') // light on
			arm.setLight(true);
		else if (ch == 'p') // light off
			arm.setLight(false);
		else
			System.out.println("Unknown command: " + ch);
	} // end of doArmOp()

} // end of ArmCommunicator class
