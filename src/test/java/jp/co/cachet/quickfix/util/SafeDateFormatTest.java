package jp.co.cachet.quickfix.util;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

public class SafeDateFormatTest {
	private static final SafeDateFormat safeDateFormat = SafeDateFormat.getInstance("yyyy/MM/dd-HH:mm:ss.SSS");
	private static final ThreadLocal<SimpleDateFormat> simpleDateFormat = new ThreadLocal<SimpleDateFormat>() {
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss.SSS");
		}
	};

	private static String getDateStr() {
		return safeDateFormat.format(System.currentTimeMillis());
	}

	@Test
	public void test() throws ParseException {
		// Fairfieldの公式ベースの確認
		for (String timeZoneId : TimeZone.getAvailableIDs()) {
			if (timeZoneId.contains("/")) {
				assertFairfieldDays("yyyyMMdd", TimeZone.getTimeZone(timeZoneId), false);
			}
		}

		// TimeZone確認
		for (String timeZoneId : new String[] { "UTC", "GMT+9", "JST", "America/New_York" }) {
			String pattern = "yyyy/MM/dd-HH:mm:ss.SSS";
			TimeZone timeZone = TimeZone.getTimeZone(timeZoneId);
			SimpleDateFormat sdf = new SimpleDateFormat(pattern);
			sdf.setTimeZone(timeZone);
			SafeDateFormat safe = SafeDateFormat.getInstance(pattern, timeZone);

			String date = sdf.format(new Date());
			long msDate = sdf.parse(date).getTime();
			assertEquals(timeZoneId, msDate, safe.parse(date));
			assertEquals(timeZoneId, date, safe.format(msDate));
		}
	}

	/**
	 * Fairfieldの公式の確認。
	 * 
	 * @param pattern
	 * @param timeZone
	 * @param failsOnAssert
	 * @throws ParseException
	 */
	private void assertFairfieldDays(String pattern, TimeZone timeZone, boolean failsOnAssert) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		sdf.setTimeZone(timeZone);
		SafeDateFormat safe = SafeDateFormat.getInstance(pattern, timeZone);

		boolean passed = true;
		StringBuilder failedDates = new StringBuilder();
		String minYear = null;
		String maxYear = null;
		long ms = 0;
		for (int i = -100000; i < 100000; i++) {
			ms = 86400000L * i;
			String date = sdf.format(new Date(ms));
			long msDate = sdf.parse(date).getTime();
			if (failsOnAssert) {
				assertEquals(msDate, safe.parse(date));
				assertEquals(date, safe.format(msDate));
			}
			if (msDate != safe.parse(date) || !date.equals(safe.format(msDate))) {
				passed = false;
				failedDates.append(date).append(',');
			}

			maxYear = date.substring(0, 4);
			if (minYear == null) {
				minYear = maxYear;
			}
		}

		if (passed) {
			System.out.printf("passed %s from %s year till %s year\n", timeZone.getID(), minYear, maxYear);
		} else {
			failedDates.setLength(failedDates.length() - 1);
			System.out.printf("FAILED! %s %s\n", timeZone.getID(), failedDates.toString());
		}
	}

	@Test
	public void launchBenchmark() throws Exception {
		Options opt = new OptionsBuilder()
				// Specify which benchmarks to run.
				// You can be more specific if you'd like to run only one
				// benchmark per test.
				.include(this.getClass().getName() + ".*")
				// Set the following options as needed
				.mode(Mode.AverageTime)
				.timeUnit(TimeUnit.NANOSECONDS)
				.warmupTime(TimeValue.seconds(1))
				.warmupIterations(3)
				.measurementTime(TimeValue.seconds(1))
				.measurementIterations(3)
				.threads(2)
				.forks(1)
				.shouldFailOnError(true)
				.shouldDoGC(true)
				// .jvmArgs("-XX:+UnlockDiagnosticVMOptions",
				// "-XX:+PrintInlining")
				// .addProfiler(WinPerfAsmProfiler.class)
				.build();

		new Runner(opt).run();
	}

	@Benchmark
	public void formatOfSafeDateFormat() {
		safeDateFormat.format(System.currentTimeMillis());
	}

	@Benchmark
	public void formatOfSimpleDateFormat() {
		simpleDateFormat.get().format(new Date());
	}

	@Benchmark
	public void parseOfSafeDateFormat() {
		safeDateFormat.parse(getDateStr());
	}

	@Benchmark
	public void parseOfSimpleDateFormat() throws ParseException {
		simpleDateFormat.get().parse(getDateStr());
	}

}
