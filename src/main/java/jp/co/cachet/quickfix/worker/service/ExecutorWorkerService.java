package jp.co.cachet.quickfix.worker.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jp.co.cachet.quickfix.worker.Worker;
import jp.co.cachet.quickfix.worker.WorkerService;

public class ExecutorWorkerService implements WorkerService {
	private final ExecutorService executorService;
	private final int threads;
	
	public ExecutorWorkerService() {
		this(Runtime.getRuntime().availableProcessors());
	}
	
	public ExecutorWorkerService(int threads) {
		this.threads = threads;
		executorService = Executors.newFixedThreadPool(threads);
	}
	
	public void submit(Worker worker) {
		executorService.submit(worker);
	}

	public int getThreads() {
		return threads;
	}

}
