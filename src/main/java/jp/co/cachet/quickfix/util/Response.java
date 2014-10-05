package jp.co.cachet.quickfix.util;

public interface Response<T> {
	void onResponse(T arg);
}
