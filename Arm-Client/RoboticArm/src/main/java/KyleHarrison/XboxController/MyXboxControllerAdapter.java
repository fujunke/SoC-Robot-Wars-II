package KyleHarrison.XboxController;

import KyleHarrison.RoboticArm.ArmCommunicator;
import ch.aplu.xboxcontroller.XboxControllerAdapter;

public class MyXboxControllerAdapter extends XboxControllerAdapter {
	private boolean lightFlipState = false;
	private boolean gripperFlipState = false;
	private int lastInput = 0;
	private ArmCommunicator arm;
	
	public MyXboxControllerAdapter(ArmCommunicator arm) {
		super();
		this.arm = arm;
	}

	public void leftThumbMagnitude(double magnitude) {
		setLastInput();
	}
	
	ControllerSettings leftUpThumb = new ControllerSettings();
	ControllerSettings rightUpThumb = new ControllerSettings();
	
	private void leftUpThumb(double direction){
		if(this.leftUpThumb.enabled==false){
			System.out.println(direction);
			this.leftUpThumb.waitTime=this.leftUpThumb.delay;	
			if(direction>=175 && direction <=185){
				this.leftUpThumb.enabled=true;	
			}
		}else if(this.leftUpThumb.enabled){
			if(direction>=175 && direction <=185){
				arm.doArmOp('f', arm);
			}else{
				this.leftUpThumb.enabled=false;
			}
		}
	}
	
	private void rightUpThumb(double direction){
		if(this.rightUpThumb.enabled==false){
			System.out.println(direction);
			if(direction>=0 && direction <=35){
				this.rightUpThumb.enabled=true;
			}
		}else if(this.rightUpThumb.enabled){
			if(direction>=0 && direction <=35){
				arm.doArmOp('r', arm);
			}else{
				this.rightUpThumb.enabled=false;
			}
		}
	}
	
	public void leftThumbDirection(double direction){
		
		leftUpThumb(direction);
	
		rightUpThumb(direction);
		
	}

	public void rightThumbMagnitude(double magnitude) {
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

	public void rightThumbDirection(double direction) {
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

		}
	}

	public void isConnected(boolean connected) {

	}
}
