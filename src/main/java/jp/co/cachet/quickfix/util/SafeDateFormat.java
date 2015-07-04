package jp.co.cachet.quickfix.util;

import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class SafeDateFormat {
	static final int EPOC_DAYS = getFairfieldDays(1970, 1, 1);

	static final String PATTERN_CHARS = "yMdHmsS";
	static final int PATTERN_YEAR = 0;
	static final int PATTERN_MONTH = 1;
	static final int PATTERN_DAY = 2;
	static final int PATTERN_HOURS = 3;
	static final int PATTERN_MINUTES = 4;
	static final int PATTERN_SECONDS = 5;
	static final int PATTERN_MILLIS = 6;

	static final int[] MONTH_DAYS = new int[] { 0, 0, 0, 0, 31, 61, 92, 122, 153, 184, 214, 245, 275, 306, 337, 366 };

	private static final Map<String, Map<TimeZone, SafeDateFormat>> instanceCache = new HashMap<String, Map<TimeZone, SafeDateFormat>>();

	public static SafeDateFormat getInstance(String pattern) {
		return getInstance(pattern, TimeZone.getDefault());
	}

	public static SafeDateFormat getInstance(String pattern, TimeZone timeZone) {
		Map<TimeZone, SafeDateFormat> map = instanceCache.get(pattern);
		if (map == null) {
			map = new HashMap<TimeZone, SafeDateFormat>();
			instanceCache.put(pattern, map);
		}
		SafeDateFormat instance = map.get(timeZone);
		if (instance == null) {
			instance = new SafeDateFormat(pattern, timeZone);
			map.put(timeZone, instance);
		}
		return instance;
	}

	public static int getFairfieldDays(int year, int month, int day) {
		if (month < 3) {
			year--;
			month += 12;
		}
		return 365 * year + (year / 4) - (year / 100) + (year / 400) + (306 * (month + 1) / 10) + day - 428;
	}

	public static int parseInt(CharSequence source, int beginIndex, int endIndex) {
		int value = 0;
		for (; beginIndex < endIndex; beginIndex++) {
			char c = source.charAt(beginIndex);
			if (c < '0' || c > '9') {
				throw new NumberFormatException(
						String.format("%s beginIndex=%s endIndex=%d", source, beginIndex, endIndex));
			}
			value = value * 10 + c - '0';
		}
		return value;
	}

	private final TimeZone timeZone;
	private final long timeZoneOffset;
	private final boolean useDaylightTime;
	private final char[] compiledPattern;
	private final int length;
	private final boolean needFairfieldDays;

	private final int thisYear;
	private final long begginingOfThisYear;

	protected SafeDateFormat(String pattern) {
		this(pattern, TimeZone.getDefault());
	}

	protected SafeDateFormat(String pattern, TimeZone timeZone) {
		this.timeZone = timeZone;
		timeZoneOffset = timeZone.getRawOffset();
		useDaylightTime = timeZone.useDaylightTime();
		compiledPattern = compile(pattern);
		length = compiledPattern.length;
		boolean _needFairfieldDays = false;
		for (char c : compiledPattern) {
			if (c <= PATTERN_DAY) {
				_needFairfieldDays = true;
				break;
			}
		}
		needFairfieldDays = _needFairfieldDays;

		int fairfieldDays = (int) (System.currentTimeMillis() / 86400000L) + EPOC_DAYS;
		thisYear = fairfieldDays / 365;
		begginingOfThisYear = getFairfieldDays(thisYear, 1, 1) * 86400000L;
	}

	private char[] compile(String pattern) {
		int length = pattern.length();
		StringBuilder sb = new StringBuilder(length);
		int lastCode = -1;
		for (int i = 0; i < length; i++) {
			char c = pattern.charAt(i);
			int code = PATTERN_CHARS.indexOf(c);
			if (lastCode >= 0 && lastCode != code) {
				sb.append((char) lastCode);
			} else if (c >= PATTERN_CHARS.length() && c < ' ') {
				throw new IllegalArgumentException("'" + c + "'");
			}
			if (code < 0) {
				sb.append(c);
			}
			lastCode = code;
		}
		if (lastCode >= 0) {
			sb.append((char) lastCode);
		}

		int len = sb.length();
		char[] dst = new char[len];
		sb.getChars(0, len, dst, 0);
		return dst;
	}

	public long parse(CharSequence source) {
		int year = 1970;
		int month = 1;
		int day = 1;
		int hours = 0;
		int minutes = 0;
		int seconds = 0;
		int millis = 0;

		int pos = 0;
		for (char c : compiledPattern) {
			switch (c) {
			case PATTERN_YEAR:
				year = parseInt(source, pos, pos + 4);
				pos += 4;
				break;
			case PATTERN_MONTH:
				month = parseInt(source, pos, pos + 2);
				pos += 2;
				break;
			case PATTERN_DAY:
				day = parseInt(source, pos, pos + 2);
				pos += 2;
				break;
			case PATTERN_HOURS:
				hours = parseInt(source, pos, pos + 2);
				pos += 2;
				break;
			case PATTERN_MINUTES:
				minutes = parseInt(source, pos, pos + 2);
				pos += 2;
				break;
			case PATTERN_SECONDS:
				seconds = parseInt(source, pos, pos + 2);
				pos += 2;
				break;
			case PATTERN_MILLIS:
				millis = parseInt(source, pos, pos + 3);
				pos += 3;
				break;
			default:
				pos++;
				break;
			}
		}

		int fairfieldDays = getFairfieldDays(year, month, day);
		int milliseconds = (int) (hours * 3600000L + minutes * 60000L + seconds * 1000L + millis);
		if (!useDaylightTime && year >= thisYear) {
			return (fairfieldDays - EPOC_DAYS) * 86400000L + milliseconds - timeZoneOffset;
		}
		int dayOfWeek = fairfieldDays % 7;
		return (fairfieldDays - EPOC_DAYS) * 86400000L + milliseconds
				- timeZone.getOffset(1, year, month - 1, day, dayOfWeek == 0 ? 7 : dayOfWeek, milliseconds);
	}

	public String format(long date) {
		return format(date, new StringBuilder(length)).toString();
	}

	public StringBuilder format(long date, StringBuilder toAppendTo) {
		if (!useDaylightTime && date >= begginingOfThisYear) {
			date += timeZoneOffset + EPOC_DAYS * 86400000L;
		} else {
			date += timeZone.getOffset(date) + EPOC_DAYS * 86400000L;
		}

		int year = 1970;
		int month = 1;
		int day = 1;

		if (needFairfieldDays) {
			int fairfieldDays = (int) (date / 86400000L);
			year = fairfieldDays / 365;
			int begginingOfYear = getFairfieldDays(year, 3, 1);
			if (begginingOfYear > fairfieldDays) {
				year--;
				begginingOfYear = getFairfieldDays(year, 3, 1);
			}
			int daysFromYear = fairfieldDays - begginingOfYear;
			month = daysFromYear / 30 + 3;
			// 配列アクセスより計算の方が速いようだ。。。
			// int begginingOfMonth = MONTH_DAYS[month];
			// if (begginingOfMonth > daysFromYear) {
			// month--;
			// begginingOfMonth = MONTH_DAYS[month];
			// }
			// int day = daysFromYear - begginingOfMonth + 1;
			int begginingOfMonth = getFairfieldDays(year, month, 1);
			if (begginingOfMonth > fairfieldDays) {
				month--;
				begginingOfMonth = getFairfieldDays(year, month, 1);
			}
			day = fairfieldDays - begginingOfMonth + 1;
			if (month > 12) {
				year++;
				month -= 12;
			}
		}

		int times = (int) (date % 86400000L);
		int hours = times / 3600000;
		times %= 3600000;
		int minutes = times / 60000;
		times %= 60000;
		int seconds = times / 1000;
		int millis = times % 1000;

		for (char c : compiledPattern) {
			switch (c) {
			case PATTERN_YEAR:
				toAppendTo.append(year);
				break;
			case PATTERN_MONTH:
				appendTwo(month, toAppendTo);
				break;
			case PATTERN_DAY:
				appendTwo(day, toAppendTo);
				break;
			case PATTERN_HOURS:
				appendTwo(hours, toAppendTo);
				break;
			case PATTERN_MINUTES:
				appendTwo(minutes, toAppendTo);
				break;
			case PATTERN_SECONDS:
				appendTwo(seconds, toAppendTo);
				break;
			case PATTERN_MILLIS:
				appendThree(millis, toAppendTo);
				break;
			default:
				toAppendTo.append(c);
				break;
			}
		}

		return toAppendTo;
	}

	private void appendTwo(int dateElement, StringBuilder toAppendTo) {
		if (dateElement < 10) {
			toAppendTo.append('0');
		}
		toAppendTo.append(dateElement);
	}

	private void appendThree(int dateElement, StringBuilder toAppendTo) {
		if (dateElement < 100) {
			toAppendTo.append("00");
		} else if (dateElement < 10) {
			toAppendTo.append('0');
		}
		toAppendTo.append(dateElement);
	}

}
