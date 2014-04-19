package jp.co.cachet.quickfix.util;

public interface Factory<T, V> {
	T getInstance(V arg);
}
