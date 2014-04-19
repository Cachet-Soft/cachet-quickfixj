package jp.co.cachet.quickfix.taskengine;

import jp.co.cachet.quickfix.util.Factory;

public class TaskEngineFactory implements Factory<TaskEngine, TaskEngineType> {

	public TaskEngine getInstance(TaskEngineType engineType) {
		switch (engineType) {
		case DISRUPTOR:
			throw new UnsupportedOperationException("Not implemented yet");
		case EXECUTOR_SERVICE:
			default:
			return null;
		}
	}

}
