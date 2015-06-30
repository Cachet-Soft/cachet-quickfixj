package jp.co.cachet.quickfix.util;

import static org.junit.Assert.assertEquals;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

public class SafeDecimalFormatTest {
	private static final AtomicLong idCounter = new AtomicLong(10000000);
	private static final SafeDecimalFormat safeDecimalFormat = SafeDecimalFormat.getInstance("D-00000000");
	private static final ThreadLocal<DecimalFormat> decimalFormat = new ThreadLocal<DecimalFormat>() {
		protected DecimalFormat initialValue() {
			return new DecimalFormat("D-00000000");
		}
	};

	private static String getIdStr() {
		return safeDecimalFormat.format(idCounter.incrementAndGet());
	}

	@Test
	public void test() throws ParseException {
		String[] patterns = new String[] { "0.000000", "#.######", "#.00000", "b0.000000e", "b#.######e",
				"0000000000",
				"0,000.000", "#,###.###", "D-0000000" };
		for (String pattern : patterns) {
			SafeDecimalFormat sdf = SafeDecimalFormat.getInstance(pattern);
			DecimalFormat df = new DecimalFormat(pattern);
			DecimalFormat dfBigDecimal = new DecimalFormat(pattern);
			dfBigDecimal.setParseBigDecimal(true);
			for (double d : new double[] { -0.000001, 0.000009, -0.000010,
					0.100000, 1.100000, 10.100000, 100.001, 1100.0001 }) {
				assertEquals(d + " " + pattern, df.format(d), sdf.format(d));
				String numStr = df.format(d);
				assertEquals(numStr + " " + pattern, df.parse(numStr).doubleValue(), sdf.parse(numStr),
						Double.MIN_NORMAL);
				assertEquals(numStr + " " + pattern, dfBigDecimal.parse(numStr), sdf.parseBigDecimal(numStr));
			}
		}
		for (String pattern : patterns) {
			SafeDecimalFormat sdf = SafeDecimalFormat.getInstance(pattern);
			DecimalFormat df = new DecimalFormat(pattern);
			DecimalFormat dfBigDecimal = new DecimalFormat(pattern);
			dfBigDecimal.setParseBigDecimal(true);
			for (long d : new long[] { -1, 9, -10, 100, 1100, 100000, 1100000, 10100000 }) {
				assertEquals(d + " " + pattern, df.format(d), sdf.format(d));
				String numStr = df.format(d);
				assertEquals(numStr + " " + pattern, df.parse(numStr).doubleValue(), sdf.parse(numStr),
						Double.MIN_NORMAL);
				assertEquals(numStr + " " + pattern, dfBigDecimal.parse(numStr), sdf.parseBigDecimal(numStr));
			}
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
	public void formatOfSafeDecimalFormat() {
		safeDecimalFormat.format(idCounter.incrementAndGet());
	}

	@Benchmark
	public void formatOfDecimalFormat() {
		decimalFormat.get().format(idCounter.incrementAndGet());
	}

	@Benchmark
	public void parseOfSafeDecimalFormat() {
		safeDecimalFormat.parse(getIdStr());
	}

	@Benchmark
	public void parseOfDecimalFormat() throws ParseException {
		decimalFormat.get().parse(getIdStr());
	}
}
