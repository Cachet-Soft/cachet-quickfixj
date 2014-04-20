package jp.co.cachet.quickfix.workerservice;

import jp.co.cachet.quickfix.worker.Worker;

public interface WorkerService {
	void submit(Worker worker);
}
