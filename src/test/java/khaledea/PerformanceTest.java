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
public class PerformanceTest  {
	static int QUEUE_SIZES = 512;
	static int TEST_SIZE = 512*512*16;
	/**
	* Test atomic index queue and measure its latency
	*/
	@Test
	public void profileBoth() throws InterruptedException{
		ArrayList<Integer> threadNumbers = new ArrayList<>(Arrays.asList(1, 2, 4, 8, 16));
		ArrayList<Integer> queueSizes = new ArrayList<>(Arrays.asList(4, 8, 16, 32, 64, 128, 512, 1024, 2*1024, 4*1024, 8*1024, 16*1024));
		ArrayList<Integer> testSizes = new ArrayList<>(Arrays.asList(1024, 2*1024, 4*1024, 8*1024, 16*1024, 32*1024, 64*1024, 128*1024, 512*1024, 1024*1024, 2*1024*1024, 4*1024*1024, 8*1024*1024, 16*1024*1024));
		for (Integer testSize: testSizes) {
			System.out.println("Test size:" + testSize);
			for (Integer queueSize: queueSizes) {
				if (queueSize > testSize) {
					continue;
				}
				for (Integer threadNum: threadNumbers) {
					double atomicLatencyAverage =0.0;
					double blockingLatencyAverage =0.0;
					for (int i=0; i<100; i++){
						atomicLatencyAverage += queueTest(testSize, queueSize, threadNum, "Atomic");
						blockingLatencyAverage += queueTest(testSize, queueSize, threadNum, "Blocking");
					}
					atomicLatencyAverage = atomicLatencyAverage/100;
					blockingLatencyAverage = blockingLatencyAverage/100;
					System.out.println("Atomic:(queueSize, threadNumber, latency): (" + queueSize + ", " + threadNum + ", " + atomicLatencyAverage + ")");
					System.out.println("Blocking:(queueSize, threadNumber, latency): (" + queueSize + ", " + threadNum + ", " + blockingLatencyAverage + ")");
				}
			}
		}
	}

	public Long queueTest(Integer testSize, Integer queueSize, Integer threadNum, String queueType) throws InterruptedException{
		Object queue;
		if (queueType.equals("Atomic")){
			 queue = new AtomicIndexQueue<>(queueSize);
		} else {
			queue = new ArrayBlockingQueue<>(queueSize);
		}
		
		int max = testSize;
		int skip = threadNum;
		long startTime = System.currentTimeMillis();
		ConsumerThread c = new ConsumerThread(queue, max);
		c.start();
		Thread[] threads = new Thread[threadNum];
		for (int i = 0; i < threadNum; i++) {
			threads[i] = new ProducerThread(i, skip, max, queue);
			threads[i].start();
		}
		for (Thread thread : threads) {
			thread.join();
		}
		c.join();
		ArrayList<Integer> consumed = c.getConsumed();
		long endTime = System.currentTimeMillis();
		// System.out.println("consumed:" + consumed);
		consumed.sort((x, y) -> x.compareTo(y));
		boolean match = true;
		for(int i = 0; i < max; i+=skip) {
			if (!consumed.get(i).equals(i)){
				match = false;
				System.out.println("i:" + i);
				System.out.println("consumed.get(i):" + consumed.get(i));
				break;
			}
		}
		assertTrue("Produced values were not all consumed correctly", match);
		return endTime-startTime;
	}

}
