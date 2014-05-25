package KyleHarrison.XboxController;

import KyleHarrison.RoboticArm.ArmCommunicator;
import ch.aplu.turtle.*;
import ch.aplu.xboxcontroller.*;

import java.awt.event.*;
import java.io.Console;
import java.util.Scanner;

public class ControllerDemo {

	private final String connectInfo = "Connected. 'Left thumb' to move, 'Start' to clear";
	private volatile boolean isExiting = false;
	private int speed = 0;
	private ArmCommunicator arm;

	public ControllerDemo() {
		this.arm = new ArmCommunicator();
		
		XboxController xc = new XboxController();
		xc.isConnected();

		xc.addXboxControllerListener(new MyXboxControllerAdapter(this.arm));
		xc.setLeftThumbDeadZone(0.2);
		
		while (!isExiting) {

			System.out.println("Enter a single letter command (and <ENTER>:");
			arm.printHelp();
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
					arm.printHelp();
				else
					arm.doArmOp(ch, arm);
				System.out.print(">> ");
				break;
			}
		}
		arm.close();
		xc.release();
		System.exit(0);
	}

	public static void main(String[] args) {
		new ControllerDemo();
	}
}
