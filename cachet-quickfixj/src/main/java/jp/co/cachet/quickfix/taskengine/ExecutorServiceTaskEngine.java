package jp.co.cachet.quickfix.taskengine;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jp.co.cachet.quickfix.task.Task;

public class ExecutorServiceTaskEngine implements TaskEngine {
	private final ExecutorService executorService;
	
	public ExecutorServiceTaskEngine() {
		this(Runtime.getRuntime().availableProcessors());
	}
	
	public ExecutorServiceTaskEngine(int nThreads) {
		executorService = Executors.newFixedThreadPool(nThreads);
	}
	
	public void submit(Task task) {
		executorService.submit(task);
	}

}
