package edu.gmu.swe622.lab3;

import java.util.LinkedList;
import java.util.List;

/*
 * Problem description:
 * Barber:
	Cuts 1 person’s hair at a time
	When finished, dismiss customer.
	Check waiting room for more customers. If more, then cut next customer’s hair. If no more, take a nap
   Customer:
	 Walks in, sees if barber is napping, if so, wakes barber, else, goes to waiting room
  Create a small simulator for this, assuming that there will be 1 barber, and a variable number of customers,
  arriving at random times
 */
public class SleepingBarber {

	static class Barber implements Runnable{
		public String name;
		List<Customer> waitingRoom;
		public Barber(String name, List<Customer> waitingRoom) {
			this.name = name;
			this.waitingRoom = waitingRoom;
		}
		Customer currentCustomer = null;
		boolean isAnEmployedBarber = true;
		@Override
		public void run() {
			while(isAnEmployedBarber)
			{
				if(currentCustomer == null)
				{
					try {
						synchronized (this) {
							this.wait(); //aka go to sleep
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				//I'm awake! must be a customer
				System.out.println("Barber " + name + " is cutting the hair of " + currentCustomer.name);
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if(waitingRoom.isEmpty())
				{
					try {
						System.out.println("Barber " + name + " is going to sleep!");
						synchronized (this) {
							this.wait();
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				currentCustomer = waitingRoom.remove(0);
			}
		}

		public void wakeUp(Customer newCustomer)
		{
			currentCustomer = newCustomer;
		}
	}
	static class Customer implements Runnable{
		String name;
		public Customer(String name)
		{
			this.name = name;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
		}
	}
	public static void main(String[] args) {
		//You implement the entire thing.
		List<Customer> waitingRoom = new LinkedList<SleepingBarber.Customer>();
		Barber barber = new Barber("Giovanni", waitingRoom);
		barber.currentCustomer = new Customer("Default Customer");
		Thread barberThread = new Thread(barber);
		barberThread.start();
		for(int i = 0; i < 100000; i++)
		{
			Customer c = new Customer("Customer " + i);
			if(barber.currentCustomer != null)
				waitingRoom.add(c); //barber is busy!
			else
			{
				//wake up stupid sleeping barber
				barber.currentCustomer = c;
				synchronized (barber) {
					barber.notify();
				}
			}
		}
	}
}
