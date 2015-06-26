package jp.co.cachet.quickfix.util;

import java.util.HashMap;
import java.util.Map;

public class SafeDecimalFormat {
	static final Map<String, SafeDecimalFormat> instanceCache = new HashMap<String, SafeDecimalFormat>();

	public static SafeDecimalFormat getInstance(String pattern) {
		SafeDecimalFormat instance = instanceCache.get(pattern);
		if (instance == null) {
			instance = new SafeDecimalFormat(pattern);
			instanceCache.put(pattern, instance);
		}
		return instance;
	}

	protected SafeDecimalFormat(String pattern) {

	}

	public String format(double number) {
		return format(number, new StringBuilder()).toString();
	}

	public StringBuilder format(double d, StringBuilder builder) {
		if (d < 0) {
			builder.append('-');
			d = -d;
		}
		long scaled = (long) (d * 1e6 + 0.5);
		long factor = 1000000;
		int scale = 7;
		while (factor * 10 <= scaled) {
			factor *= 10;
			scale++;
		}
		while (scale > 0) {
			if (scale == 6)
				builder.append('.');
			long c = scaled / factor % 10;
			factor /= 10;
			builder.append((char) ('0' + c));
			scale--;
		}
		return builder;
	}

}
