package strategies;

import automail.IMailDelivery;
import automail.IRobot;
import automail.Robot;
import automail.SpecialArmDecorator;

public class Automail {
	      
    public IRobot[] robots;
    public IMailPool mailPool;
    
    public Automail(IMailPool mailPool, IMailDelivery delivery, int numRobots, boolean CAUTION_ENABLED) {
    	// Swap between simple provided strategies and your strategies here
    	    	
    	/** Initialize the MailPool */
    	
    	this.mailPool = mailPool;
    	
    	/** Initialize robots */
    	robots = new IRobot[numRobots];
    	for (int i = 0; i < numRobots; i++) {
    		if(CAUTION_ENABLED) {
    			robots[i] = new SpecialArmDecorator(new Robot(delivery, mailPool));
    		}else {
    			robots[i] = new Robot(delivery, mailPool);
    		}
    	}
    }
    
}
