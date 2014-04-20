package jp.co.cachet.quickfix.worker;


public interface WorkerService {
	void submit(Worker worker);
	int getThreads();
}
