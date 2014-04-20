package jp.co.cachet.quickfix.workerservice;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jp.co.cachet.quickfix.worker.Worker;

public class ExecutorWorkerService implements WorkerService {
	private final ExecutorService executorService;
	
	public ExecutorWorkerService() {
		this(Runtime.getRuntime().availableProcessors());
	}
	
	public ExecutorWorkerService(int nThreads) {
		executorService = Executors.newFixedThreadPool(nThreads);
	}
	
	public void submit(Worker worker) {
		executorService.submit(worker);
	}

}
