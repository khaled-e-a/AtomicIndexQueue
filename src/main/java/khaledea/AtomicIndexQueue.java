package khaledea;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class AtomicIndexQueue<T> {

	private int size;
	private Object[] queue;
	public AtomicInteger queueIndex;

	/**
	 * 
	 * @param size maximum size of the queue
	 */
	public AtomicIndexQueue(int size) {
		this.size = size;
		queue = new Object[size];
		queueIndex = new AtomicInteger();
		queueIndex.set(0);
	}

	public void put(T value) {
		while (true) {
			synchronized (queueIndex){
				if (queueIndex.get() < size) {
					queue[queueIndex.get()] = value;
					queueIndex.incrementAndGet();
					break;
				}
			}
		}
		return;
	}
	/**
	 * 
	 * @return true if the queue is full
	 */
	public boolean isFull(){
		return 	queueIndex.get() == size;
	}

	/**
	 * 
	 * @return an ArrayList<T> of the contents of the queue and resets the queue
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<T> drain(){
		ArrayList<Object> al;
		al = new ArrayList<>(Arrays.asList(queue));
		queueIndex.set(0);
		ArrayList<T> r = new ArrayList<>();
		for (Object o: al) {
			r.add((T) o);
		}
		return r;
	}

	/**
	 * 
	 * @return an ArrayList<T> of the contents of the queue
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<T> toArrayList(){
		ArrayList<Object> al = new ArrayList<>(Arrays.asList(queue));
		ArrayList<T> r = new ArrayList<>();
		for (Object o: al) {
			r.add((T) o);
		}
		return r;
	}
}