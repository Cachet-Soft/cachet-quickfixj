package jp.co.cachet.quickfix.util;

import static org.junit.Assert.assertEquals;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.ParseException;

import org.junit.Test;

public class SafeDecimalFormatTest {

	@Test
	public void test() throws ParseException {
		String[] patterns = new String[] { "0.000000", "#.######", "b0.000000e", "b#.######e", "0000000000",
				"0,000.000", "#,###.###" };
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

		int max = 1000000;
		StringBuilder builder = new StringBuilder(23);
		StringBuffer buffer = new StringBuffer(23);
		FieldPosition pos = new FieldPosition(0);

		{
			// format(double) latency確認
			SafeDecimalFormat sdf = SafeDecimalFormat.getInstance("0.000000");
			DecimalFormat df = new DecimalFormat("0.000000");
			double number = 123.4567;
			long nsStart = System.nanoTime();
			for (int i = 0; i < max; i++) {
				builder.setLength(0);
				sdf.format(number, builder);
			}
			long ns0 = System.nanoTime() - nsStart;

			nsStart = System.nanoTime();
			for (int i = 0; i < max; i++) {
				buffer.setLength(0);
				df.format(number, buffer, pos);
			}
			long ns1 = System.nanoTime() - nsStart;
			assertEquals(df.format(number), sdf.format(number));
			System.out.printf("format(double) latency SafeDecimalFormat(%,d ns) DecimalFormat(%,d ns)\n", ns0, ns1);
		}

		{
			// format(long) latency確認
			SafeDecimalFormat sdf = SafeDecimalFormat.getInstance("0000000");
			DecimalFormat df = new DecimalFormat("0000000");
			long number = System.currentTimeMillis();
			long nsStart = System.nanoTime();
			for (int i = 0; i < max; i++) {
				builder.setLength(0);
				sdf.format(number, builder);
			}
			long ns0 = System.nanoTime() - nsStart;

			nsStart = System.nanoTime();
			for (int i = 0; i < max; i++) {
				buffer.setLength(0);
				df.format(number, buffer, pos);
			}
			long ns1 = System.nanoTime() - nsStart;
			assertEquals(df.format(number), sdf.format(number));
			System.out.printf("format(long) latency SafeDecimalFormat(%,d ns) DecimalFormat(%,d ns)\n", ns0, ns1);
		}

		{
			// parse() latency確認
			SafeDecimalFormat sdf = SafeDecimalFormat.getInstance("0.000000");
			DecimalFormat df = new DecimalFormat("0.000000");
			String number = df.format(123.4567);
			long nsStart = System.nanoTime();
			for (int i = 0; i < max; i++) {
				sdf.parse(number);
			}
			long ns0 = System.nanoTime() - nsStart;

			nsStart = System.nanoTime();
			for (int i = 0; i < max; i++) {
				df.parse(number);
			}
			long ns1 = System.nanoTime() - nsStart;
			assertEquals(df.parse(number).doubleValue(), sdf.parse(number), Double.MIN_NORMAL);
			System.out.printf("parse() latency SafeDecimalFormat(%,d ns) DecimalFormat(%,d ns)\n", ns0, ns1);
		}
	}

}
