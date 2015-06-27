package jp.co.cachet.quickfix.util;

import java.util.HashMap;
import java.util.Map;

public class SafeDecimalFormat {
	static final String PATTERN_CHARS = "0#,.";
	static final int PATTERN_ZERO_DIGIT = '0';
	static final int PATTERN_DIGIT = '#';
	static final int PATTERN_GROUP_SEPARATOR = ',';
	static final int PATTERN_DECIMAL_SEPARATOR = '.';

	static final Map<String, SafeDecimalFormat> instanceCache = new HashMap<String, SafeDecimalFormat>();

	public static SafeDecimalFormat getInstance(String pattern) {
		SafeDecimalFormat instance = instanceCache.get(pattern);
		if (instance == null) {
			instance = new SafeDecimalFormat(pattern);
			instanceCache.put(pattern, instance);
		}
		return instance;
	}

	private final char[] compiledPattern;
	private final int length;
	final long factor;
	final int scale;
	final int minimumFractionDigit;
	final int zeroPaddingScale;
	final int minimumIntegerDigit;

	protected SafeDecimalFormat(String pattern) {
		StringBuilder sb = new StringBuilder(pattern.length());
		boolean foundDecimalSeparator = false;
		int _scale = 0;
		int _minimumFractionDigit = 0;
		int _minimumIntegerDigit = 0;
		int lastCode = -1;
		for (int i = 0; i < pattern.length(); i++) {
			char c = pattern.charAt(i);
			int code = PATTERN_CHARS.indexOf(c);
			switch (c) {
			case PATTERN_ZERO_DIGIT:
				if (foundDecimalSeparator) {
					_scale++;
					_minimumFractionDigit++;
				} else {
					_minimumIntegerDigit++;
				}
				break;
			case PATTERN_DIGIT:
				if (foundDecimalSeparator) {
					_scale++;
				}
				break;
			case PATTERN_DECIMAL_SEPARATOR:
				foundDecimalSeparator = true;
				break;
			case PATTERN_GROUP_SEPARATOR:
				break;
			default:
				if (lastCode >= 0) {
					sb.append('\0');
				}
				sb.append(c);
				break;
			}
			lastCode = code;
		}
		if (lastCode >= 0) {
			sb.append('\0');
		}

		minimumFractionDigit = _minimumFractionDigit;
		zeroPaddingScale = _scale - _minimumFractionDigit;
		minimumIntegerDigit = _minimumIntegerDigit;
		scale = _scale;
		factor = (long) Math.pow(10, _scale);

		int len = sb.length();
		char[] dst = new char[len];
		sb.getChars(0, len, dst, 0);
		compiledPattern = dst;
		length = len + 20;

	}

	public String format(long number) {
		return format(number, new StringBuilder(length)).toString();
	}

	public StringBuilder format(long number, StringBuilder toAppendTo) {
		if (number < 0) {
			toAppendTo.append('-');
			number = -number;
		}

		for (char c : compiledPattern) {
			switch (c) {
			case '\0':
				subFormat(number, toAppendTo);
				break;
			default:
				toAppendTo.append(c);
				break;
			}
		}
		return toAppendTo;
	}

	public StringBuilder subFormat(long number, StringBuilder toAppendTo) {
		long _factor = 1;
		int _scale = 1;
		while (_factor * 10 <= number || _scale < minimumIntegerDigit) {
			_factor *= 10;
			_scale++;
		}
		while (_scale > 0) {
			long c = number / _factor % 10;
			toAppendTo.append((char) ('0' + c));
			_factor /= 10;
			_scale--;
		}
		if (minimumFractionDigit > 0) {
			toAppendTo.append('.');
			for (int i = 0; i < minimumFractionDigit; i++) {
				toAppendTo.append('0');
			}
		}
		return toAppendTo;
	}

	public String format(double number) {
		return format(number, new StringBuilder(length)).toString();
	}

	public StringBuilder format(double number, StringBuilder toAppendTo) {
		if (number < 0) {
			toAppendTo.append('-');
			number = -number;
		}

		for (char c : compiledPattern) {
			switch (c) {
			case '\0':
				subFormat(number, toAppendTo);
				break;
			default:
				toAppendTo.append(c);
				break;
			}
		}
		return toAppendTo;
	}

	public StringBuilder subFormat(double number, StringBuilder toAppendTo) {
		long unscaled = (long) (number * factor + 0.5);
		long _factor = factor;
		int _scale = scale + 1;
		int minimumIntegerScale = scale + minimumIntegerDigit;
		while (_factor * 10 <= unscaled || _scale < minimumIntegerScale) {
			_factor *= 10;
			_scale++;
		}
		while (_scale > 0) {
			if (_scale == scale) {
				toAppendTo.append('.');
			}
			long c = unscaled / _factor % 10;
			toAppendTo.append((char) ('0' + c));
			if (_scale <= zeroPaddingScale && unscaled % _factor == 0) {
				break; // 0埋め不要なので抜けます
			}
			_factor /= 10;
			_scale--;
		}
		return toAppendTo;
	}

}
