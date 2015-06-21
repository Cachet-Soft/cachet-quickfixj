package jp.co.cachet.quickfix.util;

import java.util.TimeZone;

public class SafeDateFormat {
	static int EPOC_DAYS = getFairfieldDays(1970, 1, 1);
	static long TZ_OFFSET = TimeZone.getDefault().getRawOffset();

	public static int getFairfieldDays(int year, int month, int day) {
		if (month < 3) {
			year--;
			month += 12;
		}
		return 365 * year + (year / 4) - (year / 100) + (year / 400) + (306 * (month + 1) / 10) + day - 428;
	}

	public static int parseInt(String source, int beginIndex, int endIndex) {
		int value = 0;
		for (; beginIndex < endIndex; beginIndex++) {
			char c = source.charAt(beginIndex);
			if (c < '0' || c > '9') {
				throw new NumberFormatException(source);
			}
			value = value * 10 + c - '0';
		}
		return value;
	}

	public long parse(String source) {
		int year = parseInt(source, 0, 4);
		int month = parseInt(source, 4, 6);
		int day = parseInt(source, 6, 8);
		int fairFieldDays = getFairfieldDays(year, month, day);
		return (fairFieldDays - EPOC_DAYS) * 86400000L - TZ_OFFSET;
	}

	public String format(long date) {
		return format(date, new StringBuilder()).toString();
	}

	public StringBuilder format(long date, StringBuilder toAppendTo) {
		date += TZ_OFFSET;

		int fairfieldDays = (int) (date / 86400000L) + EPOC_DAYS;
		int year = fairfieldDays / 365;
		int begginingOfYear = getFairfieldDays(year, 3, 1);
		if (begginingOfYear > fairfieldDays) {
			year--;
			begginingOfYear = getFairfieldDays(year, 3, 1);
		}
		int daysFromYear = fairfieldDays - begginingOfYear;
		int month = daysFromYear / 30 + 3;
		int begginingOfMonth = getFairfieldDays(year, month, 1);
		if (getFairfieldDays(year, month, 1) > fairfieldDays) {
			month--;
			begginingOfMonth = getFairfieldDays(year, month, 1);
		}
		int day = fairfieldDays - begginingOfMonth + 1;

		if (month > 12) {
			year++;
			month -= 12;
		}
		toAppendTo.append(year);
		if (month < 10) {
			toAppendTo.append('0');
		}
		toAppendTo.append(month);
		if (day < 10) {
			toAppendTo.append('0');
		}
		toAppendTo.append(day);

		return toAppendTo;
	}

}
