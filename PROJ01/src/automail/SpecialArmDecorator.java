package automail;

import automail.Robot.RobotState;
import exceptions.BreakingFragileItemException;
import exceptions.ExcessiveDeliveryException;
import exceptions.ItemTooHeavyException;

public class SpecialArmDecorator implements IRobot{
	
	static public final int INDIVIDUAL_MAX_WEIGHT = 2000;
	
	private static final int WRAPPING_TIME = 2;
	private static final int UNWRAPPING_TIME = 1;
	
	public RobotState current_state;
	
	private Robot robot;
	private MailItem fragileItem;
	private int wrapping_count;
	private int unwrapping_count;
	private boolean receivedDispatch;
	private boolean isWrapping;
	private boolean isUnwrapping;
    private int deliveryCounter;
	
	public SpecialArmDecorator(Robot robot) {
		this.robot = robot;
		fragileItem = null;
		wrapping_count = 0;
		unwrapping_count = 0;
		this.receivedDispatch = false;
		this.isWrapping = false;
		this.isUnwrapping = false;
	    this.deliveryCounter = 0;
	}

	@Override
	public void step() throws ExcessiveDeliveryException {
		switch(robot.getCurrentState()) {
    		/** This state is triggered when the robot is returning to the mailroom after a delivery */
    		case RETURNING:
    			/** If its current position is at the mailroom, then the robot should change state */
                if(robot.getCurrentFloor() == Building.MAILROOM_LOCATION){
        			/** Tell the sorter the robot is ready */
                	robot.getMailpool().registerWaiting(this);
                	changeState(RobotState.WAITING);
                } else {
                	/** If the robot is not at the mailroom floor yet, then move towards it! */
                    moveTowards(Building.MAILROOM_LOCATION);
                	break;
                }
			case WAITING:
                /** If the StorageTube is ready and the Robot is waiting in the mailroom then start the delivery */
				if(!isEmpty() && receivedDispatch) {
					
					receivedDispatch = false;
                	deliveryCounter = 0; // reset delivery counter
					if (fragileItem != null) { // prioritise fragile items first
						// Wrapping the package
						if (wrapping_count < WRAPPING_TIME) {
							this.isWrapping = true;
							wrapping_count++;
							receivedDispatch = true;
							break;
						} else {
							this.isWrapping = false;
							setRoute(fragileItem);
							wrapping_count = 0;
						}
        			} else {
						setRoute(robot.getDeliveryItem());
					}
					changeState(RobotState.DELIVERING);
				} 
				break;
			case DELIVERING:
				if(robot.getCurrentFloor() == robot.getDestinationFloor()) { // If already here drop off either way
					if(robot.getMailpool().checkFloorEmpty(this)) {
						robot.getMailpool().registerDelivery(this);
					}
					if (robot.getMailpool().checkDeliveryAllowed(this)) {
						if (fragileItem != null) {
							// Unwrapping the package
							if (unwrapping_count < UNWRAPPING_TIME) {
								this.isUnwrapping = true;
								unwrapping_count++;
								break;
							} else {
								this.isUnwrapping = false;
								robot.getDelivery().deliver(fragileItem);
								fragileItem = null;
								unwrapping_count = 0;
							}
	                    }
	    				/** Delivery complete, report this to the simulator! */
	                    if(deliveryCounter > 3){  // Implies a simulation bug
	                    	throw new ExcessiveDeliveryException();
	                    }
	                    /** Check if want to return, i.e. if there is no item in the tube*/
	                    if(robot.getDeliveryItem() == null && robot.getTube() == null){
	                    	changeState(RobotState.RETURNING);
	                    } else {
	                    	if (robot.getDeliveryItem() != null) {
	                    		robot.getDelivery().deliver(robot.getDeliveryItem());
	                    		robot.setDeliveryItem(null);
	                            deliveryCounter++;
	                            
	                            if (robot.getTube() != null) {
	                            	/** If there is another item, set the robot's route to the location to deliver the item */
	                            	robot.setDeliveryItem(robot.getTube());
	                            	robot.setTube(null);
	                                setRoute(robot.getDeliveryItem());
	                                changeState(RobotState.DELIVERING);
	                            } else {
									changeState(RobotState.RETURNING);
								}
	                    	}
						}
					}
					if (robot.getMailpool().isRegistered(this)) {
						robot.getMailpool().unregisterDelivery(this);
					}
    			} else {
	        		/** The robot is not at the destination yet, move towards it! */
	                moveTowards(robot.getDestinationFloor());
    			}
                break;
    	}
	}
	

	@Override
	public void moveTowards(int destination) {
		if (robot.getMailpool().isRegistered(this)) {
			robot.getMailpool().unregisterDelivery(this);
		}
        if(robot.getCurrentFloor() < destination){
        	int floor = robot.getCurrentFloor();
   		 	floor++;
   		 	robot.setCurrentFloor(floor);
        } else {
        	int floor = robot.getCurrentFloor();
   		 	floor--;
   		 	robot.setCurrentFloor(floor);
		}
		// Register delivery
		if (robot.getCurrentFloor() == robot.getDestinationFloor()) {
			if (robot.getMailpool().checkDeliveryAllowed(this)) {
				robot.getMailpool().registerDelivery(this);
			}
		}
    } 

	@Override
	public void dispatch() {
    	receivedDispatch = true;
	}

	public boolean isWrapping() {
		return this.isWrapping;
	}

	public boolean isUnwrapping() {
		return this.isUnwrapping;
	}

	@Override
	public void addToHand(MailItem mailItem) throws ItemTooHeavyException, BreakingFragileItemException {
		assert(robot.getDeliveryItem() == null);
		if(mailItem.fragile) {	
			fragileItem = mailItem;
		}else {
			robot.setDeliveryItem(mailItem);
		}
		
		if (mailItem.weight > INDIVIDUAL_MAX_WEIGHT) throw new ItemTooHeavyException();
	}

	@Override
	public void addToTube(MailItem mailItem) throws ItemTooHeavyException, BreakingFragileItemException {
		robot.addToTube(mailItem);
		
	}

	@Override
	public boolean isEmpty() {
		return (fragileItem == null && robot.getDeliveryItem() == null && robot.getTube() == null);
	}

	protected void setRoute(MailItem mailItem) {
        /** Set the destination floor */
		robot.setDestinationFloor(mailItem.getDestFloor());
    }
	 
	@Override
	public void changeState(RobotState nextState) {
		if(fragileItem != null) {
			System.out.printf("T: %3d > %9s-> [%s]%n", Clock.Time(), robot.getIdTube(), fragileItem.toString());
		}
		robot.changeState(nextState);
	}

	@Override
	public int getCurrentFloor() {
		return robot.getCurrentFloor();
	}

	@Override
	public int getDestinationFloor() {
		return robot.getDestinationFloor();
	}
	
	public boolean hasFragile() {
		return fragileItem != null;	
	}
	
}