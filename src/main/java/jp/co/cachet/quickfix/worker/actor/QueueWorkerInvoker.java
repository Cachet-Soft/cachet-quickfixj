package jp.co.cachet.quickfix.worker.actor;

import java.util.concurrent.atomic.AtomicLong;

import jp.co.cachet.quickfix.worker.WorkerService;

public abstract class QueueWorkerInvoker<T> implements QueueWorker.WorkerInvoker {
	private final AtomicLong current = new AtomicLong(-1);
	private final AtomicLong next = new AtomicLong(0);

	protected final WorkerService workerService;
	private final QueueWorker<?>[] ringBuffer;
	private final int mask;

	public QueueWorkerInvoker(WorkerService workerService) {
		this.workerService = workerService;
		int capacity = calcCapacity(workerService.getThreads());
		this.ringBuffer = new QueueWorker<?>[capacity];
		this.mask = capacity - 1;
		for (int i = 0; i < capacity; i++) {
			ringBuffer[i] = newWorker();
		}
	}

	@SuppressWarnings("unchecked")
	public void submit(T item) {
		final long index = next.get();
		QueueWorker<?> worker = index > current.get() ? ringBuffer[(int) current.incrementAndGet() & mask]
				: ringBuffer[(int) index & mask];
		((QueueWorker<T>) worker).submit(item);
	}

	public void onRun() {
		final long index = next.get();
		ringBuffer[(int) index & mask].clear();
		next.incrementAndGet();
	}

	private int calcCapacity(int nThreads) {
		final int expectedCapacity = nThreads + Runtime.getRuntime().availableProcessors();
		int capacity = 1;
		for (; capacity < expectedCapacity; capacity *= 2) {
		}
		return capacity;
	}

	protected abstract QueueWorker<T> newWorker();
}
