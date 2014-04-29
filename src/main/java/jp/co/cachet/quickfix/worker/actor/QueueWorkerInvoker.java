package jp.co.cachet.quickfix.worker.actor;

import java.util.concurrent.atomic.AtomicLong;

import jp.co.cachet.quickfix.worker.WorkerService;

public abstract class QueueWorkerInvoker<T> implements QueueWorker.WorkerInvoker {
	private final AtomicLong current = new AtomicLong(0);
	private final AtomicLong next = new AtomicLong(1);

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
		// 現在スロットが次スロットに追いつくまで、現在スロットを返します。
		final long currentIndex = current.get();
		QueueWorker<?> worker = null;
		if (next.get() > currentIndex) {
			worker = ringBuffer[(int) currentIndex & mask];
		} else {
			worker = nextWorker();
		}
		((QueueWorker<T>) worker).submit(item);
	}

	private QueueWorker<?> nextWorker() {
		QueueWorker<?> worker = ringBuffer[(int) next.incrementAndGet() & mask];
		worker.clear();
		return worker;
	}

	public void onRun() {
		// 現在スロットをひとつ進めます
		current.incrementAndGet();
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
