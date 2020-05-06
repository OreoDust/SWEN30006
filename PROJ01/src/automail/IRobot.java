package automail;

import automail.Robot.RobotState;
import exceptions.BreakingFragileItemException;
import exceptions.ExcessiveDeliveryException;
import exceptions.ItemTooHeavyException;

public interface IRobot {
	
	 void step() throws ExcessiveDeliveryException;
	 void moveTowards(int destination);

	 void dispatch();
	 void changeState(RobotState nextState);
	 boolean isEmpty();
	 
	 int getCurrentFloor();
	 int getDestinationFloor();
	 boolean hasFragile();

	 boolean isWrapping();

	 boolean isUnwrapping();
	 
	 /**
	  * Add mail to Robot's Hand
	  * @param mailItem
	  * @throws ItemTooHeavyException
	  * @throws BreakingFragileItemException
	  */
	 void addToHand(MailItem mailItem) throws ItemTooHeavyException, BreakingFragileItemException;
	 
	 /**
	  * Add mail to Robot's Tube
	  * @param mailItem
	  * @throws ItemTooHeavyException
	  * @throws BreakingFragileItemException
	  */
	 void addToTube(MailItem mailItem) throws ItemTooHeavyException, BreakingFragileItemException;	 
	
}