package jp.co.cachet.quickfix.worker;

import jp.co.cachet.quickfix.util.Factory;

public enum Workers implements Factory<WorkerService, ServiceType> {

	INSTANCE;

	public WorkerService getInstance() {
		return getInstance(ServiceType.EXECUTOR_SERVICE);
	}
	
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
