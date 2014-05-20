package pi.examples;

import java.util.Scanner;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class MotorController {
	
	private static GpioPinDigitalOutput myLed;
	private static GpioController gpio;
	private static Scanner keyboard;

    public static void main(String[] args) throws InterruptedException {
    	
    	 System.out.println("<--Pi4J--> GPIO Motor Controller ... started.");
    	 
         // create gpio controller
    	 initilisation();
         gpio = GpioFactory.getInstance();
         gpio = setup(gpio);
         
         // keep program running until user aborts (CTRL-C)
         for (;;) {
              if(getKeyboardInput()==0){
             	 myLed.blink(1000, 3000);
              }
             //Thread.sleep(500);
              menu();
             System.out.println("Cycling motors");
         }
         
         // stop all GPIO activity/threads by shutting down the GPIO controller
         // (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
         //gpio.shutdown();  
    }
    
    private static void initilisation(){
    	keyboard = new Scanner(System.in);
    }
    
    private static void menu(){
    	System.out.println("Select a menu option : ");
    }
    
    private static GpioController setup(GpioController gpio){
        // setup gpio pins #04 an output pins and make sure they are all LOW at startup
        myLed = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, PinState.LOW);
        System.out.println(" ... complete the GPIO #02 circuit and see the blink trigger take effect.");
        return gpio;
    }
    
    private static int getKeyboardInput(){
        System.out.println("enter an integer");
        return keyboard.nextInt();
    }
	
}
