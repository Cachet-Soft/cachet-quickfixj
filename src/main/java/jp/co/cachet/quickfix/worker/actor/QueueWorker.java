package jp.co.cachet.quickfix.worker.actor;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import jp.co.cachet.quickfix.worker.Worker;
import jp.co.cachet.quickfix.worker.WorkerService;

public abstract class QueueWorker<T> implements Worker {
	public interface WorkerInvoker {
		void onRun();
	}
	
	private final Queue<T> queue = new LinkedBlockingQueue<T>();
	private final AtomicInteger counter = new AtomicInteger(0);
	
	private final WorkerService workerService;
	private final WorkerInvoker workerInvoker;
	
	private boolean done = false;

	public QueueWorker(WorkerService workerService, WorkerInvoker workerInvoker) {
		this.workerService = workerService;
		this.workerInvoker = workerInvoker;
	}
	
	public void submit(T item) {
		queue.add(item);
		if (counter.incrementAndGet() == 1) {
			workerService.submit(this);
		}
	}
	
	protected abstract void process(T item);
	
	public void run() {
		if (!done) {
			workerInvoker.onRun();
		}
		T item = null;
		while ((item = queue.poll()) != null) {
			process(item);
		}
		done = true;
		counter.set(0);
	}
	
	/* package */ void clear() {
		if (done) {
			done = false;
			counter.set(0);
			if (!queue.isEmpty()) {
				throw new IllegalStateException();
			}
		}
	}

}
