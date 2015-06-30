package jp.co.cachet.quickfix.util;

import static org.junit.Assert.assertEquals;

import java.text.FieldPosition;
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

		int max = 1000000;
		StringBuilder builder = new StringBuilder(23);
		StringBuffer buffer = new StringBuffer(23);
		FieldPosition pos = new FieldPosition(0);
		// format() latency確認
		long nsStart = System.nanoTime();
		for (int i = 0; i < max; i++) {
			builder.setLength(0);
			safe.format(System.currentTimeMillis(), builder);
		}
		long ns0 = System.nanoTime() - nsStart;

		nsStart = System.nanoTime();
		for (int i = 0; i < max; i++) {
			buffer.setLength(0);
			sdf.format(new Date(), buffer, pos);
		}
		long ns1 = System.nanoTime() - nsStart;
		System.out.printf("format() latency SafeDateFormat(%,d ns) SimpleDateFormat(%,d ns)\n", ns0, ns1);

		String date = sdf.format(new Date());
		// parse() latency確認
		nsStart = System.nanoTime();
		for (int i = 0; i < max; i++) {
			safe.parse(date);
		}
		ns0 = System.nanoTime() - nsStart;

		nsStart = System.nanoTime();
		for (int i = 0; i < max; i++) {
			sdf.parse(date);
		}
		ns1 = System.nanoTime() - nsStart;
		System.out.printf("parse() latency SafeDateFormat(%,d ns) SimpleDateFormat(%,d ns)\n", ns0, ns1);

		// format(timeOnly) latency確認
		safe = SafeDateFormat.getInstance("HH:mm:ss.SSS");
		sdf = new SimpleDateFormat("HH:mm:ss.SSS");
		nsStart = System.nanoTime();
		for (int i = 0; i < max; i++) {
			builder.setLength(0);
			safe.format(System.currentTimeMillis(), builder);
		}
		ns0 = System.nanoTime() - nsStart;

		nsStart = System.nanoTime();
		for (int i = 0; i < max; i++) {
			buffer.setLength(0);
			sdf.format(new Date(), buffer, pos);
		}
		ns1 = System.nanoTime() - nsStart;
		System.out.printf("format(timeOnly) latency SafeDateFormat(%,d ns) SimpleDateFormat(%,d ns)\n", ns0, ns1);
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
