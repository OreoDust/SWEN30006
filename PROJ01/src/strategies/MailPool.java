package strategies;

import java.util.LinkedList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.ListIterator;

import automail.IRobot;
import automail.MailItem;
import exceptions.BreakingFragileItemException;
import exceptions.ItemTooHeavyException;

public class MailPool implements IMailPool {

	private class Item {
		int destination;
		MailItem mailItem;
		
		public Item(MailItem mailItem) {
			destination = mailItem.getDestFloor();
			this.mailItem = mailItem;
		}
	}
	
	public class ItemComparator implements Comparator<Item> {
		@Override
		public int compare(Item i1, Item i2) {
			int order = 0;
			if (i1.destination > i2.destination) {  // Further before closer
				order = 1;
			} else if (i1.destination < i2.destination) {
				order = -1;
			}
			return order;
		}
	}
	
	private LinkedList<Item> pool;
	private LinkedList<IRobot> robots;
	private HashMap<Integer, LinkedList<IRobot>> deliveringRobots;

	public MailPool(int nrobots){
		// Start empty
		pool = new LinkedList<Item>();
		robots = new LinkedList<IRobot>();
		deliveringRobots = new HashMap<Integer, LinkedList<IRobot>>();
	}

	public void addToPool(MailItem mailItem) {
		Item item = new Item(mailItem);
		pool.add(item);
		pool.sort(new ItemComparator());
	}
	
	@Override
	public void step() throws ItemTooHeavyException, BreakingFragileItemException {
		try{
			ListIterator<IRobot> i = robots.listIterator();
			while (i.hasNext()) loadRobot(i);
		} catch (Exception e) { 
            throw e; 
        } 
	}
	
	private void loadRobot(ListIterator<IRobot> i) throws ItemTooHeavyException, BreakingFragileItemException {
		IRobot robot = i.next();
		assert(robot.isEmpty());
		// System.out.printf("P: %3d%n", pool.size());
		ListIterator<Item> j = pool.listIterator();
		if (pool.size() > 0) {
			boolean inHand = false;
			boolean inTube = false;
			try {
				while(pool.size() > 0) {				
					MailItem item = j.next().mailItem;
					if(item.isFragile()) {
						if (!robot.hasFragile()) {
							robot.addToHand(item);
							j.remove();
						} else {
							break;
						}
					} else {
						if(!inHand) {
							robot.addToHand(item);
							j.remove();
							inHand = true;
						} else if (!inTube) {
							robot.addToTube(item);
							j.remove();
							inTube = true;
						} else {
							break;
						}
					}
				}
				robot.dispatch(); // send the robot off if it has any items to deliver
				i.remove(); // remove from mailPool queue
			} catch (Exception e) {
				try {
					throw e;
				} catch (Exception e1) {
					e1.printStackTrace();
				}
	        } 
		}
	}
	
	public void registerDelivery(IRobot currentRobot) {
		int dest_floor = currentRobot.getDestinationFloor();
		if (deliveringRobots.get(dest_floor) == null) {
			LinkedList<IRobot> onFloorRobots = new LinkedList<>();
			onFloorRobots.add(currentRobot);
			deliveringRobots.put(dest_floor, onFloorRobots);
		} else {
			deliveringRobots.get(dest_floor).add(currentRobot);
		}
	}
	
	public void unregisterDelivery(IRobot currentRobot) {
		deliveringRobots.get(currentRobot.getCurrentFloor()).remove(currentRobot);
	}
	
	@Override
	public boolean checkFloorEmpty(IRobot currentRobot) {
		LinkedList<IRobot> onFloorRobots = deliveringRobots.get(currentRobot.getCurrentFloor()); 
		return onFloorRobots == null;
	}
	
	public boolean checkDeliveryAllowed(IRobot currentRobot) {
		LinkedList<IRobot> onFloorRobots = deliveringRobots.get(currentRobot.getCurrentFloor());
		if (onFloorRobots != null) {
			if (onFloorRobots.stream().filter(robot -> robot.hasFragile()).findAny().isPresent()) {
				return onFloorRobots.getFirst().equals(currentRobot);
			}else {
				return true;
			}
		}else{
			return true;
		}
	}

	public boolean isRegistered(IRobot currentRobot) {
		return deliveringRobots.get(currentRobot.getCurrentFloor()) != null;
	}

	@Override
	public void registerWaiting(IRobot robot) { // assumes won't be there already
		robots.add(robot);
	}

}
