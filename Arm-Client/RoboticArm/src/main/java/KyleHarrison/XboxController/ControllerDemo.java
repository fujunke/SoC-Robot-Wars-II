package KyleHarrison.XboxController;

//TurtleDemo.java

import KyleHarrison.RoboticArm.ArmCommunicator;
import ch.aplu.turtle.*;
import ch.aplu.xboxcontroller.*;

import java.awt.event.*;
import java.io.Console;
import java.util.Scanner;

public class ControllerDemo {
	private class MyXboxControllerAdapter extends XboxControllerAdapter {
		
		private boolean lightFlipState = false;
		private boolean gripperFlipState = false;
		private int lastInput = 0;
		
		public void leftThumbMagnitude(double magnitude) {
			speed = (int) (100 * magnitude);
			t.speed(speed);
			setLastInput();
		}

		public void rightThumbMagnitude(double magnitude) {
			speed = (int) (100 * magnitude);
			t.speed(speed);
			setLastInput();
		}

		public void buttonA(boolean pressed) {
			if (pressed) {
				System.out.println("Opened");
				arm.doArmOp('u', arm);
				setLastInput();
			}
		}

		public void buttonB(boolean pressed) {
			if (pressed) {
				System.out.println("Closed");
				arm.doArmOp('j', arm);
				setLastInput();
			}
		}

		public void leftShoulder(boolean pressed) {
			if (pressed) {

					if (lightFlipState) {
						// Turn Off
						arm.doArmOp('p', arm);
						lightFlipState = false;
					} else {
						// Turn On
						arm.doArmOp('l', arm);
						lightFlipState = true;
					}
					setLastInput();
			}
		}

		public void rightShoulder(boolean pressed) {
			if (pressed) {
				if (gripperFlipState) {
					// Turn Off
					arm.doArmOp('s', arm);
					gripperFlipState = false;
				} else {
					// Turn On
					arm.doArmOp('w', arm);
					gripperFlipState = true;
				}
				setLastInput();
			}
		}
		
		  /**
		   * Notification when the direction pad is actuated. This pad has 8 
		   * possible positions returned by the direction parameter: 0->NORTH, 
		   * 1->NORTHEAST, 2->EAST, 3->SOUTHEAST, 4->SOUTH, 5->SOUTHWEST,
		   * 6->WEST, 7->NORTHWEST 
		   * @param direction one of the values 0..7
		   * @param pressed true, when button is pressed; false, when released
		   */
		public void dpad(int direction, boolean pressed){
			if (pressed) {
				
				switch(direction){
				case 0:
					arm.doArmOp('d', arm);
					break;
				case 2: 
					arm.doArmOp('f', arm);
					break;
				case 4:
					arm.doArmOp('e', arm);
					break;
				case 6:
					arm.doArmOp('r', arm);
					break;	
				}
			}
		}

		public void rightTrigger(double value) {
			System.out.println("Turn Right");
			arm.doArmOp('i', arm);
			setLastInput();
		}

		public void leftTrigger(double value) {
			System.out.println("Turn Left");
			arm.doArmOp('k', arm);
			setLastInput();
		}

		public void leftThumbDirection(double direction) {
			t.heading(direction);
			setLastInput();
		}

		public void rightThumbDirection(double direction) {
			t.heading(direction);
			if (direction > 160 && direction > 180) {
				// arm.doArmOp('e', arm);
			} else if (direction > 0 && direction > 20 || direction > 330) {
				// arm.doArmOp('d', arm);
			}
			setLastInput();
		}



		public void setLastInput() {
			lastInput = (int) (System.currentTimeMillis() / 1000);
		}

		public boolean checkInput() {
			int time = (int) (System.currentTimeMillis() / 1000);
			if ((lastInput + 1) == time || lastInput == 0) {
				return true;
			} else {
				return false;
			}
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

	public ControllerDemo() {
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
		this.arm = new ArmCommunicator();

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
