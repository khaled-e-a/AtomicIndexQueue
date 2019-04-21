package khaledea;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;

import org.junit.Test;

/**
* Unit test for AtomicIndexQueue.
*/
public class CorrectnessTest  {
	public static final int QUEUE_SIZES = 2;
	public static final int TEST_SIZE = 512*2;
	

	@Test
	public void atomicIndexQueueTest() throws InterruptedException{
		AtomicIndexQueue<Integer> atomicIndexQueue = new AtomicIndexQueue<>(QUEUE_SIZES);
		int max = TEST_SIZE;
		int skip = 4;
		long startTime = System.currentTimeMillis();
		ConsumerThread c = new ConsumerThread(atomicIndexQueue, max);
		ProducerThread p1 = new ProducerThread(0, skip, max, atomicIndexQueue);
		ProducerThread p2 = new ProducerThread(1, skip, max, atomicIndexQueue);
		ProducerThread p3 = new ProducerThread(2, skip, max, atomicIndexQueue);
		ProducerThread p4 = new ProducerThread(3, skip, max, atomicIndexQueue);
		c.start();
		p1.start();
		p2.start();
		p3.start();
		p4.start();
		p1.join();
		p2.join();
		p3.join();
		p4.join();
		c.join();
		ArrayList<Integer> consumed = c.getConsumed();
		long endTime = System.currentTimeMillis();
		consumed.sort((x, y) -> x.compareTo(y));
		boolean match = true;
		for(int i = 0; i < max; i+=skip) {
			if (!consumed.get(i).equals(i)){
				match = false;
				break;
			}
		}
		System.out.println("Elapsed time in atomicIndexQueueTest is " + ((Long)((endTime-startTime))).toString() + " ms");
		assertTrue("Produced values were not all consumed correctly", match);
	}

	@Test
	public void arrayBlockingQueueTest() throws InterruptedException{
		ArrayBlockingQueue<Integer> arrayBlockingQueue = new ArrayBlockingQueue<>(QUEUE_SIZES);
		int max = TEST_SIZE;
		int skip = 4;
		long startTime = System.currentTimeMillis();
		ConsumerThread c = new ConsumerThread(arrayBlockingQueue, max);
		ProducerThread p1 = new ProducerThread(0, skip, max, arrayBlockingQueue);
		ProducerThread p2 = new ProducerThread(1, skip, max, arrayBlockingQueue);
		ProducerThread p3 = new ProducerThread(2, skip, max, arrayBlockingQueue);
		ProducerThread p4 = new ProducerThread(3, skip, max, arrayBlockingQueue);
		c.start();
		p1.start();
		p2.start();
		p3.start();
		p4.start();
		p1.join();
		p2.join();
		p3.join();
		p4.join();
		c.join();
		ArrayList<Integer> consumed = c.getConsumed();
		c.interrupt();
		long endTime = System.currentTimeMillis();
		consumed.sort((x, y) -> x.compareTo(y));
		boolean match = true;
		for(int i = 0; i < max; i+=skip) {
			if (!consumed.get(i).equals(i)){
				match = false;
				break;
			}
		}
		System.out.println("Elapsed time in arrayBlockingQueueTest is " + ((Long)((endTime-startTime))).toString() + " ms");
		assertTrue("Produced values were not all consumed correctly", match);
	}




}


class ProducerThread extends Thread{ 
	private int id;
	private int skip;
	private int max;
	private AtomicIndexQueue<Integer> atomicIndexQueue;
	private ArrayBlockingQueue<Integer> arrayBlockingQueue;
	ProducerThread(int id, int skip, int max, Object queue) {
		this.id = id;
		this.skip = skip;
		this.max = max;
		if (queue instanceof AtomicIndexQueue<?>) {
			this.atomicIndexQueue = (AtomicIndexQueue<Integer>) queue;
		} else if (queue instanceof ArrayBlockingQueue<?>) {
			this.arrayBlockingQueue = (ArrayBlockingQueue<Integer>) queue;
		} else {
			throw new IllegalArgumentException("Must use an AtomicIndexQueue or an ArrayBlockingQueue");
		}
	}

	@Override
	public void run(){
		if (atomicIndexQueue != null) {
			for(int i = id; i < max; i+=skip) {
				atomicIndexQueue.put(i);
			}
		} else if (arrayBlockingQueue != null) {
			try {
				for(int i = id; i < max; i+=skip) {
					arrayBlockingQueue.put(i);
				}
			} catch (InterruptedException e) {
				throw new Error("Interrrupted excpetion");
			}
		}	
	}
}

class ConsumerThread extends Thread{ 
	private AtomicIndexQueue<Integer> atomicIndexQueue;
	private ArrayBlockingQueue<Integer> arrayBlockingQueue;
	private ArrayList<Integer> consumed;
	private int max;
	ConsumerThread(Object queue, Integer max) {
		if (queue instanceof AtomicIndexQueue<?>) {
			this.atomicIndexQueue = (AtomicIndexQueue<Integer>) queue;
		} else if (queue instanceof ArrayBlockingQueue<?>) {
			this.arrayBlockingQueue = (ArrayBlockingQueue<Integer>) queue;
		} else {
			throw new IllegalArgumentException("Must use an AtomicIndexQueue or an ArrayBlockingQueue");
		}
		this.consumed = new ArrayList<>();
		this.max = max;
	}
	public void run(){
		if (atomicIndexQueue != null) {
			while(true) {
				if (atomicIndexQueue.isFull()) {
					consumed.addAll(atomicIndexQueue.drain());
				}
				if(consumed.size()==max) {
					break;
				}
			}
		} else if (arrayBlockingQueue != null) { 
			while(true) {
				if (arrayBlockingQueue.remainingCapacity() == 0) {
					arrayBlockingQueue.drainTo(consumed);
				}
				if(consumed.size()==max) {
					break;
				}
			}
		}
	}

	public ArrayList<Integer> getConsumed() {
		return consumed;
	}

}