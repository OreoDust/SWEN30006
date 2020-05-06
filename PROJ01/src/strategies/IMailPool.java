package strategies;


import automail.IRobot;
import automail.MailItem;
import exceptions.BreakingFragileItemException;
import exceptions.ItemTooHeavyException;

/**
 * addToPool is called when there are mail items newly arrived at the building to add to the MailPool or
 * if a robot returns with some undelivered items - these are added back to the MailPool.
 * The data structure and algorithms used in the MailPool is your choice.
 * 
 */
public interface IMailPool {
	
    /**
     * Adds an item to the mail pool
     * @param mailItem the mail item being added.
     */
     void addToPool(MailItem mailItem);
    
    /**
     * load up any waiting robots with mailItems, if any.
     */
	void step() throws ItemTooHeavyException, BreakingFragileItemException;

    /**
     * @param register robot to list when arrives destination floor
     */	
     void registerDelivery(IRobot robot);

    /**
     * @param unregister robot from list when leaves current floor after delivery
     */	
     void unregisterDelivery(IRobot robot);

    /**
     * @param check if current floor of robot is empty
     */	
     boolean checkFloorEmpty(IRobot currentRobot);

    /**
     * @param check if delivery is allowed before entering a robot
     */	
     boolean checkDeliveryAllowed(IRobot currentRobot);

    /**
     * @param check if current robot is registered delivery
     */	
     boolean isRegistered(IRobot currentRobot);

    /**
     * @param robot refers to a robot which has arrived back ready for more mailItems to deliver
     */	
	void registerWaiting(IRobot robot);

}
