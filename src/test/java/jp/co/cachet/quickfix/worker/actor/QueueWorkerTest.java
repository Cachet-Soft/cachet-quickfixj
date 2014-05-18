package jp.co.cachet.quickfix.worker.actor;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

import jp.co.cachet.quickfix.worker.WorkerService;
import jp.co.cachet.quickfix.worker.service.ExecutorWorkerService;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * QueueWorkerをテストします。<br/>
 * TODO: 実行漏れが起きているのを修正します。<br/>
 * 
 * @author masaaki
 * 
 */
public class QueueWorkerTest {
	public static AtomicLong total = new AtomicLong(0);

	@Before
	public void setUp() {
		total.set(0);
	}

	@Test
	public void testOneThread() {
		System.out.println("availableProcessors=" + Runtime.getRuntime().availableProcessors());
		test(1, 100000);
	}

	@Test
	public void testCoreNumThread() {
		test(Runtime.getRuntime().availableProcessors(), 100000);
	}

	// @Ignore("for latency test")
	@Test
	public void testManyThread() {
		test(Runtime.getRuntime().availableProcessors() * 10, 100000);
	}

	public void test(final int threads, final long count) {
		final long ns = System.nanoTime();

		QueueWorkerInvoker<Integer> workerInvoker = getWorkerInvoker(threads);
		Long expected = (1 + count) * count / 2;
		for (int i = 0; i <= count; i++) {
			workerInvoker.submit(i);
		}
		long current = total.get();
		int nTry = 0;
		while (true) {
			LockSupport.parkNanos(100 * 1000 * 1000);
			if (current == total.longValue()) {
				System.out.println("expected=" + expected + " actual=" + total);
				if (expected.equals(total) || ++nTry > 2) {
					break;
				}
			}
			current = total.get();
		}

		System.out.println(threads + " threads throughput(/s) =" + count * 1000000000 / (System.nanoTime() - ns));
		assertTrue("expected=" + expected + " actual=" + total, expected.equals(total.get()));
	}

	private QueueWorkerInvoker<Integer> getWorkerInvoker(int threads) {
		return new QueueWorkerInvoker<Integer>(new ExecutorWorkerService(threads)) {

			@Override
			protected QueueWorker<Integer> newWorker() {
				return new DummyWorker(workerService, this);
			}

		};
	}

	static class DummyWorker extends QueueWorker<Integer> {
		private static final Logger log = LoggerFactory.getLogger(DummyWorker.class);

		public DummyWorker(WorkerService workerService,
				jp.co.cachet.quickfix.worker.actor.QueueWorker.WorkerInvoker workerInvoker) {
			super(workerService, workerInvoker);
		}

		@Override
		protected void process(Integer item) {
			final long ns = System.nanoTime();
			log.info("{}", toString(ns));
			total.addAndGet(item);
			// Thread.yield();
			final long elapsed = System.nanoTime() - ns;
			if (100000 < elapsed) {
				System.err.println("latency has problem! ns=" + elapsed);
			}
		}

		private String toString(long begin) {
			for (long ns = begin + 1000; ns > System.nanoTime();) {

			}
			final long elapsed = System.nanoTime() - begin;
			return "latency ns=" + elapsed + (1000 < elapsed ? " !!! PROBLEM !!!" : "");
		}
	}
}
