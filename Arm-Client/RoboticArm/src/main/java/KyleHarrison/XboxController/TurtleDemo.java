package KyleHarrison.XboxController;

//TurtleDemo.java

import KyleHarrison.RoboticArm.ArmCommunicator;
import ch.aplu.turtle.*;
import ch.aplu.xboxcontroller.*;

import java.awt.event.*;
import java.io.Console;
import java.util.Scanner;

public class TurtleDemo {
	private class MyXboxControllerAdapter extends XboxControllerAdapter {
		public void leftThumbMagnitude(double magnitude) {
			speed = (int) (100 * magnitude);
			t.speed(speed);
		}
		
		public void rightThumbMagnitude(double magnitude){
			speed = (int) (100 * magnitude);
			t.speed(speed);
		}
		
		public void buttonA(boolean pressed){
			System.out.println("Opened");
			arm.doArmOp('u', arm);
		}
		
		
		public void buttonB(boolean pressed){
			System.out.println("Closed");
			arm.doArmOp('j', arm);
		}


		public void leftThumbDirection(double direction) {
			t.heading(direction);
		}
		
		public void rightThumbDirection(double direction){
			t.heading(direction);
		}

		public void start(boolean pressed) {
			if (pressed) {
				t.clear();
				t.home();
			}
		}

		public void isConnected(boolean connected) {
			if (connected)
				t.setTitle(connectInfo);
			else
				t.setTitle("Connection lost");
		}
	}

	private final String connectInfo = "Connected. 'Left thumb' to move, 'Start' to clear";
	private Turtle t = new Turtle();
	private volatile boolean isExiting = false;
	private int speed = 0;
	private ArmCommunicator arm;
	
	public TurtleDemo() {
		t.setTitle("Connecting...");
		XboxController xc = new XboxController();
		t.wrap();
		if (!xc.isConnected())
			t.setTitle("Xbox controller not connected");
		else
			t.setTitle(connectInfo);
		xc.addXboxControllerListener(new MyXboxControllerAdapter());
		xc.setLeftThumbDeadZone(0.2);
		t.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				isExiting = true;
			}
		});

		while (!isExiting) {
			if (speed > 10)
				t.forward(10);
			else
				Turtle.sleep(10);
			
			this.arm = new ArmCommunicator();

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

			// restore start state
			// arm.openGripper(true);
			// arm.setLight(false);

			arm.close();
			
		}
		xc.release();
		System.exit(0);
	}

	public static void main(String[] args) {
			
		
		new TurtleDemo();
	}
}
