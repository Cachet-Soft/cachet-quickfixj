package jp.co.cachet.quickfix.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss.SSS");
		SafeDateFormat safe = SafeDateFormat.getInstance("yyyy/MM/dd-HH:mm:ss.SSS");

		String lastYear = "1970";
		long ms = 0;
		for (int i = 0; i < 100000; i++) {
			ms += 86400000;
			String date = sdf.format(new Date(ms));
			long msDate = sdf.parse(date).getTime();
			// parse確認
			assertEquals(msDate, safe.parse(date));
			// format確認
			assertEquals(date, safe.format(msDate));

			String year = date.substring(0, 4);
			if (!year.equals(lastYear)) {
				System.out.printf("passed all days of %s year\n", lastYear);
				lastYear = year;
			}
		}

		// TimeZone確認
		SafeDateFormat safeUtc = SafeDateFormat.getInstance("yyyy/MM/dd-HH:mm:ss.SSS", TimeZone.getTimeZone("UTC"));
		assertEquals("1970/01/01-00:00:00.000", safeUtc.format(0));
		SafeDateFormat safeGmt9 = SafeDateFormat.getInstance("yyyy/MM/dd-HH:mm:ss.SSS", TimeZone.getTimeZone("GMT+9"));
		SafeDateFormat safeJst = SafeDateFormat.getInstance("yyyy/MM/dd-HH:mm:ss.SSS", TimeZone.getTimeZone("JST"));
		assertEquals(safeJst.format(0), safeGmt9.format(0));
		try {
			SafeDateFormat.getInstance("yyyy/MM/dd-HH:mm:ss.SSS", TimeZone.getTimeZone("America/New_York"));
			fail();
		} catch (Exception ignored) {
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
