package edu.gmu.swe622.lab3;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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

	static class Barber implements Runnable {
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
			while (isAnEmployedBarber) {
				lockToEnterOrCheckWaitingRoom.lock();
				if (currentCustomer == null) {
					if (waitingRoom.isEmpty()) {
						try {
							System.out.println("Barber " + name + " is going to sleep!");
							lockToEnterOrCheckWaitingRoom.unlock();

							synchronized (this) {
								this.wait();
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					else
					{
						lockToEnterOrCheckWaitingRoom.unlock();
						currentCustomer = waitingRoom.remove(0);
					}
				}
				else
					lockToEnterOrCheckWaitingRoom.unlock();

				// I'm awake! must be a customer
				System.out.println("Barber " + name + " is cutting the hair of " + currentCustomer.name);
				//Remove current customer
				currentCustomer = null;
			}
		}

		public void wakeUp(Customer newCustomer) {
			currentCustomer = newCustomer;
		}
	}

	static class Customer implements Runnable {
		String name;

		public Customer(String name) {
			this.name = name;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub

		}
	}

	public static Lock lockToEnterOrCheckWaitingRoom = new ReentrantLock();

	public static void main(String[] args) {
		// You implement the entire thing.
		List<Customer> waitingRoom = Collections.synchronizedList(new LinkedList<SleepingBarber.Customer>());
		Barber barber = new Barber("Giovanni", waitingRoom);
		barber.currentCustomer = new Customer("Default Customer");
		Thread barberThread = new Thread(barber);
		barberThread.start();
		for (int j = 0; j < 10; j++) {
			final String customerNamePrefix = "Customer "+j+"-";
			Thread t = new Thread(new Runnable() {

				@Override
				public void run() {
					for (int i = 0; i < 1000; i++) {
						Customer c = new Customer(customerNamePrefix + i);
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						lockToEnterOrCheckWaitingRoom.lock();
						try {
							if (barber.currentCustomer != null)
							{
								waitingRoom.add(c); // barber is busy!
							}
							else {
								System.out.println("Waking up barber for " + c.name);
								// wake up stupid sleeping barber
								barber.currentCustomer = c;
								synchronized (barber) {
									barber.notify();
								}
							}
						} finally {
							lockToEnterOrCheckWaitingRoom.unlock();
						}
					}

				}
			});
			t.start();
		}
		try {
			barberThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
