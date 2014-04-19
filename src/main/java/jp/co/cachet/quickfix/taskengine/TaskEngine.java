package jp.co.cachet.quickfix.taskengine;

import jp.co.cachet.quickfix.task.Task;

public interface TaskEngine {
	void submit(Task task);
}
