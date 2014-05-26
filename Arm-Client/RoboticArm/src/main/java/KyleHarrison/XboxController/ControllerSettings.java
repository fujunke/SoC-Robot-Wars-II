package KyleHarrison.XboxController;

public class ControllerSettings {

	int delay = 5;
	int waitTime = 0;
	double lastInput;
	boolean enabled = false;
	
	public ControllerSettings() {
		super();
	}
	
	public int getDelay() {
		return delay;
	}
	public void setDelay(int delay) {
		this.delay = delay;
	}
	public double getLastInput() {
		return lastInput;
	}
	public void setLastInput(double lastInput) {
		this.lastInput = lastInput;
	}
	public int getWaitTime() {
		return waitTime;
	}
	public void setWaitTime(int waitTime) {
		this.waitTime = waitTime;
	}
	
	
}
