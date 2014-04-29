package jp.co.cachet.quickfix.worker.actor;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.locks.LockSupport;

import jp.co.cachet.quickfix.worker.WorkerService;
import jp.co.cachet.quickfix.worker.service.ExecutorWorkerService;

import org.junit.Before;
import org.junit.Test;

/**
 * QueueWorkerをテストします。<br/>
 * TODO: 実行漏れが起きているのを修正します。<br/>
 * 
 * @author masaaki
 * 
 */
public class QueueWorkerTest {
	public static Long total = 0L;

	public static void add(int value) {
		synchronized (total) {
			total += value;
		}
	}

	@Before
	public void setUp() {
		total = 0L;
	}

	@Test
	public void testOneThread() {
		System.out.println("availableProcessors=" + Runtime.getRuntime().availableProcessors());
		test(1, 10000);
	}

	@Test
	public void testCoreNumThread() {
		test(Runtime.getRuntime().availableProcessors(), 10000);
	}

	@Test
	public void testManyThread() {
		test(Runtime.getRuntime().availableProcessors() * 10, 1000000);
	}

	public void test(final int threads, final long count) {
		QueueWorkerInvoker<Integer> workerInvoker = getWorkerInvoker(threads);
		Long expected = (1 + count) * count / 2;
		for (int i = 0; i <= count; i++) {
			workerInvoker.submit(i);
		}
		long current = total;
		int nTry = 0;
		while (true) {
			LockSupport.parkNanos(100 * 1000 * 1000);
			if (current == total.longValue()) {
				System.out.println("expected=" + expected + " actual=" + total);
				if (expected.equals(total) || ++nTry > 2) {
					break;
				}
			}
			current = total;
		}
		assertTrue("expected=" + expected + " actual=" + total, expected.equals(total));
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

		public DummyWorker(WorkerService workerService,
				jp.co.cachet.quickfix.worker.actor.QueueWorker.WorkerInvoker workerInvoker) {
			super(workerService, workerInvoker);
		}

		@Override
		protected void process(Integer item) {
			System.out.println(item);
			QueueWorkerTest.add(item);
		}

	}
}
