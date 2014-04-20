package jp.co.cachet.quickfix.workerservice;

import jp.co.cachet.quickfix.util.Factory;

public class Workers implements Factory<WorkerService, ServiceType> {

	public WorkerService getInstance(ServiceType serviceType) {
		switch (serviceType) {
		case DISRUPTOR:
			throw new UnsupportedOperationException("Not implemented yet");
		case EXECUTOR_SERVICE:
			default:
			return null;
		}
	}

}
